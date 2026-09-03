package com.eventplatform.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    /** 방문만 가능한 행사 / 무료 예약 / 유료 예약 */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private String organizerName;

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "event_start_at", nullable = false)
    private LocalDateTime eventStartAt;

    @Column(name = "event_end_at", nullable = false)
    private LocalDateTime eventEndAt;

    @Column(name = "registration_start_at", nullable = false)
    private LocalDateTime registrationStartAt;

    @Column(name = "registration_end_at", nullable = false)
    private LocalDateTime registrationEndAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer capacity = 100;

    // 기관 담당자 ID (users 테이블 참조 - 직접 JOIN 없이 ID만 보관)
    @Column(nullable = false)
    private Long organizerId;

    // 예약 확정 인원 (추천 서비스 정렬 기준)
    @Column(nullable = false)
    @Builder.Default
    private Integer reservationCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Category {
        FESTIVAL, EXHIBITION, PERFORMANCE, CULTURE_EXPERIENCE, SPORTS, EDUCATION, OTHER
    }

    public enum EventType {
        FREE_VISIT, FREE_RESERVATION, PAID_RESERVATION;

        public boolean requiresReservation() {
            return this != FREE_VISIT;
        }

        public boolean requiresPayment() {
            return this == PAID_RESERVATION;
        }
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    public enum RegistrationStatus {
    BEFORE, OPEN, CLOSED
}

    @Transient
    public RegistrationStatus getRegistrationStatus() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(registrationStartAt)) {
            return RegistrationStatus.BEFORE;
        }

        if (!now.isBefore(registrationEndAt)) {
            return RegistrationStatus.CLOSED;
        }

        return RegistrationStatus.OPEN;
    }

    public void increaseReservationCount() {
        if (!eventType.requiresReservation()) {
            throw new IllegalStateException("자유 방문형 행사는 예약할 수 없습니다.");
        }
        RegistrationStatus status = getRegistrationStatus();

        if (status == RegistrationStatus.BEFORE) {
            throw new IllegalStateException("아직 접수 기간이 아닙니다.");
        }

        if (status == RegistrationStatus.CLOSED) {
            throw new IllegalStateException("접수가 종료되었습니다.");
        }

        if (reservationCount >= capacity) {
            throw new IllegalStateException("최대 신청 인원이 초과되었습니다.");
        }

        reservationCount++;
    }
}
