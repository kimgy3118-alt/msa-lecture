package com.eventplatform.event.controller;

import com.eventplatform.event.dto.EventDto;
import com.eventplatform.event.entity.Event;
import com.eventplatform.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * POST /events - 행사 등록 (기관 담당자만)
     * Gateway에서 전달한 X-User-Id 헤더로 기관 담당자 ID 추출
     */
    @PostMapping
    public ResponseEntity<EventDto.ApiResponse<EventDto.EventResponse>> createEvent(
            @Valid @RequestBody EventDto.CreateRequest request,
            @RequestHeader("X-User-Id") Long organizerId) {

        EventDto.EventResponse response = eventService.createEvent(request, organizerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventDto.ApiResponse.success(response));
    }

    /**
     * GET /events - 전체 행사 목록
     */
    @GetMapping
    public ResponseEntity<EventDto.ApiResponse<List<EventDto.EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(
                EventDto.ApiResponse.success(eventService.getAllEvents())
        );
    }

    /**
     * GET /events/{id} - 행사 상세
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDto.ApiResponse<EventDto.EventResponse>> getEvent(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                EventDto.ApiResponse.success(eventService.getEvent(id))
        );
    }

    /**
     * GET /events/category/{category} - 카테고리별 행사
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<EventDto.ApiResponse<List<EventDto.EventResponse>>> getEventsByCategory(
            @PathVariable Event.Category category) {
        return ResponseEntity.ok(
                EventDto.ApiResponse.success(eventService.getEventsByCategory(category))
        );
    }

    /**
     * GET /events/internal/exists/{id} - 행사 존재 여부 (Reservation Service 호출)
     */
    @GetMapping("/internal/exists/{id}")
    public ResponseEntity<Boolean> existsEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.existsEvent(id));
    }

    /**
     * GET /events/internal/{id} - 행사 상세 조회 (Reservation Service 내부 호출용)
     * - 내 예약 목록 응답 조립 시 사용
     * - 래퍼 없이 EventResponse만 직접 반환
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<EventDto.EventResponse> getEventInternal(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    /**
     * POST /events/internal/{id}/reservation-count - 예약생 수 증가 (Reservation Service 호출)
     */
    @PostMapping("/internal/{id}/reservation-count")
    public ResponseEntity<Void> increaseReservationCount(@PathVariable Long id) {
        eventService.increaseReservationCount(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /events/internal/recommend - 추천 서비스용 미예약 행사 조회
     * category: 카테고리, excludeIds: 이미 예약한 행사 ID 목록
     */
    @GetMapping("/internal/recommend")
    public ResponseEntity<List<EventDto.EventResponse>> getRecommendEvents(
            @RequestParam Event.Category category,
            @RequestParam(defaultValue = "") List<Long> excludeIds) {
        return ResponseEntity.ok(eventService.getRecommendEvents(category, excludeIds));
    }
}