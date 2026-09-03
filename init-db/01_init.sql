-- 전국 행사 예약 플랫폼 초기 DDL
-- Spring JPA ddl-auto: update 로도 생성되지만
-- 명시적 DDL로 테이블 선후 관계를 문서화

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    role        VARCHAR(20)     NOT NULL COMMENT 'STANDARD | ADMIN',
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 기관 담당자가 행사 등록 (organizer_id → users.id)
CREATE TABLE IF NOT EXISTS events (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    title            VARCHAR(255)    NOT NULL,
    description      TEXT,
    category         VARCHAR(50)     NOT NULL COMMENT 'FESTIVAL|EXHIBITION|PERFORMANCE|OTHER',
    event_type       VARCHAR(30)     NOT NULL COMMENT 'FREE_VISIT|FREE_RESERVATION|PAID_RESERVATION',
    venue            VARCHAR(255)    NOT NULL,
    organizer_name   VARCHAR(255)    NOT NULL,
    image_url        LONGTEXT,
    price            DECIMAL(10,2)   NOT NULL,
    
    event_start_at        DATETIME(6)     NOT NULL,
    event_end_at          DATETIME(6)     NOT NULL,
    registration_start_at DATETIME(6)     NOT NULL,
    registration_end_at   DATETIME(6)     NOT NULL,
    capacity              INT             NOT NULL DEFAULT 100,

    organizer_id     BIGINT          NOT NULL,
    reservation_count INT             NOT NULL DEFAULT 0,
    status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE | INACTIVE',
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    PRIMARY KEY (id),
    FOREIGN KEY (organizer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 사용자가 행사 예약 (user_id → users.id, event_id → events.id)
CREATE TABLE IF NOT EXISTS reservations (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    event_id   BIGINT      NOT NULL,
    status      VARCHAR(30) NOT NULL DEFAULT 'PAYMENT_PENDING' COMMENT 'PAYMENT_PENDING | CONFIRMED | CANCELLED',
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_event (user_id, event_id),
    FOREIGN KEY (user_id)   REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 유료 행사 예약 결제 (user_id → users.id, event_id → events.id)
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    event_id       BIGINT          NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | COMPLETED | FAILED | CANCELLED',
    transaction_id  VARCHAR(255)    UNIQUE,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id)   REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
