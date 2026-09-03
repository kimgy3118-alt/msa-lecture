package com.eventplatform.reservation.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.reservation-completed}")
    private String reservationCompletedTopic;

    /**
     * reservation.completed 이벤트 발행
     * → Recommend Service가 수신하여 추천 갱신
     */
    public void publishReservationCompleted(KafkaEvent.ReservationCompletedEvent event) {
        log.info("[Kafka Producer] reservation.completed 발행 - reservationId: {}, userId: {}, eventId: {}",
                event.getReservationId(), event.getUserId(), event.getEventId());

        kafkaTemplate.send(reservationCompletedTopic, String.valueOf(event.getUserId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka Producer] reservation.completed 발행 실패: {}", ex.getMessage());
                    } else {
                        log.info("[Kafka Producer] reservation.completed 발행 성공 - offset: {}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
