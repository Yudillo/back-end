# Yudillo API

> Trello 스타일 칸반보드 **Yudillo** 의 백엔드. [front-end-develop](../front-end-develop) 의 React 클라이언트에 REST API 를 제공한다.

기존 프론트엔드는 Supabase 의 Auth 와 DB 를 클라이언트에서 직접 호출하고 있었다.
이 프로젝트는 그 역할을 전부 Spring Boot 로 옮겨, 인증부터 데이터 접근까지 서버가 책임지는 구조로 만든다.

</br>

## ⚙️ 기술스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.1.1, Spring MVC, Spring Security 7 |
| Persistence | Spring Data JPA (Hibernate), Flyway |
| Database | PostgreSQL 17 (테스트는 H2) |
| Auth | 자체 발급 JWT (Access / Refresh), BCrypt |
| Docs | springdoc-openapi (Swagger UI) |
| Build | Gradle 9.7.1 (wrapper 포함) |

</br>

## 🪄 설치 및 실행

### 💡 실행환경

```
java -version
openjdk version "17.0.14"
```

Gradle 은 따로 설치하지 않아도 된다. 저장소에 포함된 wrapper(`./gradlew`)가 알맞은 버전을 내려받는다.

### 🎈 1. DB 준비

DB 서버를 별도로 운영한다면 접속정보만 환경변수로 맞추면 된다. 로컬에서 간단히 띄우려면:

```bash
docker compose up -d
```

### 🎈 2. 환경변수 설정

```bash
cp .env.example .env
```

| 변수 | 설명 |
| --- | --- |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | DB 접속정보 |
| `JWT_SECRET` | HS256 대칭키. **최소 32바이트** 랜덤 문자열 (`openssl rand -base64 48`) |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트엔드 오리진 (쉼표로 여러 개) |
| `SPRING_PROFILES_ACTIVE` | `local` 또는 `prod` |

`local` 프로필에는 개발 편의를 위한 기본값이 들어있어 환경변수 없이도 기동된다. **운영에서는 반드시 직접 주입한다.**

### 🎈 3. 실행

```bash
./gradlew bootRun
```

```bash
# Windows
gradlew.bat bootRun
```

| 주소 | 설명 |
| --- | --- |
| http://localhost:${port} | API 서버 |
| http://localhost:${port}/swagger-ui.html | Swagger UI |
| http://localhost:${port}/v3/api-docs | OpenAPI 스펙 (JSON) |

### 🎈 4. 테스트

```bash
./gradlew test
```

</br>

## 🔐 인증 흐름

1. `POST /api/auth/signup` — 회원가입. 비밀번호는 BCrypt 로 해싱해 저장한다.
2. `POST /api/auth/login` — `accessToken`(30분) + `refreshToken`(14일) 발급. Refresh 토큰은 DB 에도 저장해 서버가 폐기할 수 있게 한다.
3. 이후 모든 요청은 `Authorization: Bearer {accessToken}` 헤더를 붙인다.
4. Access 토큰이 만료되면 `POST /api/auth/reissue` 로 재발급한다. 이때 Refresh 토큰도 함께 교체된다(rotation).
5. `POST /api/auth/logout` — 저장된 Refresh 토큰을 폐기한다.

Swagger UI 우측 상단 **Authorize** 에 `accessToken` 을 넣으면 보호된 API 도 문서에서 바로 호출할 수 있다.

## 📁 폴더구조

도메인별로 패키지를 나누고, 공통 관심사는 `global` 에 모았다.

```
src/main/java/com/yudillo/api/
│
├── global/                   # 도메인에 속하지 않는 공통 관심사
│   ├── config/               # SecurityConfig, SwaggerConfig, CORS 설정
│   ├── security/             # JWT 발급·검증, 인증 필터, 인증 주체(AuthUser)
│   ├── entity/               # BaseTimeEntity (생성/수정 시각 공통)
│   ├── response/             # ApiResponse, PageResponse 공통 응답 포맷
│   └── exception/            # ErrorCode, BusinessException, GlobalExceptionHandler
│
├── auth/                     # 회원가입, 로그인, 토큰 재발급/폐기
├── user/                     # 내 정보 조회, 닉네임·비밀번호 변경
└── ApiApplication.java

src/main/resources/
├── application.yml           # 공통 설정
├── application-local.yml     # 로컬 개발
├── application-prod.yml      # 운영 (API 문서 비활성화)
└── db/migration/             # Flyway 마이그레이션
```

각 도메인 패키지는 `controller` / `service` / `repository` / `entity` / `dto` 로 구성한다.
서비스는 인터페이스 없이 클래스 하나로 두고, DTO 는 `record` 로 작성한다.

</br>

## 🔎 API 요약

전체 스펙은 Swagger UI 에서 확인할 수 있다.

### Auth

| Method | Path | 인증 | 설명 |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | - | 회원가입 |
| POST | `/api/auth/login` | - | 로그인 (토큰 발급) |
| POST | `/api/auth/reissue` | - | 토큰 재발급 |
| POST | `/api/auth/logout` | ✅ | 로그아웃 |

### User

| Method | Path | 인증 | 설명 |
| --- | --- | --- | --- |
| GET | `/api/users/me` | ✅ | 내 정보 조회 |
| PATCH | `/api/users/me/name` | ✅ | 사용자 이름 변경 |
| PATCH | `/api/users/me/password` | ✅ | 비밀번호 변경 |

</br>

## 📐 응답 포맷

에러 코드는 [`ErrorCode`](src/main/java/com/yudillo/api/global/exception/ErrorCode.java) 에서 관리한다.
성공 메시지는 [`SuccessMessage`](src/main/java/com/yudillo/api/global/response/SuccessMessage.java) 에서 관리한다.

</br>

## 🌱 남은 작업

현재 범위는 **인증과 사용자**까지다. 프론트엔드에 구현된 화면(로그인/회원가입/비밀번호 변경)에
대응하는 API 만 두고, 칸반보드 도메인은 프론트 화면이 생길 때 함께 추가한다.

- [ ] 이메일 인증 / 비밀번호 재설정 — Supabase 가 대신 보내주던 메일. SMTP 설정 + 인증 토큰 테이블 필요
- [ ] 프론트엔드 마이그레이션 — `src/supabase/` 를 이 API 호출로 교체
- [ ] 테스트 — 컨텍스트 로딩 확인만 있다. 인증 흐름 테스트 추가
- [ ] 프로젝트 / 리스트 / 카드 / 활동 로그 — 프론트 화면 작업 후
- [ ] OAuth, 실시간 협업
