package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.events.event.ReceiptData;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.GenericException;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.PriceRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.PricingPackage;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.VerifyPaymentRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.PaymentCreatedResponseDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.PaymentRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Payment;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.PaymentStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentService {

    @Value("${razorpay.key-id}")
    private String keyId;
    @Value("${razorpay.key-secret}")
    private String keySecret;
    private final PaymentRepository paymentRepository;
    private final PriceRepository priceRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RazorpayClient razorpayClient;
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher eventPublisher;
    @SneakyThrows
    @Transactional
    public ApiResponse createPaymentRequest(CurrentUserPrincipal user, Integer packageId ) {

        boolean check = userRepository.existsById(user.getUserId());
        if (!check) {
            throw new BadRequestException("User not found payment cannot be processed");
        }
        PricingPackage p = priceRepository.findById(packageId).orElse(null);
        if (p == null) {
            log.info("Package not found for id {}", packageId);
            throw new BadRequestException("Package not found");
        }

        String tier = p.getTier();
        String role = user.role.toString();
        if (tier.equalsIgnoreCase("FREE") || !p.getRole().toString().equalsIgnoreCase(role)) {
            log.info("trying to pay for free package");
            throw new BadRequestException("Payment cannot be made against free package");
        }
        Long amount = p.getAmount().movePointRight(2).longValueExact();
        String orderId;
        Payment payment;
        try {
            JSONObject req = new JSONObject();
            req.put("receipt", "recpt_" + UUID.randomUUID().toString());
            req.put("currency", "INR");
            req.put("amount", amount);
            Order order = razorpayClient.orders.create(req);
            orderId = order.get("id");

            payment = Payment.builder()
                    .userId(user.getUserId())
                    .orderId(orderId)
                    .currency("INR")
                    .packageId(packageId)
                    .status(PaymentStatus.CREATED.name())
                    .amountPaise(amount)
                    .build();
            Payment saved = paymentRepository.save(payment);
            if (saved == null) {
                log.error("Payment not saved");
                throw new BadRequestException("Internal server error");
            }
        } catch (RazorpayException e) {
            log.error(e.getMessage());
            throw new BadRequestException("Payment cannot be made against free package. \nCause :" + e.getCause() + "\n Description :" + e.getDescription());
        }
        return ResponseHelper.commonSuccess("Payment Created Successfully", GetDetails.<PaymentCreatedResponseDto>builder()
                .details(PaymentCreatedResponseDto.builder()
                        .orderId(orderId)
                        .paymentId(payment.getPaymentId())
                        .amount(amount)
                        .packagePurchased(p.getTier())
                        .build())
                .build());
    }


    private boolean isSignatureValid(String orderId, String paymentId, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(
                    mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    @SneakyThrows
    public ApiResponse verifyAndActivate(CurrentUserPrincipal user, VerifyPaymentRequestDto req) {
        Payment payment = paymentRepository.findByOrderId(req.razorpay_order_id())
                .orElseThrow(() -> new BadRequestException("Unknown order"));
        if (!payment.getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Order does not belong to you");
        }
        if (!isSignatureValid(req.razorpay_order_id(), req.razorpay_payment_id(), req.razorpay_signature())) {
            throw new BadRequestException("Payment verification failed");
        }
        int updated = paymentRepository.updatePaymentStatus(PaymentStatus.PAID.name(),req.razorpay_payment_id(),req.razorpay_signature(),Instant.now(), payment.getOrderId());
        if (updated == 0) {              // pehle hi PAID ho chuka, dobara kuch mat karo
            log.info("Order {} already processed", req.razorpay_order_id());
            throw new GenericException("Order already processed");
        }
        PricingPackage pkg = priceRepository.findById(payment.getPackageId()).orElseThrow();
        Instant endDate=subscriptionService.activatePlan(payment.getUserId(), pkg.getTier(), pkg.getDuration());
        ReceiptData receipt=new ReceiptData(user.email,pkg.getTier(),pkg.getDuration(),pkg.getAmount(),req.razorpay_order_id(),req.razorpay_payment_id(),payment.getUpdatedAt(),endDate,true);
        eventPublisher.publishEvent(receipt);
        return ResponseHelper.commonSuccess("Payment verified, plan activated",
                GetDetails.<String>builder().details("PRO").build());
    }

    @Transactional
    public ApiResponse recordFailure(CurrentUserPrincipal user, String orderId, String reason) {
        paymentRepository.saveFailureReason(orderId, user.getUserId(), reason, Instant.now());
        return ResponseHelper.commonSuccess("Recorded",
                GetDetails.<String>builder().details("OK").build());
    }
}
