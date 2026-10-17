# 관리자 API 계약

관리자 대시보드(`daytune_admin`)가 붙는 서버 API의 단일 원천이다. 엔드포인트를 추가·변경하면 같은 변경에서 이 문서를 고친다.
표는 Jackson 직렬화 결과 기준이다.

## 공통

- 경로: `/admin/**` (하위 prefix 없음)
- 권한: `POST /admin/auth/login`만 permitAll, 나머지는 `ROLE_ADMIN`
- 인증: `Authorization: Bearer {accessToken}` (로그인 응답의 토큰, 만료 7일, 리프레시 없음 → 만료 시 재로그인)
- 응답: 항상 `ApiResponse<T>`
  - 성공 `{ "status": "SUCCESS", "message": string, "data": T }`
  - 실패 `{ "status": "ERROR", "message": string }` (`data` 없음)
- 날짜 `yyyy-MM-dd`, 시각 `yyyy-MM-ddTHH:mm:ss` — 오프셋 없음, **Asia/Seoul** 기준
- 기간 파라미터 `from`·`to`: `yyyy-MM-dd`, 필수, **양 끝 포함**, 최대 92일. 역순·초과는 400
- 일별 시리즈(`daily[]`)는 from~to 모든 날짜를 포함하고 빈 날짜는 0으로 채운다 (날짜 오름차순)
- 금액: USD, `number`, 소수 6자리
- 제공사(provider) enum: `OPENAI` | `SUNO` | `AWS_S3` | `FIREBASE` (목록 응답에서 이 순서)
- 기능(operation) enum과 제공사 (목록 응답에서 이 순서)

| operation | provider | 비고 |
|---|---|---|
| FOLLOW_UP_QUESTION | OPENAI | 후속 질문·일기 요약 |
| EMOTION_INSIGHT | OPENAI | 감정 분석 |
| LYRICS_GENERATE | OPENAI | 가사 생성 |
| MUSIC_GENERATE | SUNO | 음악 생성 요청 |
| MUSIC_STATUS_POLL | SUNO | 생성 상태 조회 (과금 단위 아님, 생성 건수와 별개) |
| S3_UPLOAD | AWS_S3 | 공유 페이지 등 업로드 |
| STORAGE_UPLOAD | FIREBASE | Storage 업로드 |
| PUSH_SEND | FIREBASE | FCM 푸시 발송 |

### 오류 상태

| 상태 | 본문 | 언제 |
|---|---|---|
| 400 | ApiResponse 오류 | 파라미터 누락·형식 오류·범위 위반 |
| 401 | ApiResponse 오류 | 로그인 실패 |
| 401 | `{"status": "ERROR", "message": "Access Token 값이 유효하지 않습니다."}` | 토큰 없음·만료·위조 (앱 API와 같은 본문) |
| 403 | `{"status":"ERROR","message":"권한이 없습니다."}` | ROLE_ADMIN이 아닌 토큰 |
| 404 | ApiResponse 오류 | 없는 유저 |
| 429 | ApiResponse 오류 | 로그인 잠금 중 |
| 400 / 415 | **Boot 기본 오류 JSON** (`{timestamp, status, error, path}`, `status`가 숫자) | 요청 본문 JSON 파싱 실패(400), `Content-Type` 누락·불일치(415) — `POST /admin/auth/login` 해당 |
| 500 | **Boot 기본 오류 JSON** (`{timestamp, status: 500, error, path}`, `status`가 숫자) | 처리되지 않은 서버 오류 |

프론트는 HTTP 상태 코드로 분기하고, 본문은 `status === "ERROR"`일 때만 `message`를 표시한다.

### CORS

- `/admin/**`에만 적용. 허용 오리진은 설정값 `admin.cors.allowed-origins` (local: `http://localhost:5173`, prod: 미정 → 비어 있으면 전부 거부)
- 메서드 `GET`·`POST`·`OPTIONS`, 헤더 `Authorization`·`Content-Type`, credentials 미사용

---

## POST /admin/auth/login

- 권한: permitAll
- **`Authorization` 헤더를 붙이지 않는다.** 만료·위조 토큰이 붙어 있으면 JWT 필터가 먼저 401 토큰 본문을 반환한다
- 요청 본문

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| email | string | N | 공백 불가 |
| password | string | N | 공백 불가 |

- 응답: `ApiResponse<AdminLoginResponse>`

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| accessToken | string | N | JWT (ROLE_ADMIN, 7일) |

- 실패
  - 401 `"이메일 또는 비밀번호가 올바르지 않습니다."` — 이메일 없음·비밀번호 불일치·비관리자·탈퇴 계정 구분 없음
  - 429 `"로그인 시도가 너무 많습니다. 잠시 후 다시 시도하세요."` — 같은 이메일(대소문자 무시)로 15분 내 5회 실패하면 마지막 실패부터 15분 잠금. 잠금 중엔 비밀번호를 확인하지 않는다. 성공 시 초기화. 서버 재시작 시 초기화(인메모리)
  - 400 — email·password 공백

## GET /admin/usage — 이용 현황

- 파라미터: `from`, `to` (필수)
- 응답: `ApiResponse<AdminUsage>`

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| totalUsers | number | N | 조회 시각 기준 누적 유저 수, 탈퇴 제외 (역할 무관) |
| totals.signups | number | N | 기간 내 가입 수 (이후 탈퇴 포함) |
| totals.activeUsers | number | N | 기간 내 일기를 쓴 유저 수 (기간 전체 중복 제거 — daily 합과 다를 수 있음) |
| totals.diaries | number | N | 기간 내 일기 수 |
| totals.musics | number | N | 기간 내 생성된 음악 행 수 (생성 1회 = 2행, 선택 후 1행만 남음) |
| totals.shares | number | N | 기간 내 공유 URL 생성 수 |
| totals.pushes | number | N | 기간 내 푸시 발송 성공 수 (미터링 기준 → 미터링 배포 이후부터만 집계) |
| daily[].date | string | N | yyyy-MM-dd |
| daily[].signups | number | N | |
| daily[].activeUsers | number | N | 그 날 일기를 쓴 유저 수 |
| daily[].diaries | number | N | |
| daily[].musics | number | N | |
| daily[].shares | number | N | |
| daily[].pushes | number | N | |

## GET /admin/costs — 외부 API 과금

- 파라미터: `from`, `to` (필수)
- 응답: `ApiResponse<AdminCosts>`. 모든 값은 미터링 로그(배포 이후 호출) 기준
- 비용은 **OPENAI만 추정값**(호출 시점 토큰 × 단가). 나머지 제공사는 산정하지 않아 `null`. 추정·실제 합산 금지
- OPENAI 비용은 성공 호출만 포함. 단가표에 없는 모델의 호출은 비용 0으로 들어간다 (서버 WARN 로그)

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| asOf | string | N | 집계 시각 |
| providers[] | | | 제공사 4개 항상 포함 (호출 0건도) |
| providers[].provider | string | N | provider enum |
| providers[].calls | number | N | 호출 수 (실패 포함) |
| providers[].failures | number | N | 실패 수 |
| providers[].unitsIn | number | Y | 입력 토큰 합. OPENAI만 값(0 이상), 나머지 null |
| providers[].unitsOut | number | Y | 출력 토큰 합. OPENAI만 값(0 이상), 나머지 null |
| providers[].estimatedCostUsd | number | Y | OPENAI만 값(0 이상), 나머지 null |
| providers[].source | string | N | `ESTIMATED` (OPENAI) / `NONE` (비용 미산정) |
| daily[] | | | from~to 모든 날짜 × 제공사 4개 (날짜 오름차순, 날짜 안에서 provider enum 순) |
| daily[].date | string | N | yyyy-MM-dd |
| daily[].provider | string | N | |
| daily[].calls | number | N | 빈 칸 0 |
| daily[].estimatedCostUsd | number | Y | OPENAI만 값(빈 칸 0), 나머지 null |
| topOperations[] | | | 호출이 있는 기능만, 비용 내림차순 → 호출 수 내림차순 상위 10 (비용 null은 맨 뒤) |
| topOperations[].provider | string | N | |
| topOperations[].operation | string | N | operation enum |
| topOperations[].calls | number | N | |
| topOperations[].estimatedCostUsd | number | Y | OPENAI 기능만 값, 나머지 null |
| topUsers[] | | | user_id가 있는 호출만 (배치 경로 제외), 비용 내림차순(null은 0 취급) → 호출 수 내림차순 상위 10 |
| topUsers[].userId | number | N | |
| topUsers[].nickname | string | Y | 유저가 삭제됐거나 닉네임이 없으면 null |
| topUsers[].calls | number | N | 모든 제공사 합 |
| topUsers[].estimatedCostUsd | number | Y | OPENAI 비용 합. 비용 산정된 호출이 없으면 null |

## GET /admin/users — 유저 검색

- 파라미터
  - `query` (선택): 숫자면 id 일치 OR 이메일·닉네임 부분 일치(대소문자 무시). 비면 전체
  - `page` (선택, 기본 0): 0 이상
  - `size` (선택, 기본 20): 1~50
  - 범위 위반은 400
- 정렬: 가입일 내림차순 → id 내림차순. 탈퇴 유저 포함
- 응답: `ApiResponse<AdminUserPage>`

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| content[] | UserSummary[] | N | |
| page | number | N | 요청한 page |
| size | number | N | 요청한 size |
| totalElements | number | N | 전체 일치 수 |

UserSummary

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| id | number | N | |
| nickname | string | Y | |
| email | string | Y | |
| role | string | N | `ROLE_USER` \| `ROLE_ADMIN` |
| createdDate | string | N | 가입 시각 |
| lastLoginDate | string | Y | |
| deletedDate | string | Y | 탈퇴 시각 |

## GET /admin/users/{userId} — 유저 상세

- 파라미터: `from`, `to` (필수, `costs[]`에만 적용)
- 없는 유저: 404 `"유저를 찾을 수 없습니다."`
- 응답: `ApiResponse<AdminUserDetail>`

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| id, nickname, email, role, createdDate, lastLoginDate, deletedDate | | | UserSummary와 동일 |
| counts.diaries | number | N | 전체 기간 |
| counts.musics | number | N | 전체 기간 (그 유저 일기의 음악 행) |
| counts.shares | number | N | 전체 기간 |
| recentActivity[] | | | 일기·음악 합쳐 최신 20건 (생성 시각 내림차순) |
| recentActivity[].type | string | N | `DIARY` \| `MUSIC` |
| recentActivity[].id | number | N | 일기 id 또는 음악 id |
| recentActivity[].createdDate | string | N | |
| costs[] | | | 기간 내, 제공사 4개 항상 포함 |
| costs[].provider | string | N | |
| costs[].calls | number | N | |
| costs[].estimatedCostUsd | number | Y | OPENAI만 값(0 이상), 나머지 null |

## GET /admin/system — 시스템 상태

- 파라미터: `hours` (선택, 기본 24, 1~168). 범위 위반은 400
- 응답: `ApiResponse<AdminSystem>`

| 필드 | 타입 | null | 비고 |
|---|---|---|---|
| asOf | string | N | 집계 시각 |
| windowFrom | string | N | asOf − hours |
| apis[] | | | 기능 8개 항상 포함 (operation enum 순) |
| apis[].provider | string | N | |
| apis[].operation | string | N | |
| apis[].calls | number | N | 창 내 호출 수 |
| apis[].failures | number | N | |
| apis[].failureRate | number | N | 0~1, 소수 4자리. 호출 0건이면 0 |
| apis[].p95LatencyMs | number | Y | nearest-rank p95 (ms). 호출 0건이면 null |
| batchRuns[] | | | Spring Batch 실행 이력 최신 10건 (생성 시각 내림차순). 조회 실패 시 빈 배열 |
| batchRuns[].jobName | string | N | 예: `updateMusicFileUrlsJob` |
| batchRuns[].status | string | N | Spring Batch `BatchStatus` (COMPLETED, FAILED, STARTED 등) |
| batchRuns[].startTime | string | N | 시작 시각 (시작 전이면 생성 시각) |
| batchRuns[].endTime | string | Y | 실행 중이면 null |
| batchRuns[].exitCode | string | N | 예: COMPLETED, FAILED, UNKNOWN |
| recentErrors[] | | | 창 내 실패 호출 최신 20건 |
| recentErrors[].createdDate | string | N | |
| recentErrors[].provider | string | N | |
| recentErrors[].operation | string | N | |
| recentErrors[].errorCode | string | Y | `HTTP_{상태}` \| `SUNO_{응답 code}` \| `SUNO_EMPTY_BODY` \| 예외 클래스명(예: `SocketTimeoutException`). 오류 메시지 원문은 저장·노출하지 않는다 |
