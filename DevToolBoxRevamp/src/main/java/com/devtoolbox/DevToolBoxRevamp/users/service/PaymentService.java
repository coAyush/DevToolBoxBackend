package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.config.RazorpayConfig;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.PriceRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.PricingPackage;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.PackagePaymentRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.PaymentCreatedResponseDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.PaymentRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Payment;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.PaymentStatus;
import com.google.gson.JsonObject;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    @SneakyThrows
    @Transactional
    public ApiResponse createPaymentRequest(CurrentUserPrincipal user, PackagePaymentRequestDto dto) {
        boolean check = userRepository.existsById(user.getUserId());
        if (!check) {
            throw new BadRequestException("User not found payment cannot be processed");
        }
        PricingPackage p = priceRepository.findById(dto.getPackageId()).orElse(null);
        if (p == null) {
            log.info("Package not found for id {}", dto.getPackageId());
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
                    .packageId(dto.getPackageId())
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

    private
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
}
