package com.eventplatform.payment.repository;

import com.eventplatform.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    Optional<Payment> findByUserIdAndEventId(Long userId, Long eventId);

    Optional<Payment> findByTransactionId(String transactionId);
}