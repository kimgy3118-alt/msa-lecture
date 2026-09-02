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

`course-service`의 컴파일과 Docker 이미지 빌드가 정상 완료되었다.

```text
BUILD SUCCESSFUL
```
