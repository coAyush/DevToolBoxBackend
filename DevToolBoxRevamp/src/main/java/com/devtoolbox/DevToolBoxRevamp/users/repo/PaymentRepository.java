package com.devtoolbox.DevToolBoxRevamp.users.repo;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Payment;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.PaymentStatus;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findById(UUID id);
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByOrderId(String orderId);
    @Modifying(clearAutomatically = true)
    @Query(
            value = """
              UPDATE payment p
              SET status= :status,payment_id=:paymentId,signature= :signature,updated_at=:updatedAt
              WHERE order_id= :orderId AND status = 'CREATED'
            """
    ,nativeQuery = true)
    int updatePaymentStatus(@Param("status")String status, @Param("paymentId")String paymentId, @Param("signature")String signature, @Param("updatedAt") Instant updatedAt, @Param("orderId")String orderId);

    @Modifying
    @Query("""
        UPDATE Payment p SET p.failureReason = :reason, p.updatedAt = :now
         WHERE p.orderId = :orderId AND p.userId = :userId AND p.status = 'CREATED'
        """)
    int saveFailureReason(@Param("orderId") String orderId, @Param("userId") UUID userId,
                          @Param("reason") String reason, @Param("now") Instant now);
}
