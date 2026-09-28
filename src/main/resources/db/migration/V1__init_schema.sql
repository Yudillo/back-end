-- Yudillo 초기 스키마 (PostgreSQL)

create table users
(
    id             uuid         not null,
    email          varchar(255) not null,
    password       varchar(100) not null,
    name           varchar(20)  not null,
    role           varchar(20)  not null,
    email_verified boolean      not null default false,
    created_at     timestamptz  not null,
    updated_at     timestamptz  not null,
    constraint pk_users primary key (id),
    constraint uk_users_email unique (email)
);

create table refresh_tokens
(
    id         bigserial,
    user_id    uuid         not null,
    token      varchar(512) not null,
    expires_at timestamptz  not null,
    created_at timestamptz  not null,
    constraint pk_refresh_tokens primary key (id),
    constraint uk_refresh_tokens_token unique (token),
    constraint fk_refresh_tokens_user foreign key (user_id) references users (id) on delete cascade
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
