package com.eventplatform.reservation.repository;

import com.eventplatform.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.Status status);

    Optional<Reservation> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // 예약 완료(ACTIVE)된 행사 ID 목록 - 추천 서비스용
    List<Reservation> findByUserIdAndStatusIn(Long userId, List<Reservation.Status> statuses);
}
