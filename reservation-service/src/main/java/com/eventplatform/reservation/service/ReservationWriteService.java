package com.eventplatform.reservation.service;

import com.eventplatform.reservation.entity.Reservation;
import com.eventplatform.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationWriteService {

    private final ReservationRepository reservationRepository;

    /**
     * 반드시 독립 트랜잭션으로 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation createReservation(Long userId, Long eventId, Reservation.Status status) {

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .status(status)
                        .build()
        );

        log.info("[ReservationWriteService] {} reservation 생성 완료 - reservationId: {}, userId: {}, eventId: {}", status,
                reservation.getId(), userId, eventId);

        return reservation;
    }
}
