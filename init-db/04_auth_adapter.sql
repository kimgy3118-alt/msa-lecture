-- 갈래 서비스 DB는 STANDARD / ADMIN 역할값을 유지한다.
-- 제공된 레거시 인증 서버만 STUDENT / INSTRUCTOR enum을 요구하므로,
-- 인증 전용 DB에 호환 계정을 동기화한다. 사용자 ID는 동일하게 보존한다.
CREATE DATABASE IF NOT EXISTS auth_platform_db;
GRANT ALL PRIVILEGES ON auth_platform_db.* TO 'manager'@'%';

CREATE TABLE IF NOT EXISTS auth_platform_db.users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO auth_platform_db.users (id, email, password, name, role, created_at, updated_at)
SELECT id, email, password, name,
       CASE role WHEN 'ADMIN' THEN 'INSTRUCTOR' ELSE 'STUDENT' END,
       created_at, updated_at
FROM event_platform_db.users
ON DUPLICATE KEY UPDATE
    email = VALUES(email), password = VALUES(password), name = VALUES(name),
    role = VALUES(role), updated_at = VALUES(updated_at);

DROP TRIGGER IF EXISTS event_platform_db.sync_auth_user_after_insert;
CREATE TRIGGER event_platform_db.sync_auth_user_after_insert
AFTER INSERT ON event_platform_db.users
FOR EACH ROW
INSERT INTO auth_platform_db.users (id, email, password, name, role, created_at, updated_at)
VALUES (NEW.id, NEW.email, NEW.password, NEW.name,
        CASE NEW.role WHEN 'ADMIN' THEN 'INSTRUCTOR' ELSE 'STUDENT' END,
        NEW.created_at, NEW.updated_at);

DROP TRIGGER IF EXISTS event_platform_db.sync_auth_user_after_update;
CREATE TRIGGER event_platform_db.sync_auth_user_after_update
AFTER UPDATE ON event_platform_db.users
FOR EACH ROW
UPDATE auth_platform_db.users
SET email = NEW.email, password = NEW.password, name = NEW.name,
    role = CASE NEW.role WHEN 'ADMIN' THEN 'INSTRUCTOR' ELSE 'STUDENT' END,
    updated_at = NEW.updated_at
WHERE id = NEW.id;
