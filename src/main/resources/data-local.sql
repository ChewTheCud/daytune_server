-- 로컬 개발 서버(local 프로필) 샘플 데이터
-- - application-local.yml에서만 로드된다 (H2 인메모리, create-drop). 운영 DB에 실행하지 않는다.
-- - 모든 개인정보 형태 값은 가짜다. 이메일 도메인은 example.com만 쓴다.
-- - 날짜는 전부 "실행 시점 기준 상대 날짜"다. 언제 띄워도 최근 30일 데이터가 보인다.
-- - id를 명시해 넣고, 마지막에 IDENTITY를 1000부터 다시 시작시켜 앱의 신규 INSERT와 충돌하지 않게 한다.

-- 프롬프트 카테고리 (5개) + 상세 (카테고리당 2개)
INSERT INTO prompt_category (id, main_prompt, color, description, created_date, updated_date) VALUES
(1, '기쁨', '#FFD54F', '샘플 카테고리: 밝고 경쾌한 분위기', DATEADD('DAY', -30, LOCALTIMESTAMP), DATEADD('DAY', -30, LOCALTIMESTAMP)),
(2, '평온', '#81C784', '샘플 카테고리: 잔잔하고 편안한 분위기', DATEADD('DAY', -30, LOCALTIMESTAMP), DATEADD('DAY', -30, LOCALTIMESTAMP)),
(3, '슬픔', '#64B5F6', '샘플 카테고리: 차분하고 쓸쓸한 분위기', DATEADD('DAY', -30, LOCALTIMESTAMP), DATEADD('DAY', -30, LOCALTIMESTAMP)),
(4, '설렘', '#F48FB1', '샘플 카테고리: 두근거리는 분위기', DATEADD('DAY', -30, LOCALTIMESTAMP), DATEADD('DAY', -30, LOCALTIMESTAMP)),
(5, '분노', '#E57373', '샘플 카테고리: 강렬하고 거친 분위기', DATEADD('DAY', -30, LOCALTIMESTAMP), DATEADD('DAY', -30, LOCALTIMESTAMP));

INSERT INTO prompt_category_detail (id, prompt_category_id, prompt, created_date, updated_date)
SELECT r.x, 1 + MOD(r.x - 1, 5), CONCAT('sample music prompt ', r.x),
       DATEADD('DAY', -30, LOCALTIMESTAMP), DATEADD('DAY', -30, LOCALTIMESTAMP)
FROM SYSTEM_RANGE(1, 10) r(x);

-- 관리자 1명 (id 1)
INSERT INTO user (id, sns_id, email, password, nickname, profile_image_url, birth_date, phone_number, role,
                  last_login_date, deleted_date, created_date, updated_date) VALUES
(1, 'local-sns-admin', 'admin@example.com', 'dummy-password-hash-not-real', '관리자', NULL, NULL, NULL, 'ROLE_ADMIN',
 LOCALTIMESTAMP, NULL, DATEADD('DAY', -30, LOCALTIMESTAMP), LOCALTIMESTAMP);

-- 일반 유저 20명 (id 2~21, userN@example.com)
-- 가입일: MOD(n*7, 30)일 전 (n=1..20에서 0~29일 사이로 서로 다르게 분산)
-- 마지막 로그인: 가입일과 오늘 사이 (GREATEST로 가입 시각보다 앞서지 않게 한다)
INSERT INTO user (id, sns_id, email, password, nickname, profile_image_url, birth_date, phone_number, role,
                  last_login_date, deleted_date, created_date, updated_date)
SELECT u.x + 1,
       CONCAT('local-sns-', u.x),
       CONCAT('user', u.x, '@example.com'),
       'dummy-password-hash-not-real',
       CONCAT('테스트유저', LPAD(CAST(u.x AS VARCHAR), 2, '0')),
       NULL, NULL, NULL,
       'ROLE_USER',
       GREATEST(u.signup, DATEADD('HOUR', -MOD(u.x * 3, 10), DATEADD('DAY', -MOD(u.x * 13, MOD(u.x * 7, 30) + 1), LOCALTIMESTAMP))),
       NULL,
       u.signup,
       u.signup
FROM (SELECT x, DATEADD('HOUR', -MOD(x * 5, 24), DATEADD('DAY', -MOD(x * 7, 30), LOCALTIMESTAMP)) AS signup
      FROM SYSTEM_RANGE(1, 20) r(x)) u;

-- 일기 60개 (id 1~60)
-- 작성자: 유저 n = 1 + MOD(x-1, 20) (id n+1), 작성일: 해당 유저 가입일 이후 0~29일 전
-- MOD(x, 4) <> 0 인 일기(45개)는 음악 생성 완료(music_status = TRUE), 나머지 15개는 미완료
INSERT INTO diary (id, user_id, prompt_category_id, content, summary, prompt, music_status, created_date, updated_date)
SELECT d.x,
       d.n + 1,
       1 + MOD(d.x, 5),
       CONCAT('샘플 일기 본문 ', d.x, ': 오늘은 평범하지만 기억하고 싶은 하루였다.'),
       CONCAT('샘플 요약 ', d.x),
       CONCAT('sample diary prompt ', d.x),
       MOD(d.x, 4) <> 0,
       DATEADD('HOUR', -MOD(d.x * 5, 12), DATEADD('DAY', -MOD(d.x * 11, MOD(d.n * 7, 30) + 1), LOCALTIMESTAMP)),
       DATEADD('HOUR', -MOD(d.x * 5, 12), DATEADD('DAY', -MOD(d.x * 11, MOD(d.n * 7, 30) + 1), LOCALTIMESTAMP))
FROM (SELECT x, 1 + MOD(x - 1, 20) AS n FROM SYSTEM_RANGE(1, 60) s(x)) d;

-- 일기 질문·답변: MOD(x, 5) <> 0 인 일기(48개) × 3문항
INSERT INTO diary_question_answer (diary_id, question_order, question, answer, created_date, updated_date)
SELECT d.id,
       q.x,
       CASE q.x WHEN 1 THEN '오늘 하루 중 가장 기억에 남는 순간은 언제였어?'
                WHEN 2 THEN '그때 어떤 기분이 들었어?'
                ELSE '내일의 나에게 해주고 싶은 말이 있어?' END,
       CONCAT('샘플 답변 ', d.id, '-', q.x),
       d.created_date, d.created_date
FROM diary d, SYSTEM_RANGE(1, 3) q(x)
WHERE MOD(d.id, 5) <> 0;

-- 감정 분석: MOD(x, 3) <> 0 인 일기(40개) × 2개
INSERT INTO diary_emotion_insight (diary_id, emotion, reason, created_date, updated_date)
SELECT d.id,
       CASE MOD(d.id + e.x, 5) WHEN 0 THEN '기쁨' WHEN 1 THEN '평온' WHEN 2 THEN '슬픔' WHEN 3 THEN '설렘' ELSE '분노' END,
       CONCAT('샘플 감정 분석 이유 ', d.id, '-', e.x),
       DATEADD('MINUTE', 1, d.created_date), DATEADD('MINUTE', 1, d.created_date)
FROM diary d, SYSTEM_RANGE(1, 2) e(x)
WHERE MOD(d.id, 3) <> 0;

-- 가사: 음악 생성 완료 일기(45개)당 1개 (id = diary id)
INSERT INTO lyrics (id, diary_id, content, created_date, updated_date)
SELECT d.id, d.id,
       CONCAT('[Verse]', CHAR(10), '샘플 가사 ', d.id, CHAR(10), '[Chorus]', CHAR(10), '샘플 후렴'),
       DATEADD('MINUTE', 2, d.created_date), DATEADD('MINUTE', 2, d.created_date)
FROM diary d
WHERE d.music_status = TRUE;

-- 음악: 음악 생성 완료 일기(45개)당 2개 (id = (diary id - 1) * 2 + k)
INSERT INTO music (id, diary_id, prompt_category_id, generation_prompt, suno_ai_music_id, file_url, created_date, updated_date)
SELECT (d.id - 1) * 2 + k.x,
       d.id,
       d.prompt_category_id,
       CONCAT('sample generation prompt ', d.id),
       CONCAT('fake-suno-', d.id, '-', k.x),
       CONCAT('https://example.com/music/', d.id, '-', k.x, '.mp3'),
       DATEADD('MINUTE', 5, d.created_date), DATEADD('MINUTE', 5, d.created_date)
FROM diary d, SYSTEM_RANGE(1, 2) k(x)
WHERE d.music_status = TRUE;

-- 공유 URL: MOD(diary id, 6) = 1 인 일기의 첫 번째 음악(10개), 생성 후 7일 만료
INSERT INTO share_url (music_id, url, expiration_date, created_date, updated_date)
SELECT m.id,
       CONCAT('https://example.com/share/', m.id),
       DATEADD('DAY', 7, m.created_date),
       m.created_date, m.created_date
FROM music m
WHERE MOD(m.diary_id, 6) = 1 AND MOD(m.id, 2) = 1;

-- FCM 토큰: 짝수 번호 유저 10명 (명백한 가짜 값)
INSERT INTO fcm_token (user_id, fcm_token, device_id, created_date, updated_date)
SELECT u.id,
       CONCAT('fake-fcm-token-local-', u.id),
       CONCAT('fake-device-', u.id),
       u.created_date, u.created_date
FROM user u
WHERE u.role = 'ROLE_USER' AND MOD(u.id - 1, 2) = 0;

-- 앱에서 새로 INSERT할 때 위의 명시 id와 겹치지 않도록 IDENTITY 재시작
ALTER TABLE prompt_category ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE prompt_category_detail ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE user ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE diary ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE lyrics ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE music ALTER COLUMN id RESTART WITH 1000;
