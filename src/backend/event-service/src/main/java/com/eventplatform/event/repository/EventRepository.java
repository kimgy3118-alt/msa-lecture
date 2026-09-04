package com.eventplatform.event.repository;

import com.eventplatform.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 카테고리별 행사 조회 (추천 서비스 사용)
    List<Event> findByCategoryAndStatus(Event.Category category, Event.Status status);

    // 기관 담당자별 행사 조회
    List<Event> findByOrganizerId(Long organizerId);

    // 활성 행사 전체 조회
    List<Event> findByStatus(Event.Status status);

    // 카테고리별 + 특정 ID 제외 조회 (추천 서비스: 이미 예약한 행사 제외)
    List<Event> findByCategoryAndStatusAndIdNotIn(
            Event.Category category,
            Event.Status status,
            List<Long> excludeIds
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Event c WHERE c.id = :eventId")
    Optional<Event> findByIdForUpdate(@Param("eventId") Long eventId);
}
