-- 서버 시작 시 어드민 계정 자동 생성 (없을 때만)
INSERT INTO users (email, password, nickname, role, status, email_verified, created_at, updated_at)
SELECT 'admin@market.com',
       '$2a$10$993OiNZqLYcuh3NM81uTYOnexBHHZwpY8R1Xl8Okprw1pqx7IqVQy',  -- "admin1234"
       '관리자',
       'ADMIN',
       'ACTIVE',
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@market.com'
);