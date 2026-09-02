package com.lecture.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

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

    // 강사 ID (users 테이블 참조 - 직접 JOIN 없이 ID만 보관)
    @Column(nullable = false)
    private Long instructorId;

    // 수강생 수 (추천 서비스 정렬 기준)
    @Column(nullable = false)
    @Builder.Default
    private Integer enrollmentCount = 0;

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
        BACKEND, FRONTEND, DEVOPS, DATA_SCIENCE, MOBILE, SECURITY, DATABASE, OTHER
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

    public void increaseEnrollmentCount() {
        RegistrationStatus status = getRegistrationStatus();

        if (status == RegistrationStatus.BEFORE) {
            throw new IllegalStateException("아직 접수 기간이 아닙니다.");
        }

        if (status == RegistrationStatus.CLOSED) {
            throw new IllegalStateException("접수가 종료되었습니다.");
        }

        if (enrollmentCount >= capacity) {
            throw new IllegalStateException("최대 신청 인원이 초과되었습니다.");
        }

        enrollmentCount++;
    }
}
