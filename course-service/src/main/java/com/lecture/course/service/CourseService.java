package com.lecture.course.service;

import com.lecture.course.dto.CourseDto;
import com.lecture.course.entity.Course;
import com.lecture.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    /**
     * 강의 등록 (강사만 가능 - SecurityConfig에서 role 검증)
     */
    @Transactional
    public CourseDto.CourseResponse createCourse(CourseDto.CreateRequest request, Long instructorId) {
        
        // 날짜 검증은 정확히 여기에 넣기
        if (!request.getEventStartAt().isBefore(request.getEventEndAt())) {
            throw new IllegalArgumentException(
                    "행사 종료 일시는 시작 일시보다 늦어야 합니다."
            );
        }

        if (!request.getRegistrationStartAt()
                .isBefore(request.getRegistrationEndAt())) {
            throw new IllegalArgumentException(
                    "접수 종료 일시는 시작 일시보다 늦어야 합니다."
            );
        }

        if (request.getRegistrationEndAt()
                .isAfter(request.getEventStartAt())) {
            throw new IllegalArgumentException(
                    "접수는 행사 시작 전까지 종료되어야 합니다."
            );
        }
        
        
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())

                // 추가
                .eventStartAt(request.getEventStartAt())
                .eventEndAt(request.getEventEndAt())
                .registrationStartAt(request.getRegistrationStartAt())
                .registrationEndAt(request.getRegistrationEndAt())
                .capacity(request.getCapacity())

                .instructorId(instructorId)
                .build();

        return CourseDto.CourseResponse.from(courseRepository.save(course));
    }

    /**
     * 강의 단건 조회
     */
    public CourseDto.CourseResponse getCourse(Long id) {
        Course course = findCourseById(id);
        return CourseDto.CourseResponse.from(course);
    }

    /**
     * 전체 활성 강의 목록 조회
     */
    public List<CourseDto.CourseResponse> getAllCourses() {
        return courseRepository.findByStatus(Course.Status.ACTIVE).stream()
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 강의 조회
     */
    public List<CourseDto.CourseResponse> getCoursesByCategory(Course.Category category) {
        return courseRepository.findByCategoryAndStatus(category, Course.Status.ACTIVE).stream()
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 강의 존재 여부 확인 (Enrollment Service → Course Service REST 호출용)
     */
    public boolean existsCourse(Long id) {
        return courseRepository.existsById(id);
    }

    /**
     * course.increaseEnrollmentCount()를 실행하면 작성한 코드에 의해 접수 기간과 정원이 검사됨
     */
    @Transactional
    public void increaseEnrollmentCount(Long courseId) {
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "행사를 찾을 수 없습니다: " + courseId
                        )
                );

        course.increaseEnrollmentCount();
    }

    /**
     * 추천 서비스용: 카테고리별 미수강 강의 조회
     * - excludeCourseIds: 이미 수강한 강의 ID 목록
     */
    public List<CourseDto.CourseResponse> getRecommendCourses(
            Course.Category category, List<Long> excludeCourseIds) {

        List<Course> courses = excludeCourseIds.isEmpty()
                ? courseRepository.findByCategoryAndStatus(category, Course.Status.ACTIVE)
                : courseRepository.findByCategoryAndStatusAndIdNotIn(
                        category, Course.Status.ACTIVE, excludeCourseIds);

        // 수강생 수 기준 내림차순 정렬
        return courses.stream()
                .sorted((a, b) -> b.getEnrollmentCount() - a.getEnrollmentCount())
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다: " + id));
    }
}
