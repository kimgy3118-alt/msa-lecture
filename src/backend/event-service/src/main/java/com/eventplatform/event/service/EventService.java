package com.eventplatform.event.service;

import com.eventplatform.event.dto.EventDto;
import com.eventplatform.event.entity.Event;
import com.eventplatform.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    /**
     * 행사 등록 (기관 담당자만 가능 - SecurityConfig에서 role 검증)
     */
    @Transactional
    public EventDto.EventResponse createEvent(EventDto.CreateRequest request, Long organizerId) {
        
        // 날짜 검증
        if (!request.getEventStartAt().isBefore(request.getEventEndAt())) {
            throw new IllegalArgumentException(
                    "행사 종료 일시는 시작 일시보다 늦어야 합니다."
            );
        }

        if (request.getEventType().requiresReservation()) {
            if (!request.getRegistrationStartAt().isBefore(request.getRegistrationEndAt())) {
                throw new IllegalArgumentException("접수 종료 일시는 시작 일시보다 늦어야 합니다.");
            }
            if (request.getRegistrationEndAt().isAfter(request.getEventStartAt())) {
                throw new IllegalArgumentException("접수는 행사 시작 전까지 종료되어야 합니다.");
            }
        }

        if (request.getEventType() == Event.EventType.FREE_VISIT
                && request.getPrice().signum() != 0) {
            throw new IllegalArgumentException("자유 방문형 행사의 가격은 0원이어야 합니다.");
        }

        if (request.getEventType() == Event.EventType.FREE_RESERVATION
                && request.getPrice().signum() != 0) {
            throw new IllegalArgumentException("무료 예약형 행사의 가격은 0원이어야 합니다.");
        }

        if (request.getEventType() == Event.EventType.PAID_RESERVATION
                && request.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("유료 예약형 행사의 가격은 0원보다 커야 합니다.");
        }
        
        
        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .eventType(request.getEventType())
                .venue(request.getVenue())
                .organizerName(request.getOrganizerName())
                .imageUrl(request.getImageUrl())
                .price(request.getPrice())

                // 추가
                .eventStartAt(request.getEventStartAt())
                .eventEndAt(request.getEventEndAt())
                .registrationStartAt(request.getRegistrationStartAt())
                .registrationEndAt(request.getRegistrationEndAt())
                .capacity(request.getCapacity())

                .organizerId(organizerId)
                .build();

        return EventDto.EventResponse.from(eventRepository.save(event));
    }

    /**
     * 행사 단건 조회
     */
    public EventDto.EventResponse getEvent(Long id) {
        Event event = findEventById(id);
        return EventDto.EventResponse.from(event);
    }

    /**
     * 전체 활성 행사 목록 조회
     */
    public List<EventDto.EventResponse> getAllEvents() {
        return eventRepository.findByStatus(Event.Status.ACTIVE).stream()
                .map(EventDto.EventResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 행사 조회
     */
    public List<EventDto.EventResponse> getEventsByCategory(Event.Category category) {
        return eventRepository.findByCategoryAndStatus(category, Event.Status.ACTIVE).stream()
                .map(EventDto.EventResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 행사 존재 여부 확인 (Reservation Service → Event Service REST 호출용)
     */
    public boolean existsEvent(Long id) {
        return eventRepository.existsById(id);
    }

    /**
     * event.increaseReservationCount()를 실행하면 작성한 코드에 의해 접수 기간과 정원이 검사됨
     */
    @Transactional
    public void increaseReservationCount(Long eventId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "행사를 찾을 수 없습니다: " + eventId
                        )
                );

        event.increaseReservationCount();
    }

    /**
     * 추천 서비스용: 카테고리별 미예약 행사 조회
     * - excludeEventIds: 이미 예약한 행사 ID 목록
     */
    public List<EventDto.EventResponse> getRecommendEvents(
            Event.Category category, List<Long> excludeEventIds) {

        List<Event> events = excludeEventIds.isEmpty()
                ? eventRepository.findByCategoryAndStatus(category, Event.Status.ACTIVE)
                : eventRepository.findByCategoryAndStatusAndIdNotIn(
                        category, Event.Status.ACTIVE, excludeEventIds);

        // 예약생 수 기준 내림차순 정렬
        return events.stream()
                .sorted((a, b) -> b.getReservationCount() - a.getReservationCount())
                .map(EventDto.EventResponse::from)
                .collect(Collectors.toList());
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("행사를 찾을 수 없습니다: " + id));
    }
}
