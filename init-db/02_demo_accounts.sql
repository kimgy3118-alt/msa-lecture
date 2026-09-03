-- 갈래 서비스의 역할값: STANDARD(일반 참여자) / ADMIN(기관 관리자)
INSERT INTO users (email, password, name, role, created_at, updated_at)
VALUES
  ('owner@test.com', '$2a$10$R0tq9nnQBrTXbIH.x0HGFOitkaDQBwHET/bh/RsHy4ZrOymiorZZu', '홀랑이', 'STANDARD', NOW(6), NOW(6)),
  ('owner2@test.com', '$2a$10$R0tq9nnQBrTXbIH.x0HGFOitkaDQBwHET/bh/RsHy4ZrOymiorZZu', '홀랑홀랑이', 'ADMIN', NOW(6), NOW(6));
