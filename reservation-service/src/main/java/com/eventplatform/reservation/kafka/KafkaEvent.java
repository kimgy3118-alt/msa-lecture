package com.eventplatform.reservation.kafka;

import lombok.*;

/**
 * Kafka 이벤트 메시지 DTO
 */
public class KafkaEvent {

    /**
     * Payment Service → Reservation Service
     * 결제 완료 이벤트 수신
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentCompletedEvent {
        private Long paymentId;
        private Long userId;
        private Long eventId;
        private String status; // COMPLETED
    }

    /**
     * Reservation Service → Recommend Service
     * 예약 활성화 완료 이벤트 발행
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationCompletedEvent {
        private Long reservationId;
        private Long userId;
        private Long eventId;
    }
}
