package com.eventplatform.reservation.service;

import com.eventplatform.reservation.dto.ReservationDto;
import com.eventplatform.reservation.entity.Reservation;
import com.eventplatform.reservation.kafka.ReservationKafkaProducer;
import com.eventplatform.reservation.kafka.KafkaEvent;
import com.eventplatform.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventServiceClient eventServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final ReservationKafkaProducer kafkaProducer;
    private final ReservationWriteService reservationWriteService;

    // 중복 검사
    // → 접수 기간 및 정원 검사
    // → 신청 인원 증가
    // → Reservation 생성
    // → 결제
    public ReservationDto.ReservationResponse reserve(
            Long userId,
            Long eventId
    ) {
        // 1. 행사와 예약/결제 정책을 한 번에 확인한다.
        Map<String, Object> event = eventServiceClient.getEvent(eventId);
        String eventType = String.valueOf(event.get("eventType"));
        if ("FREE_VISIT".equals(eventType)) {
            throw new IllegalArgumentException("자유 방문형 행사는 예약 없이 방문할 수 있습니다.");
        }

        // 2. 중복 신청 확인
        if (reservationRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new IllegalArgumentException(
                    "이미 신청한 행사입니다."
            );
        }

        // 3. 접수 기간과 정원을 검사하고 인원 1명 증가
        // 검사에 실패하면 여기서 예외가 발생하고 아래 코드는 실행되지 않음
        eventServiceClient.increaseReservationCount(eventId);

        // 4. 신청 정보 생성
        boolean paidReservation = "PAID_RESERVATION".equals(eventType);
        Reservation reservation = reservationWriteService.createReservation(
                userId,
                eventId,
                paidReservation ? Reservation.Status.PAYMENT_PENDING : Reservation.Status.CONFIRMED
        );

        // 유료 예약만 행사 등록 가격으로 결제한다. 무료 예약은 즉시 확정한다.
        if (paidReservation) {
            paymentServiceClient.requestPayment(userId, eventId, toBigDecimal(event.get("price")));
        } else {
            kafkaProducer.publishReservationCompleted(
                    KafkaEvent.ReservationCompletedEvent.builder()
                            .reservationId(reservation.getId())
                            .userId(userId)
                            .eventId(eventId)
                            .build());
        }

        log.info(
                "[ReservationService] 행사 예약 완료 ({}) - reservationId: {}",
                reservation.getStatus(),
                reservation.getId()
        );

        return ReservationDto.ReservationResponse.from(reservation);
    }

    /**
     * 예약 활성화
     */
    @Transactional
    public void confirmPaidReservation(Long userId, Long eventId) {
        Reservation reservation = reservationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "예약 정보를 찾을 수 없습니다 - userId: " + userId + ", eventId: " + eventId));

        if (reservation.getStatus() != Reservation.Status.PAYMENT_PENDING) {
            return;
        }
        reservation.confirm();

        // eventServiceClient.increaseReservationCount(eventId);

        kafkaProducer.publishReservationCompleted(
                KafkaEvent.ReservationCompletedEvent.builder()
                        .reservationId(reservation.getId())
                        .userId(userId)
                        .eventId(eventId)
                        .build()
        );

        log.info("[ReservationService] 예약 활성화 완료 - reservationId: {}", reservation.getId());
    }

    /**
     * 사용자 예약 목록 조회
     * - event-service에서 행사 상세 정보를 붙여서 반환
     */
    public List<ReservationDto.ReservationResponse> getReservationsByUser(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);

        return reservations.stream()
                .map(reservation -> {
                    Map<String, Object> eventInfo = eventServiceClient.getEvent(reservation.getEventId());

                    ReservationDto.EventSummary eventSummary = ReservationDto.EventSummary.builder()
                            .id(toLong(eventInfo.get("id")))
                            .title((String) eventInfo.get("title"))
                            .description((String) eventInfo.get("description"))
                            .category(normalizeCategory((String) eventInfo.get("category")))
                            .price(toBigDecimal(eventInfo.get("price")))
                            .thumbnail((String) eventInfo.get("thumbnail"))
                            .organizerName(
                                    firstNonNull(
                                            (String) eventInfo.get("organizerName"),
                                            (String) eventInfo.get("organizer_name")
                                    )
                            )
                            .reservationCount(toInteger(
                                    firstNonNullObject(
                                            eventInfo.get("reservationCount"),
                                            eventInfo.get("reservation_count")
                                    )
                            ))
                            .build();

                    return ReservationDto.ReservationResponse.from(reservation, eventSummary);
                })
                .collect(Collectors.toList());
    }

    /**
     * 예약 이력 조회 - 추천 서비스용
     */
    public ReservationDto.ReservationHistoryResponse getReservationHistory(Long userId) {
        List<Long> activeEventIds = reservationRepository
                .findByUserIdAndStatus(userId, Reservation.Status.CONFIRMED)
                .stream()
                .map(Reservation::getEventId)
                .collect(Collectors.toList());

        return ReservationDto.ReservationHistoryResponse.builder()
                .userId(userId)
                .activeEventIds(activeEventIds)
                .build();
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;

        return switch (category) {
            case "BACKEND" -> "백엔드";
            case "FRONTEND" -> "프론트엔드";
            case "DEVOPS" -> "DevOps";
            case "DATA" -> "데이터";
            case "AI" -> "AI";
            default -> category;
        };
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal amount) return amount;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(value.toString());
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Object firstNonNullObject(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
