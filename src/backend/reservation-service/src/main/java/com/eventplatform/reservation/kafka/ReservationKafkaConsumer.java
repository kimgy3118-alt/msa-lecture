package com.eventplatform.reservation.kafka;

import com.eventplatform.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationKafkaConsumer {

    private final ReservationService reservationService;

    /**
     * payment.completed 이벤트 수신
     * → 예약 상태 PENDING → ACTIVE 로 변경
     * → reservation.completed 이벤트 발행 (→ Recommend Service)
     *
     * payment-service 쪽은 JsonSerializer + type header 미포함으로 이벤트를 발행하므로,
     * 여기서는 특정 DTO 타입으로 바로 받지 않고 Map<String, Object> 로 받아 처리한다.
     */
    @KafkaListener(
            topics = "${kafka.topic.payment-completed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(Map<String, Object> event) {
        log.info("[Kafka Consumer] payment.completed raw event 수신: {}", event);

        try {
            Object userIdValue = event.get("userId");
            Object eventIdValue = event.get("eventId");

            if (userIdValue == null || eventIdValue == null) {
                throw new IllegalArgumentException("Kafka 이벤트에 userId 또는 eventId가 없습니다.");
            }

            Long userId = ((Number) userIdValue).longValue();
            Long eventId = ((Number) eventIdValue).longValue();

            log.info("[Kafka Consumer] payment.completed 파싱 완료 - userId: {}, eventId: {}",
                    userId, eventId);

            reservationService.confirmPaidReservation(userId, eventId);

            log.info("[Kafka Consumer] 예약 활성화 완료 - userId: {}, eventId: {}",
                    userId, eventId);

        } catch (Exception e) {
            log.error("[Kafka Consumer] 예약 활성화 실패 - event: {}, error: {}",
                    event, e.getMessage(), e);
        }
    }
}
