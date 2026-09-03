# Docker 빌드 429 오류 해결

## 문제

Docker 빌드 중 Maven Central에서 `429 Too Many Requests`가 발생해 Gradle 의존성을 내려받지 못했다.

## 문제 분석

- 현재 공인 IP가 Maven Central 요청 제한에 걸렸다.
- 각 서비스가 빌드될 때마다 Gradle과 의존성을 반복 다운로드해 요청량이 증가했다.
- 프로젝트는 Maven이 아닌 Gradle 기반이므로 `mvn`과 `settings-google.xml`은 적용되지 않는다.

## 해결 방법

- Gradle 저장소를 Google Maven Central 미러로 변경했다.
- 불필요한 `gradlew dependencies` 단계를 제거했다.
- 5개 Java 서비스가 동일한 Docker Gradle 캐시를 공유하도록 설정했다.

## 결과

`event-service`의 컴파일과 Docker 이미지 빌드가 정상 완료되었다.

```text
BUILD SUCCESSFUL
```

docker compose up -d --no-build --pull never
4. 접수 기간 및 정원 제한 미구현
- 문제: 접수 전·종료 후에도 신청할 수 있고, 최대 신청 인원을 초과할 수 있었음
- 해결: Event.increaseReservationCount()에 다음 검증 추가
  - 접수 시작 전 신청 제한
  - 접수 종료 후 신청 제한
  - 최대 신청 인원 초과 제한
  - 정상 상태일 때만 신청 인원 증가
5. 동시 신청 시 정원 초과 가능성
- 문제: 여러 사용자가 동시에 마지막 자리를 신청하면 정원을 초과할 가능성이 있음
- 해결: EventRepository에 비관적 잠금(PESSIMISTIC_WRITE) 적용
6. 서비스 간 오류가 무시되는 문제
- 문제: Event Service에서 정원 초과 오류가 나도 Reservation Service가 로그만 남기고 신청을 계속할 수 있었음
- 해결: EventServiceClient에서 Event Service의 오류 메시지를 받아 예외로 다시 전달하도록 수정
7. 신청 인원 중복 증가 문제
- 문제: 신청 시점과 결제 완료 시점에 모두 신청 인원을 증가시키면 인원이 두 번 증가함
- 해결:
  - enroll()에서 접수 기간·정원 검사 및 신청 인원 증가
  - activateReservation()의 중복 인원 증가 호출 제거
8. 예외 처리 코드 작성 오류
- 문제: 입력값 검증 예외와 접수 상태 예외를 하나의 메서드에 작성하려 했음
- 해결:
  - MethodArgumentNotValidException은 400 Bad Request
  - IllegalStateException은 409 Conflict
  - 각각 별도 @ExceptionHandler 메서드로 분리
9. Java 파일에 Markdown 문법 입력
- 문제: Java 파일 안에 코드 블록 표시인 ```kotlin을 넣을 가능성이 있었음
- 해결: Java 파일에는 Markdown 백틱을 넣지 않고 Java 코드만 작성
10. GitHub 업로드 용량 및 보안 문제
- 문제: Docker 이미지 파일이 약 2.8GB이고, build, bin, .env 파일도 포함되어 있었음
- 해결: .gitignore에 다음 항목을 추가해 업로드에서 제외
  - Docker 이미지 압축 파일
  - build/, bin/, node_modules/
  - .env
  - 임시 파일 및 로컬 설정 파일