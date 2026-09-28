-- 테이블/컬럼 주석. DB 도구(HeidiSQL, DBeaver 등)에서 스키마만 봐도 파악되도록 한다.
-- 데이터와 구조를 바꾸지 않는 메타데이터 전용 마이그레이션이다.

-- ─── users ───────────────────────────────────────────────────────────
comment on table users is '사용자 정보';
comment on column users.id is '사용자 IDX';
comment on column users.email is '사용자 이메일';
comment on column users.password is '사용자 패스워드 BCrypt 해시(60자)';
comment on column users.name is '사용자 이름. 한글/영문/숫자 2~10자. 중복 허용(식별자는 email)';
comment on column users.role is '서비스 전역 권한 USER, ADMIN / 확정되면 ENUM으로 수정';
comment on column users.email_verified is '이메일 인증 완료 여부. 인증 메일 발송 미구현 상태라 현재는 가입 시 true 로 저장';
comment on column users.created_at is '생성 시각(UTC)';
comment on column users.updated_at is '수정 시각(UTC)';

-- ─── refresh_tokens ─────────────────────────────────────────────────
comment on table refresh_tokens is 'Refresh 토큰 보관. JWT 는 발급 후 취소가 불가능하므로, 폐기를 위해 DB 에 저장';
comment on column refresh_tokens.id is '토큰 IDX';
comment on column refresh_tokens.user_id is '사용자 IDX';
comment on column refresh_tokens.token is '발급된 Refresh 토큰 원문. 재발급 시 rotation 으로 교체된다';
comment on column refresh_tokens.expires_at is '만료 시각(UTC)';
comment on column refresh_tokens.created_at is '발급 시각(UTC)';
