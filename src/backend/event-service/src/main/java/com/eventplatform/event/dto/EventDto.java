package com.eventplatform.event.dto;

import com.eventplatform.event.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Positive;

public class EventDto {

    // 행사 등록 요청
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "행사명은 필수입니다")
        private String title;

        private String description;

        @NotNull(message = "카테고리는 필수입니다")
        private Event.Category category;

        @NotNull(message = "행사 유형은 필수입니다")
        private Event.EventType eventType;

        @NotBlank(message = "장소는 필수입니다")
        private String venue;

        @NotBlank(message = "주관 기관명은 필수입니다")
        private String organizerName;

    private String imageUrl;

        @NotNull(message = "가격은 필수입니다")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다")
        private BigDecimal price;

        @NotNull(message = "행사 시작 일시는 필수입니다")
        private LocalDateTime eventStartAt;

        @NotNull(message = "행사 종료 일시는 필수입니다")
        private LocalDateTime eventEndAt;

        @NotNull(message = "접수 시작 일시는 필수입니다")
        private LocalDateTime registrationStartAt;

        @NotNull(message = "접수 종료 일시는 필수입니다")
        private LocalDateTime registrationEndAt;

        @NotNull(message = "최대 신청자 수는 필수입니다")
        @Positive(message = "최대 신청자 수는 1명 이상이어야 합니다")
        private Integer capacity;
    }

    // 행사 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventResponse {
        private Long id;
        private String title;
        private String description;
        private Event.Category category;
        private Event.EventType eventType;
        private String venue;
        private String organizerName;
        private String imageUrl;
        private BigDecimal price;
        private Long organizerId;
        private Integer reservationCount;
        private Event.Status status;
        private LocalDateTime createdAt;

        private LocalDateTime eventStartAt;
        private LocalDateTime eventEndAt;
        private LocalDateTime registrationStartAt;
        private LocalDateTime registrationEndAt;
        private Event.RegistrationStatus registrationStatus;
        private Integer capacity;

        public static EventResponse from(Event event) {
            return EventResponse.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .category(event.getCategory())
                    .eventType(event.getEventType())
                    .venue(event.getVenue())
                    .organizerName(event.getOrganizerName())
                    .imageUrl(event.getImageUrl())
                    .price(event.getPrice())
                    .organizerId(event.getOrganizerId())
                    .reservationCount(event.getReservationCount())
                    .status(event.getStatus())
                    .createdAt(event.getCreatedAt())
                    .eventStartAt(event.getEventStartAt())
                    .eventEndAt(event.getEventEndAt())
                    .registrationStartAt(event.getRegistrationStartAt())
                    .registrationEndAt(event.getRegistrationEndAt())
                    .registrationStatus(event.getRegistrationStatus())
                    .capacity(event.getCapacity())
                    .build();
        }
    }

    // 공통 API 응답 래퍼
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message("성공")
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }

    // 추천 서비스용 응답 (카테고리 기반 미예약 행사 목록)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendResponse {
        private List<EventResponse> events;
        private Event.Category category;
    }
}
