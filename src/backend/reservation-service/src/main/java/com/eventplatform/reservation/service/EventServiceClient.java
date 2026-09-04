package com.eventplatform.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventServiceClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Event Service: 행사 존재 여부 확인 (동기 REST)
     */
    public boolean existsEvent(Long eventId) {
        try {
            Boolean exists = webClientBuilder.build()
                    .get()
                    .uri("http://event-service/api/events/internal/exists/{id}", eventId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("[EventServiceClient] 행사 존재 확인 실패 - eventId: {}, error: {}",
                    eventId, e.getMessage());
            throw new RuntimeException("Event Service 연결 실패");
        }
    }

    /**
     * Event Service: 행사 상세 조회
     * - 내 예약 목록 응답에 event 정보를 붙일 때 사용
     * - event-service 쪽에 GET /api/events/internal/{id} 엔드포인트가 있어야 함
     */
    public Map<String, Object> getEvent(Long eventId) {
        try {
            Map<String, Object> responseBody = webClientBuilder.build()
                    .get()
                    .uri("http://event-service/api/events/internal/{id}", eventId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (responseBody == null) {
                throw new RuntimeException("Event Service 응답 본문이 비어 있습니다.");
            }

            log.info("[EventServiceClient] 행사 상세 조회 성공 - eventId: {}", eventId);
            log.debug("[EventServiceClient] 행사 상세 응답 - eventId: {}, body: {}", eventId, responseBody);

            /*
             * 응답 형태가 다음 둘 중 하나일 수 있으므로 둘 다 처리
             *
             * 1) 래퍼 응답
             * {
             *   "success": true,
             *   "message": "성공",
             *   "data": { ...event fields... }
             * }
             *
             * 2) 바로 행사 객체 반환
             * {
             *   "id": 1,
             *   "title": "...",
             *   ...
             * }
             */
            Object data = responseBody.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> eventMap = (Map<String, Object>) dataMap;
                return eventMap;
            }

            return responseBody;
        } catch (Exception e) {
            log.error("[EventServiceClient] 행사 상세 조회 실패 - eventId: {}, error: {}",
                    eventId, e.getMessage());
            throw new RuntimeException("Event Service 행사 상세 조회 실패");
        }
    }

    /**
     * Event Service:
     * 접수 기간과 정원을 검사하고 신청 인원을 1명 증가시킨다.
     */
    public void increaseReservationCount(Long eventId) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(
                            "http://event-service/api/events/internal/{id}/reservation-count",
                            eventId
                    )
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            response -> response
                                    .bodyToMono(
                                            new ParameterizedTypeReference<Map<String, Object>>() {}
                                    )
                                    .map(body -> new IllegalStateException(
                                            String.valueOf(
                                                    body.getOrDefault(
                                                            "message",
                                                            "행사 신청이 불가능합니다."
                                                    )
                                            )
                                    ))
                    )
                    .toBodilessEntity()
                    .block();

            log.info(
                    "[EventServiceClient] 신청 인원 증가 완료 - eventId: {}",
                    eventId
            );

        } catch (IllegalStateException e) {
            // 접수 기간 종료 또는 정원 초과 오류를 Reservation Service로 전달
            throw e;

        } catch (Exception e) {
            log.error(
                    "[EventServiceClient] 신청 인원 증가 실패 - eventId: {}, error: {}",
                    eventId,
                    e.getMessage()
            );

            throw new RuntimeException("Event Service 연결 실패");
        }
    }
}