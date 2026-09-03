package com.eventplatform.reservation.dto;

import com.eventplatform.reservation.entity.Reservation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public class ReservationDto {

    // 예약신청 요청
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReserveRequest {
        @NotNull(message = "행사 ID는 필수입니다")
        private Long eventId;
    }

    // 행사 요약 정보 (내 예약 목록 표시용)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventSummary {
        private Long id;
        private String title;
        private String description;
        private String category;
        private BigDecimal price;
        private String thumbnail;
        private String imageUrl;
        private String venue;
        private String organizerName;
        private Integer reservationCount;
    }

    // 예약 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationResponse {
        private Long id;
        private Long userId;
        private Long eventId;
        private Reservation.Status status;
        private LocalDateTime createdAt;

        // 추가
        private EventSummary event;

        public static ReservationResponse from(Reservation reservation) {
            return ReservationResponse.builder()
                    .id(reservation.getId())
                    .userId(reservation.getUserId())
                    .eventId(reservation.getEventId())
                    .status(reservation.getStatus())
                    .createdAt(reservation.getCreatedAt())
                    .build();
        }

        public static ReservationResponse from(Reservation reservation, EventSummary event) {
            return ReservationResponse.builder()
                    .id(reservation.getId())
                    .userId(reservation.getUserId())
                    .eventId(reservation.getEventId())
                    .status(reservation.getStatus())
                    .createdAt(reservation.getCreatedAt())
                    .event(event)
                    .build();
        }
    }

    // 추천 서비스용: 예약 이력 조회 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationHistoryResponse {
        private Long userId;
        private List<Long> activeEventIds;
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
}
