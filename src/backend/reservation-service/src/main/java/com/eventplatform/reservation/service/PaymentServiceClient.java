package com.eventplatform.reservation.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Payment Service: 결제 요청 (동기 REST)
     */
    public PaymentResult requestPayment(Long userId, Long eventId, BigDecimal amount) {
        try {
            PaymentRequest request = new PaymentRequest(userId, eventId, amount);

            PaymentResult result = webClientBuilder.build()
                    .post()
                    .uri("http://payment-service:8084/api/payments/internal/request")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResult.class)
                    .block();

            log.info("[PaymentServiceClient] 결제 요청 완료 - userId: {}, eventId: {}, result: {}",
                    userId, eventId, result != null ? result.getStatus() : "null");

            return result;
        } catch (Exception e) {
            log.error("[PaymentServiceClient] 결제 요청 실패 - userId: {}, eventId: {}, error: {}",
                    userId, eventId, e.getMessage(), e);
            throw new RuntimeException("Payment Service 연결 실패");
        }
    }

    @Getter
    @NoArgsConstructor
    static class PaymentRequest {
        private Long userId;
        private Long eventId;
        private BigDecimal amount;

        PaymentRequest(Long userId, Long eventId, BigDecimal amount) {
            this.userId = userId;
            this.eventId = eventId;
            this.amount = amount;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentResult {
        private Long paymentId;
        private String status; // COMPLETED / FAILED
    }
}