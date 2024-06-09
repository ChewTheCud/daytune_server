package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.SunoAIConfig;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.Music;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.domain.ShareUrl;
import com.eumakase.eumakase.dto.music.*;
import com.eumakase.eumakase.dto.sunoAI.SunoAIRequestDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIResponseDto;
import com.eumakase.eumakase.exception.MusicException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import com.eumakase.eumakase.repository.ShareUrlRepository;
import com.eumakase.eumakase.util.FileUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicService {
    private final SunoAIConfig sunoAIConfig;
    private final SunoAIConfig.SunoAIProperties sunoAIProperties;
    private final MusicRepository musicRepository;
    private final DiaryRepository diaryRepository;
    private final ShareUrlRepository shareUrlRepository;
    private final S3Service s3Service;
    private final FirebaseService firebaseService;
    private final FCMService fcmService;

    public MusicService(SunoAIConfig sunoAIConfig, SunoAIConfig.SunoAIProperties sunoAIProperties, MusicRepository musicRepository, DiaryRepository diaryRepository, ShareUrlRepository shareUrlRepository, S3Service s3Service, FirebaseService firebaseService, FCMService fcmService) {
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
        // 일기 조회
        Diary diary = diaryRepository.findById(musicCreateRequestDto.getDiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Diary not found with id: " + musicCreateRequestDto.getDiaryId()));

        PromptCategory promptCategory = null;

        // TODO: promptCategoryRepository 구현 필요
        // promptCategory가 존재하는 경우 조회하여 설정
//        if (requestDto.getPromptCategoryId() != null) {
//            promptCategory = promptCategoryRepository.findById(requestDto.getPromptCategoryId())
//                    .orElseThrow(() -> new IllegalArgumentException("PromptCategory not found with id: " + requestDto.getPromptCategoryId()));
//        }

        // SunoAI 음악 생성 요청 DTO 설정
        SunoAIRequestDto sunoAIRequestDto = new SunoAIRequestDto();
        sunoAIRequestDto.setPrompt(musicCreateRequestDto.getGenerationPrompt());

        // SunoAI 음악 ID 목록 생성
        List<String> sunoAIMusicIds = new ArrayList<>();
        try {
            sunoAIMusicIds = generateSunoAIMusic(sunoAIRequestDto);
        } catch (Exception e) {
            log.error("SunoAI 음악 생성 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }

        // Music 엔티티 두 개 생성 및 저장
        Music music1 = Music.builder()
                .diary(diary)
                .promptCategory(null)
                .generationPrompt(musicCreateRequestDto.getGenerationPrompt())
                .sunoAiMusicId(!sunoAIMusicIds.isEmpty() ? sunoAIMusicIds.get(0) : null)
                .build();

        Music music2 = Music.builder()
                .diary(diary)
                .promptCategory(null)
                .generationPrompt(musicCreateRequestDto.getGenerationPrompt())
                .sunoAiMusicId(sunoAIMusicIds.size() > 1 ? sunoAIMusicIds.get(1) : null)
                .build();

        musicRepository.save(music1);
        musicRepository.save(music2);

        // SunoAI에서 생성된 음악 ID의 수가 부족할 경우 로그를 남김
        if (sunoAIMusicIds.size() < 2) {
            log.warn("SunoAI에서 생성된 음악 ID의 수가 부족합니다.");
        }
    }

    /**
     * SunoAI HTTP 엔티티 빌드
     * @return HTTP 엔티티
     */
    private HttpEntity<String> buildHttpEntity() {
        HttpHeaders headers = sunoAIConfig.httpHeaders();
        return new HttpEntity<>(headers);
    }

    /**
     * SunoAI 음악 생성
     * @param sunoAIRequestDto SunoAI 음악 생성 요청 DTO
     * @return 생성된 음악 ID 목록
     * @throws Exception 예외 발생 시
     */
    public List<String> generateSunoAIMusic(SunoAIRequestDto sunoAIRequestDto) throws Exception {
        try {
            HttpEntity<SunoAIRequestDto> requestEntity = new HttpEntity<>(sunoAIRequestDto, buildHttpEntity().getHeaders());

            // SunoAI API 호출
            ResponseEntity<List<SunoAIResponseDto>> response = sunoAIConfig.restTemplate().exchange(
                    sunoAIProperties.getUrl() + "/generate",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}); // List<SunoAIResponseDto> 타입으로 응답을 매핑

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new Exception("Suno AI 생성 API 호출 실패. " + response.getStatusCode());
            }

            // 생성된 음악 ID 목록 추출
            List<String> songIds = response.getBody().stream()
                    .filter(musicData -> "submitted".equals(musicData.getStatus()) && musicData.getId() != null)
                    .map(SunoAIResponseDto::getId)
                    .collect(Collectors.toList());
            return songIds;
        } catch (HttpClientErrorException ex) {
            log.error("Suno AI 생성 API 호출 실패: {}", ex.getResponseBodyAsString(), ex);
            throw new Exception("Suno AI API 호출 실패 : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Suno AI 생성 API 호출 실패: {}", ex.getMessage(), ex);
            throw new Exception("Suno AI API 호출 실패: " + ex.getMessage(), ex);
        }
    }

    /**
     * SunoAI 음악 세부 정보 조회
     * @param songIds 쉼표로 구분된 음악 ID 문자열
     * @return 음악 세부 정보 목록
     * @throws Exception 예외 발생 시
     */
    public List<Map<String, String>> getSunoAIMusicDetails(String songIds) throws Exception {
        try {
            HttpHeaders headers = buildHttpEntity().getHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = sunoAIProperties.getUrl() + "/get?ids=" + songIds; // Query parameter로 songId 전달
            ResponseEntity<List<SunoAIResponseDto>> response = sunoAIConfig.restTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<SunoAIResponseDto>>() {});

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new Exception("Suno AI 조회 API 호출 실패: " + response.getStatusCode());
            }
            List<SunoAIResponseDto> allMusicData = response.getBody();
            if (allMusicData == null || allMusicData.isEmpty()) {
                throw new Exception("No music data found in response.");
            }

            // 필터링: "complete" 상태만
            List<Map<String, String>> completedMusicData = allMusicData.stream()
                    .filter(music -> "complete".equals(music.getStatus()) && music.getAudioUrl() != null)
                    .map(music -> Map.of("id", music.getId(), "audio_url", music.getAudioUrl()))
                    .collect(Collectors.toList());
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
        // fileUrl이 비어있는 Music 데이터를 조회
        List<Music> musicList = musicRepository.findBySunoAiMusicIdIsNotNullAndFileUrlIsNull();

        // 조회된 Music 데이터에서 suno_ai_music_id 목록을 추출
        List<String> sunoAiMusicIds = musicList.stream()
                .map(Music::getSunoAiMusicId)
                .collect(Collectors.toList());

        // 업데이트된 음악 파일과 업데이트되지 않은 음악 파일 정보를 저장할 리스트를 초기화
        List<MusicUpdateInfo> updatedMusicFiles = new ArrayList<>();
        List<MusicUpdateInfo> notUpdatedMusicFiles = new ArrayList<>();

        // 사용자별로 음악 업데이트 정보를 저장할 맵 초기화
        Map<Long, List<MusicUpdateInfo>> userMap = new HashMap<>();

        // 업데이트할 음악 데이터가 없는 경우
        if (sunoAiMusicIds.isEmpty()) {
            log.info("업데이트할 음악 데이터가 없습니다.");
            return new MusicUpdateFileUrlsResultDto(updatedMusicFiles, notUpdatedMusicFiles);
        }

        // SunoAI API를 통해 음악 세부 정보를 조회하여 5개씩 처리
        for (int i = 0; i < sunoAiMusicIds.size(); i += 5) {
            // 조회가 필요한 Suno AI id 목록을 추출
            List<String> sunoMusicIds = sunoAiMusicIds.subList(i, Math.min(i + 5, sunoAiMusicIds.size()));
            try {
                // SunoAI API를 호출하여 음악 세부 정보를 조회
                List<Map<String, String>> musicDetails = getSunoAIMusicDetails(String.join(",", sunoMusicIds));

                // 조회된 음악 세부 정보를 id와 audio_url로 매핑
                Map<String, String> idToUrlMap = musicDetails.stream()
                        .collect(Collectors.toMap(
                                music -> music.get("id"),
                                music -> music.get("audio_url")
                        ));

                // 각 Music 엔티티에 대해 파일 URL을 설정하고 업데이트된 정보 리스트에 추가
                for (Music music : musicList) {
                    String audioUrl = idToUrlMap.get(music.getSunoAiMusicId());
                    if (audioUrl != null) {
                        // audioUrl이 존재하면, Music 엔티티의 fileUrl을 설정
                        music.setFileUrl(audioUrl);
                        // 업데이트된 음악 파일 정보를 리스트에 추가
                        updatedMusicFiles.add(new MusicUpdateInfo(music.getDiary().getId(), music.getId(), audioUrl));

                        // 사용자별로 업데이트된 음악 정보를 맵에 추가
                        Long userId = music.getDiary().getUser().getId();
                        userMap.computeIfAbsent(userId, k -> new ArrayList<>())
                                .add(new MusicUpdateInfo(music.getDiary().getId(), music.getId(), audioUrl));
                    } else {
                        // audioUrl이 없으면, 업데이트되지 않은 음악 파일 정보를 리스트에 추가
                        notUpdatedMusicFiles.add(new MusicUpdateInfo(music.getDiary().getId(), music.getId(), null));
                    }
                }
                musicRepository.saveAll(musicList);
            } catch (Exception e) {
                log.error("음악 파일 URL 업데이트 중 오류가 발생했습니다.", e);
            }
        }

        // 각 사용자에 대해 한 번만 FCM 알림을 전송
        for (Map.Entry<Long, List<MusicUpdateInfo>> entry : userMap.entrySet()) {
            Long userId = entry.getKey();
            String fcmToken = null;
            String nickname = null;
            try {
                fcmToken = fcmService.getFcmTokenByUserId(userId);
                nickname = fcmService.getUserNicknameByUserId(userId);
            } catch (RuntimeException e) {
                log.error("FCM 토큰을 찾을 수 없습니다: " + e.getMessage());
            }

            if (fcmToken != null) {
                String title = "DJ 선곡 완료!";
                String message = nickname + "님의 사연으로 작곡한 음악이 도착했어요!";
                try {
                    fcmService.sendPushNotification(title, message, fcmToken);
                } catch (FirebaseMessagingException e) {
                    log.error("FCM 알림 전송 중 오류가 발생했습니다.", e);
                }
            } else {
                log.warn("FCM 토큰을 찾을 수 없습니다: User ID: " + userId);
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