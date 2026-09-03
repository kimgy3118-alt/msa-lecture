package com.eventplatform.reservation.controller;

import com.eventplatform.reservation.dto.ReservationDto;
import com.eventplatform.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * POST /reservations - 예약신청
     * Gateway에서 X-User-Id 헤더로 사용자 ID 전달
     */
    @PostMapping
    public ResponseEntity<ReservationDto.ApiResponse<ReservationDto.ReservationResponse>> reserve(
            @Valid @RequestBody ReservationDto.ReserveRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        ReservationDto.ReservationResponse response =
                reservationService.reserve(userId, request.getEventId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationDto.ApiResponse.success(response));
    }

    /**
     * GET /reservations/my - 내 예약 목록 조회
     * Gateway가 전달한 X-User-Id 헤더를 사용
     */
    @GetMapping("/my")
    public ResponseEntity<ReservationDto.ApiResponse<List<ReservationDto.ReservationResponse>>> getMyReservations(
            @RequestHeader("X-User-Id") Long userId) {

        List<ReservationDto.ReservationResponse> response =
                reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(ReservationDto.ApiResponse.success(response));
    }

    /**
     * GET /reservations/user/{userId} - 특정 사용자 예약 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ReservationDto.ApiResponse<List<ReservationDto.ReservationResponse>>> getReservations(
            @PathVariable Long userId) {

        List<ReservationDto.ReservationResponse> response =
                reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(ReservationDto.ApiResponse.success(response));
    }

    /**
     * GET /reservations/internal/history/{userId} - 예약 이력 조회 (Recommend Service용)
     */
    @GetMapping("/internal/history/{userId}")
    public ResponseEntity<ReservationDto.ReservationHistoryResponse> getReservationHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(reservationService.getReservationHistory(userId));
    }
}
