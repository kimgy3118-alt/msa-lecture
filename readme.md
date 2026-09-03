# 갈래 — 전국 행사 예약 플랫폼

전국의 축제·전시·공연·문화 체험 행사를 한곳에서 탐색하고 예약할 수 있는 B2G2C 플랫폼입니다. 기관 담당자는 행사를 등록·운영하고, 일반 사용자는 행사 정보를 확인한 뒤 무료 예약 또는 결제를 진행할 수 있습니다.

## 주요 기능

- 행사 탐색: 카테고리와 지역을 기준으로 전국 행사를 조회합니다.
- 행사 운영: 기관 담당자가 행사 사진·장소를 포함한 행사 정보를 등록·수정합니다.
- 예약: 자유 방문형, 무료 예약형, 유료 예약형을 지원합니다.
- 결제: 유료 행사 결제 상태와 결제 내역을 조회합니다.
- 추천: 예약 이력 기반 맞춤 행사와 신규 사용자 인기 행사를 추천합니다.
- 인증: OAuth2 기반 로그인과 역할별 화면을 제공합니다.

## 서비스 구성

| 구성 | 포트 | 역할 |
| --- | ---: | --- |
| Vue 3 프런트엔드 | 3000 | 사용자 화면 |
| API Gateway | 8080 | API 진입점·인증 연동 |
| Auth Server | 9000 | OAuth2 인증 서버 |
| Eureka | 8761 | 서비스 디스커버리 |
| User Service | 8081 | 회원·역할 관리 |
| Event Service | 8082 | 행사 등록·조회 |
| Reservation Service | 8083 | 예약 및 정원 처리 |
| Payment Service | 8084 | 결제 처리 |
| Recommend Service | 8085 | 행사 추천 |
| MariaDB | 3379 | 서비스 데이터베이스 |
| Kafka | 9092 | 예약·결제 이벤트 처리 |

## 실행 전 준비

- Docker Desktop 실행
- Node.js 20 이상 권장
- 프로젝트 최상위 폴더에서 명령 실행

```bash
cd /Users/younnii/Documents/Github/msa-lecture
```

## 실행 방법

### 1. 백엔드·인프라 실행

```bash
docker compose -f docker-compose.build.yml up -d --build
```

컨테이너 상태를 확인합니다.

```bash
docker compose -f docker-compose.build.yml ps
```

`event-platform-eureka`, `event-platform-auth`, `event-db`, `event-platform-kafka` 등이 `running` 또는 `healthy`가 되면 정상입니다. 최초 실행은 이미지 빌드와 서비스 등록 때문에 몇 분 걸릴 수 있습니다.

### 2. 프런트엔드 실행

새 터미널을 열어 아래를 실행합니다.

```bash
cd /Users/younnii/Documents/Github/msa-lecture/vue-frontend
npm install
npm run dev -- --host localhost
```

브라우저에서 [http://localhost:3000](http://localhost:3000)에 접속합니다.

서비스 등록 현황은 [http://localhost:8761](http://localhost:8761)에서 확인할 수 있습니다.

### 3. 샘플 행사 데이터 불러오기 (선택)

초기 DB에는 테이블만 생성됩니다. 목록에 예시 행사와 데모 계정을 넣으려면 백엔드가 실행된 뒤 프로젝트 최상위 폴더에서 아래 명령을 실행합니다.

```bash
docker exec -i event-db mariadb -umanager -pSqlDba-1 event_platform_db < mock_data/00_seed_combined.sql
```

> 이 시드 파일은 기존 사용자·행사·예약·결제 데이터를 비운 뒤 샘플 데이터로 교체합니다. 이미 등록한 데이터가 있다면 실행하지 마세요.

## 인증 이미지 관련 안내

`auth-server`, `api-gateway`는 사전 빌드 이미지를 사용합니다. 새 환경에서 아래처럼 `pull access denied` 오류가 날 경우, 수업 배포본의 `infra-images.tar`를 먼저 로드해야 합니다.

```bash
docker load -i /infra-images.tar의/실제/경로/infra-images.tar
docker compose -f docker-compose.build.yml up -d --build
```

tar 파일을 실행하는 것이 아니라 `docker load -i`로 불러와야 합니다.

## 데모 계정

| 구분 | 이메일 | 비밀번호 |
| --- | --- | --- |
| 일반 사용자 | `owner@test.com` | `password1234` |

## 종료와 로그 확인

전체 서비스 종료:

```bash
cd /Users/younnii/Documents/Github/msa-lecture
docker compose -f docker-compose.build.yml down
```

전체 로그 확인:

```bash
docker compose -f docker-compose.build.yml logs -f
```

특정 서비스 로그 확인 예시:

```bash
docker compose -f docker-compose.build.yml logs -f event-service
```

데이터베이스까지 초기화해야 할 때만 아래 명령을 사용합니다. 실행하면 기존 행사·예약·결제 데이터가 삭제됩니다.

```bash
docker compose -f docker-compose.build.yml down -v
```

## 행사 유형

| 유형 | 예약 | 결제 |
| --- | --- | --- |
| 자유 방문형 | 필요 없음 | 필요 없음 |
| 무료 예약형 | 필요 | 필요 없음 |
| 유료 예약형 | 필요 | 필요 |
