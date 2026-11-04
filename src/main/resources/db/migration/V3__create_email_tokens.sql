-- 메일로 보내는 일회용 토큰. 비밀번호 재설정과 회원가입 이메일 인증이 함께 쓴다.

create table email_tokens
(
    id         bigserial,
    user_id    uuid         not null,
    token      varchar(255) not null,
    purpose    varchar(20)  not null,
    expires_at timestamptz  not null,
    used_at    timestamptz,
    created_at timestamptz  not null,
    constraint pk_email_tokens primary key (id),
    constraint uk_email_tokens_token unique (token),
    constraint fk_email_tokens_user foreign key (user_id) references users (id) on delete cascade
);

-- 새 토큰을 발급할 때 같은 사용자/용도의 이전 토큰을 찾아 무효화하는 데 쓴다.
create index idx_email_tokens_user_id_purpose on email_tokens (user_id, purpose);

-- ─── email_tokens ───────────────────────────────────────────────────
comment on table email_tokens is '메일 발송용 일회용 토큰. 비밀번호 재설정 / 이메일 인증';
comment on column email_tokens.id is '토큰 IDX';
comment on column email_tokens.user_id is '사용자 IDX';
comment on column email_tokens.token is '메일 링크에 담기는 랜덤 문자열(SecureRandom, Base64URL)';
comment on column email_tokens.purpose is '토큰 용도 PASSWORD_RESET, EMAIL_VERIFY';
comment on column email_tokens.expires_at is '만료 시각(UTC)';
comment on column email_tokens.used_at is '사용 시각(UTC). null 이면 미사용. 한 번 쓰면 재사용 불가';
comment on column email_tokens.created_at is '발급 시각(UTC)';
