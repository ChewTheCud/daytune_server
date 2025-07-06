package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.SunoAIConfig;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.Music;
import com.eumakase.eumakase.domain.ShareUrl;
import com.eumakase.eumakase.dto.music.*;
import com.eumakase.eumakase.dto.sunoAI.SunoAIGenerationDetailResultDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIGenerationResultDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIRequestDto;
import com.eumakase.eumakase.exception.MusicException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import com.eumakase.eumakase.repository.ShareUrlRepository;
import com.eumakase.eumakase.util.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicService {
    private final RestTemplate restTemplate;
    private final SunoAIConfig sunoAIConfig;
    private final SunoAIConfig.SunoAIProperties sunoAIProperties;
    private final MusicRepository musicRepository;
    private final DiaryRepository diaryRepository;
    private final ShareUrlRepository shareUrlRepository;
    private final S3Service s3Service;
    private final FirebaseService firebaseService;
    private final FCMService fcmService;

    @Value("${sunoai.secret-key}")
    private String SECRET_KEY;

    public MusicService(RestTemplate restTemplate, SunoAIConfig sunoAIConfig, SunoAIConfig.SunoAIProperties sunoAIProperties, MusicRepository musicRepository, DiaryRepository diaryRepository, ShareUrlRepository shareUrlRepository, S3Service s3Service, FirebaseService firebaseService, FCMService fcmService) {
        this.restTemplate = restTemplate;
        this.sunoAIConfig = sunoAIConfig;
        this.sunoAIProperties = sunoAIProperties;
        this.musicRepository = musicRepository;
        this.diaryRepository = diaryRepository;
        this.shareUrlRepository = shareUrlRepository;
        this.s3Service = s3Service;
        this.firebaseService = firebaseService;
        this.fcmService = fcmService;
    }

    /**
     * 음악 생성
     * @param musicCreateRequestDto 음악 생성 요청 DTO
     */
    @Transactional
    public void createMusic(MusicCreateRequestDto musicCreateRequestDto) {
        log.info("[createMusic 호출] diaryId={} / generationPrompt='{}' / style='{}' / title='{}'",
                musicCreateRequestDto.getDiaryId(),
                musicCreateRequestDto.getPrompt(),
                musicCreateRequestDto.getStyle()
        );
        // 1. Diary 엔티티 조회
        Diary diary = diaryRepository.findById(musicCreateRequestDto.getDiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Diary not found with id: " + musicCreateRequestDto.getDiaryId()));

        // 2. SunoAI 음악 생성 요청 DTO 설정
        SunoAIRequestDto sunoAIRequestDto = SunoAIRequestDto.builder()
                .prompt(musicCreateRequestDto.getPrompt())
                .style(musicCreateRequestDto.getStyle())
                .negativeTags(musicCreateRequestDto.getNegativeTags()) // 필요 시 null 가능
                .build();

        // 3. SunoAI로부터 단일 taskId 획득
        String taskId;
        try {
            taskId = generateSunoAIMusic(sunoAIRequestDto);
        } catch (Exception e) {
            log.error("SunoAI 음악 생성 중 오류가 발생했습니다: {}", e.getMessage(), e);
            return;
        }

        // 4. Music 엔티티 두 개 생성 및 저장
        Music music1 = Music.builder()
                .diary(diary)
                .generationPrompt(musicCreateRequestDto.getPrompt())
                .sunoAiMusicId(taskId)
                .build();

        Music music2 = Music.builder()
                .diary(diary)
                .generationPrompt(musicCreateRequestDto.getPrompt())
                .sunoAiMusicId(taskId)
                .build();

        musicRepository.save(music1);
        musicRepository.save(music2);
    }

    /**
     * SunoAI 음악 생성
     * @param sunoAIRequestDto SunoAI 음악 생성 요청 DTO
     * @return 생성된 음악 ID 목록
     * @throws Exception 예외 발생 시
     */
    /**
     * SunoAI 음악 생성 (taskId 반환)
     * @param sunoAIRequestDto SunoAI 음악 생성 요청 DTO
     * @return 생성된 taskId 문자열
     * @throws Exception 예외 발생 시
     */
    public String generateSunoAIMusic(SunoAIRequestDto sunoAIRequestDto) throws Exception {
        String uri = sunoAIProperties.getUrl() + "/api/v2/generate";

        HttpHeaders headers = sunoAIConfig.httpHeaders(sunoAIProperties);
        headers.setBearerAuth(SECRET_KEY);

        try {
            // ★ 요청 바디(JSON) 직렬화하여 로그로 출력 ★
            ObjectMapper mapper = new ObjectMapper();
            String requestJson = mapper.writeValueAsString(sunoAIRequestDto);
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            log.info("[SunoAI 요청 Authorization 헤더] {}", authHeader);
            log.info("[generateSunoAIMusic] 호출 URL = {}", uri);
            log.info("[SunoAI 요청 바디] {}", requestJson);

            HttpEntity<SunoAIRequestDto> requestEntity = new HttpEntity<>(sunoAIRequestDto, headers);

            // SunoAIGenerationResultDto로 응답 매핑
            ResponseEntity<SunoAIGenerationResultDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    requestEntity,
                    SunoAIGenerationResultDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new Exception("Suno AI 생성 API 호출 실패. HTTP 상태: " + response.getStatusCode());
            }

            SunoAIGenerationResultDto resultDto = response.getBody();
            if (resultDto.getCode() != 200 || resultDto.getData() == null) {
                throw new Exception("Suno AI 생성 API 응답에서 오류 발생. code="
                        + resultDto.getCode() + ", msg=" + resultDto.getMsg());
            }

            String taskId = resultDto.getData().getTaskId();
            if (taskId == null || taskId.isEmpty()) {
                throw new Exception("응답에서 taskId를 찾을 수 없습니다.");
            }
            System.out.println("[taskId] "+taskId);
            return taskId;
        } catch (HttpClientErrorException ex) {
            log.error("Suno AI 생성 API 호출 실패: {}", ex.getResponseBodyAsString(), ex);
            throw new Exception("Suno AI API 호출 실패: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Suno AI 생성 API 호출 실패: {}", ex.getMessage(), ex);
            throw new Exception("Suno AI API 호출 실패: " + ex.getMessage(), ex);
        }
    }

    /**
     * Suno AI 음악 생성 세부 정보 조회 (GET /api/v2/generate/record-info)
     * @param taskId Suno AI에서 생성된 taskId
     * @return 상태가 "SUCCESS" 또는 "complete"인 트랙들의 ID와 audio_url을 담은 리스트
     * @throws Exception 조회 중 예외 발생 시
     */
    /**
     * Suno AI 음악 생성 세부 정보 조회 (GET /api/v2/generate/record-info)
     * @param taskId Suno AI에서 생성된 taskId
     * @return 생성된 트랙들의 ID와 audioUrl 정보를 담은 리스트
     * @throws Exception 조회 중 예외 발생 시
     */
    public List<Map<String, String>> getSunoAIMusicDetails(String taskId) throws Exception {
        // 엔드포인트: /api/v2/generate/record-info?taskId={taskId}
        String uri = sunoAIProperties.getUrl() + "/api/v2/generate/record-info?taskId=" + taskId;

        try {
            HttpHeaders headers = sunoAIConfig.httpHeaders(sunoAIProperties);
            headers.setBearerAuth(SECRET_KEY);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);


            ResponseEntity<SunoAIGenerationDetailResultDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    SunoAIGenerationDetailResultDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new Exception("Suno AI 조회 API 호출 실패: HTTP 상태 " + response.getStatusCode());
            }

            SunoAIGenerationDetailResultDto detailResultDto = response.getBody();
            if (detailResultDto.getCode() != 200 || detailResultDto.getData() == null) {
                throw new Exception("Suno AI 조회 API 응답 오류 발생: code="
                        + detailResultDto.getCode() + ", msg=" + detailResultDto.getMsg());
            }

            SunoAIGenerationDetailResultDto.ResponseNode responseNode = detailResultDto.getData().getResponse();
            if (responseNode == null || responseNode.getSunoData() == null) {
                throw new Exception("응답에서 sunoData 정보가 없습니다.");
            }

            // sunoData 리스트 순회하며 id와 streamAudioUrl을 Map으로 변환
            List<Map<String, String>> completedMusicData = new ArrayList<>();
            for (SunoAIGenerationDetailResultDto.TrackInfo track : responseNode.getSunoData()) {
                String id = track.getId();
                String sourceAudioUrl = track.getSourceAudioUrl();
                if (id != null && sourceAudioUrl != null && !sourceAudioUrl.isEmpty()) {
                    completedMusicData.add(Map.of(
                            "id", id,
                            "audio_url", sourceAudioUrl
                    ));
                }
            }

            return completedMusicData;
        } catch (HttpClientErrorException ex) {
            log.error("Suno AI 조회 API 호출 실패: {}", ex.getResponseBodyAsString(), ex);
            throw new Exception("Suno AI 조회 API 호출 실패: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            log.error("Suno AI 조회 API 호출 실패: {}", ex.getMessage(), ex);
            throw new Exception("Suno AI 조회 API 호출 실패: " + ex.getMessage(), ex);
        }
    }




    /**
     * 특정 일기의 음악 정보 조회
     * @param diaryId 일기의 ID
     * @return 일기에 속한 음악들의 정보 리스트
     */
    public List<MusicReadResponseDto> getMusicByDiaryId(Long diaryId) {
        List<Music> musics = musicRepository.findByDiaryId(diaryId);
        if (!diaryRepository.existsById(diaryId)) {
            throw new MusicException("Diary ID가 " + diaryId + "인 데이터를 찾을 수 없습니다.");
        }
        return musics.stream()
                .map(music -> new MusicReadResponseDto(music.getId(), music.getFileUrl()))
                .collect(Collectors.toList());
    }

    /**
     * Music 테이블의 fileUrl이 비어있는 레코드들을 업데이트
     * @return MusicUpdateFileUrlsResultDto - 업데이트된 음악 파일 정보와 업데이트되지 않은 음악 파일 정보를 포함
     */
    @Transactional
    public MusicUpdateFileUrlsResultDto updateMusicFileUrls() {
        // fileUrl이 NULL이거나 빈 문자열("")인 레코드를 모두 조회
        List<Music> musicList = musicRepository
                .findBySunoAiMusicIdIsNotNullAndFileUrlIsNullOrFileUrl("");

        // taskId 별로 그룹화
        Map<String, List<Music>> musicByTaskId = musicList.stream()
                .collect(Collectors.groupingBy(Music::getSunoAiMusicId));

        List<MusicUpdateInfo> updatedMusicFiles = new ArrayList<>();
        List<MusicUpdateInfo> notUpdatedMusicFiles = new ArrayList<>();
        Map<Long, List<MusicUpdateInfo>> userMap = new HashMap<>();

        for (Map.Entry<String, List<Music>> entry : musicByTaskId.entrySet()) {
            String taskId = entry.getKey();
            List<Music> musicsForTask = entry.getValue();

            try {
                // 상세 조회 후, List<Map<String,String>>에서 streamAudioUrl(= "audio_url") 추출
                List<Map<String, String>> musicDetails = getSunoAIMusicDetails(taskId);

                for (int i = 0; i < musicsForTask.size(); i++) {
                    Music music = musicsForTask.get(i);
                    String audioUrl = null;
                    if (i < musicDetails.size()) {
                        audioUrl = musicDetails.get(i).get("audio_url");
                    }

                    if (audioUrl != null && !audioUrl.isEmpty()) {
                        music.setFileUrl(audioUrl);
                        updatedMusicFiles.add(new MusicUpdateInfo(
                                music.getDiary().getId(),
                                music.getId(),
                                audioUrl
                        ));

                        Long userId = music.getDiary().getUser().getId();
                        userMap.computeIfAbsent(userId, k -> new ArrayList<>())
                                .add(new MusicUpdateInfo(
                                        music.getDiary().getId(),
                                        music.getId(),
                                        audioUrl
                                ));
                    } else {
                        notUpdatedMusicFiles.add(new MusicUpdateInfo(
                                music.getDiary().getId(),
                                music.getId(),
                                null
                        ));
                    }
                }
            } catch (Exception e) {
                log.error("음악 파일 URL 업데이트 중 오류 발생: taskId={}, error={}", taskId, e.getMessage(), e);
                // 조회 실패 시 해당 taskId에 속한 모든 Music에 notUpdated 추가
                for (Music music : musicsForTask) {
                    notUpdatedMusicFiles.add(new MusicUpdateInfo(
                            music.getDiary().getId(),
                            music.getId(),
                            null
                    ));
                }
            }
        }

        // 1. Music 엔티티 모두 저장
        List<Music> allSavedMusic = musicByTaskId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        musicRepository.saveAll(allSavedMusic);

        // 2. diary별로 musicStatus 갱신
        //    musicByTaskId 내부의 모든 Diary ID를 순회하며, 해당 Diary에 속한 모든 Music 파일이
        //    fileUrl을 정상적으로 가지고 있는지 확인 후 true로 변경
        Set<Long> diaryIdsToCheck = allSavedMusic.stream()
                .map(music -> music.getDiary().getId())
                .collect(Collectors.toSet());

        for (Long diaryId : diaryIdsToCheck) {
            // 해당 다이어리에 속한 모든 Music 조회
            List<Music> musics = musicRepository.findByDiaryId(diaryId);

            // **두 개 모두** fileUrl이 비어 있지 않은 경우 (null 아니고, 빈 문자열 아니면) → musicStatus = true
            boolean allHaveFileUrl = musics.stream()
                    .allMatch(m -> m.getFileUrl() != null && !m.getFileUrl().isEmpty());

            if (allHaveFileUrl) {
                Diary diary = diaryRepository.findById(diaryId)
                        .orElseThrow(() -> new IllegalStateException("Diary not found with id: " + diaryId));
                // 이미 true라면 굳이 save 하지 않음
                if (!diary.isMusicStatus()) {
                    diary.setMusicStatus(true);
                    diaryRepository.save(diary);
                }
            }
        }

        // 3. FCM 알림 전송 로직
        for (Map.Entry<Long, List<MusicUpdateInfo>> entry : userMap.entrySet()) {
            Long userId = entry.getKey();
            String fcmToken = null;
            String nickname = null;
            try {
                fcmToken = fcmService.getFcmTokenByUserId(userId);
                nickname = fcmService.getUserNicknameByUserId(userId);
            } catch (RuntimeException e) {
                log.error("FCM 토큰을 찾을 수 없습니다: {}", e.getMessage());
            }

            if (fcmToken != null) {
                String title = "DJ 선곡 완료!";
                String message = nickname + "님의 사연으로 작곡한 음악이 도착했어요!";
                try {
                    fcmService.sendPushNotification(title, message, fcmToken);
                } catch (FirebaseMessagingException e) {
                    log.error("FCM 알림 전송 중 오류 발생:", e);
                }
            } else {
                log.warn("FCM 토큰을 찾을 수 없습니다: User ID={}", userId);
            }
        }
        return new MusicUpdateFileUrlsResultDto(updatedMusicFiles, notUpdatedMusicFiles);
    }






    /**
     * Music 삭제
     * @param musicId 삭제할 음악 ID
     */
    public void deleteMusic(Long musicId) {
        // Music을 찾고 없으면 예외 발생
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new MusicException("Music ID가 " + musicId + "인 데이터를 찾을 수 없습니다."));

        // Music 삭제
        musicRepository.delete(music);

        // 삭제 후 다시 조회하여 확인
        Optional<Music> deletedMusic = musicRepository.findById(musicId);
        if (deletedMusic.isPresent()) {
            throw new MusicException("Music 삭제에 실패했습니다.");
        }
    }

    /**
     * 특정 일기의 음악 중 하나를 선택하고 나머지 음악을 삭제
     * 선택된 음악을 Firebase Storage에 저장하고, 해당 음악의 URL을 업데이트
     *
     * @param diaryId 선택된 음악이 속한 일기의 ID
     * @param musicId 선택된 음악의 ID
     * @throws MusicException 이미 선택된 음악일 경우 예외 발생
     */
    @Transactional
    public MusicSelectionResponseDto selectMusic(Long diaryId, Long musicId) {
        // 일기 엔티티 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("Diary not found with id: " + diaryId));

        // 선택된 음악 엔티티를 조회
        Music selectedMusic = musicRepository.findById(musicId)
                .orElseThrow(() -> new IllegalArgumentException("Music not found with id: " + musicId));

        // 이미 선택된 음악인지 확인 (이미 선택된 음악일 경우 fileUrl이 https://cdn1.suno.ai/~ -> https://storage.googleapis.com/~ 로 변경되어있음)
        if (selectedMusic.getFileUrl() != null && selectedMusic.getFileUrl().startsWith("https://storage.googleapis.com/")) {
            throw new MusicException("이미 선택된 음악입니다.");
        }

        // 선택된 음악을 제외한 나머지 음악 목록을 조회
        List<Music> otherMusics = musicRepository.findByDiaryIdAndIdNot(diaryId, musicId);

        // 나머지 음악들을 삭제
        musicRepository.deleteAll(otherMusics);

        try {
            // 원격 파일을 다운로드
            File tempFile = FileUtil.downloadFile(selectedMusic.getFileUrl());

            // 선택된 음악을 Firebase Storage에 저장
            String firebaseUrl = firebaseService.uploadFileToFirebaseStorage(tempFile, diary.getId().toString());

            // 임시 파일 삭제
            tempFile.delete();

            // 선택된 음악의 URL을 업데이트
            selectedMusic.setFileUrl(firebaseUrl);
            musicRepository.save(selectedMusic);

            // HTML 페이지 URL 생성
            String thumbnailUrl = "https://via.placeholder.com/300";  // 예시 썸네일 URL
            String htmlUrl = generateMusicSharePage(firebaseUrl, thumbnailUrl);

            // 공유 URL 생성 및 저장
            ShareUrl shareUrl = new ShareUrl();
            shareUrl.setMusic(selectedMusic);
            shareUrl.setUrl(htmlUrl);
            LocalDateTime expirationDate = LocalDateTime.now().plusDays(3); // 3일 유효 기간 설정
            shareUrl.setExpirationDate(expirationDate);
            shareUrlRepository.save(shareUrl);

            return MusicSelectionResponseDto.builder().shareUrl(htmlUrl).build();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 음악 파일의 Presigned URL을 사용하여 HTML 페이지를 생성하고, S3에 업로드
     * @param musicUrl 음악 파일의 Url
     * @return HTML 페이지의 Presigned URL
     */
    private String generateMusicSharePage(String musicUrl, String thumbnailUrl) {
        String htmlContent = "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Daytune</title>\n"
                + "    <meta property=\"og:image\" content=\"" + thumbnailUrl + "\" />\n"
                + "    <meta property=\"og:title\" content=\"Daytune\" />\n"
                + "    <meta property=\"og:description\" content=\"오늘의 감정을 음악으로\" />\n"
                + "    <style>\n"
                + "        body {\n"
                + "            margin: 0;\n"
                + "            background: linear-gradient(to bottom, #26FF63 0%, #FFFFFF 100%);\n"
                + "            display: flex;\n"
                + "            flex-direction: column;\n"
                + "            align-items: center;\n"
                + "            height: 100vh;\n"
                + "        }\n"
                + "        header {\n"
                + "            width: 100%;\n"
                + "            position: absolute;\n"
                + "            top: 5%;\n"
                + "            left: 50%;\n"
                + "            transform: translateX(-50%);\n"
                + "            display: flex;\n"
                + "            justify-content: center;\n"
                + "            text-align: center;\n"
                + "            padding: 10px 0;\n"
                + "            z-index: 1;\n"
                + "        }\n"
                + "        header img {\n"
                + "            max-width: 40%;\n"
                + "            cursor: pointer;\n"
                + "        }\n"
                + "        audio {\n"
                + "            margin-top: 400px; /* 이미지와 오디오 간격 조절 */\n"
                + "            max-width: 100%;\n"
                + "        }\n"
                + "    </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "    <header>\n"
                + "        <a href=\"https://chewthecud.today/\" target=\"_blank\">\n"
                + "            <img src=\"https://firebasestorage.googleapis.com/v0/b/daytune-3722b.appspot.com/o/images%2Flogo.png?alt=media&token=fb35d2b1-fdbb-47e2-975c-e32c4c2ed204\" alt=\"Daytune Logo\">\n"
                + "        </a>\n"
                + "    </header>\n"
                + "    <audio controls autoplay>\n"
                + "        <source src=\"" + musicUrl + "\" type=\"audio/mpeg\">\n"
                + "        Your browser does not support the audio element.\n"
                + "    </audio>\n"
                + "</body>\n"
                + "</html>";

        // S3에 HTML 페이지 업로드
        String pageFileName = "pages/" + UUID.randomUUID().toString() + ".html";
        return s3Service.uploadFile(pageFileName, htmlContent.getBytes(StandardCharsets.UTF_8), "text/html");
    }
}