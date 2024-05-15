package com.eumakase.eumakase.service;

import com.eumakase.eumakase.config.SunoAIConfig;
import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.Music;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIRequestDto;
import com.eumakase.eumakase.dto.sunoAI.SunoAIResponseDto;
import com.eumakase.eumakase.exception.MusicException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicService {
    private final FileService fileService;
    private final SunoAIConfig sunoAIConfig;
    private final SunoAIConfig.SunoAIProperties sunoAIProperties;
    private final ChatGPTService chatGPTService;
    private final MusicRepository musicRepository;
    private final DiaryRepository diaryRepository;

    public MusicService(FileService fileService, SunoAIConfig sunoAIConfig, SunoAIConfig.SunoAIProperties sunoAIProperties, ChatGPTService chatGPTService, MusicRepository musicRepository, DiaryRepository diaryRepository) {
        this.fileService = fileService;
        this.sunoAIConfig = sunoAIConfig;
        this.sunoAIProperties = sunoAIProperties;
        this.chatGPTService = chatGPTService;
        this.musicRepository = musicRepository;
        this.diaryRepository = diaryRepository;
    }

    @Transactional
    public void createMusic(MusicCreateRequestDto musicCreateRequestDto) {
        Diary diary = diaryRepository.findById(musicCreateRequestDto.getDiaryId())
                .orElseThrow(() -> new IllegalArgumentException("Diary not found with id: " + musicCreateRequestDto.getDiaryId()));

        PromptCategory promptCategory = null;

        //TODO: promptCategoryRepository 구현 필요
//        if (requestDto.getPromptCategoryId() != null) {
//            promptCategory = promptCategoryRepository.findById(requestDto.getPromptCategoryId())
//                    .orElseThrow(() -> new IllegalArgumentException("PromptCategory not found with id: " + requestDto.getPromptCategoryId()));
//        }

        // SunoAI 음악 생성 요청 DTO 설정
        SunoAIRequestDto sunoAIRequestDto = new SunoAIRequestDto();
        sunoAIRequestDto.setPrompt(musicCreateRequestDto.getGenerationPrompt());

        // SunoAI 음악 ID 목록 생성
        List<String> sunoAIMusicIds;
        try {
            sunoAIMusicIds = generateSunoAIMusic(sunoAIRequestDto);
        } catch (Exception e) {
            log.error("SunoAI 음악 생성 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new MusicException("SunoAI 음악 생성 중 오류가 발생했습니다." + e.getMessage());
        }

        // Music 엔티티 두 개 생성 및 저장
        if (sunoAIMusicIds.size() >= 2) {
            Music music1 = Music.builder()
                    .diary(diary)
                    .promptCategory(promptCategory)
                    .generationPrompt(musicCreateRequestDto.getGenerationPrompt())
                    .sunoAiMusicId(sunoAIMusicIds.get(0))
                    .build();

            Music music2 = Music.builder()
                    .diary(diary)
                    .promptCategory(promptCategory)
                    .generationPrompt(musicCreateRequestDto.getGenerationPrompt())
                    .sunoAiMusicId(sunoAIMusicIds.get(1))
                    .build();

            musicRepository.save(music1);
            musicRepository.save(music2);
        } else {
            throw new MusicException("SunoAI에서 생성된 음악 ID의 수가 부족합니다.");
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
            throw new Exception("ASuno AI 조회 API 호출 실패: " + ex.getMessage(), ex);
        }
    }

    /**
     * Music 엔티티의 fileUrl 필드 업데이트
     */
    @Transactional
    public void updateMusicFileUrls() {
        // fileUrl이 비어있는 Music 데이터를 조회
        List<Music> musicList = musicRepository.findByFileUrlIsNull();

        // suno_ai_music_id 목록 추출
        List<String> sunoAiMusicIds = musicList.stream()
                .map(Music::getSunoAiMusicId)
                .collect(Collectors.toList());

        if (sunoAiMusicIds.isEmpty()) {
            log.info("업데이트할 음악 데이터가 없습니다.");
            return;
        }

        // SunoAI 음악 세부 정보 조회
        try {
            List<Map<String, String>> musicDetails = getSunoAIMusicDetails(String.join(",", sunoAiMusicIds));

            // suno_ai_music_id로 그룹화된 map 생성
            Map<String, String> idToUrlMap = musicDetails.stream()
                    .collect(Collectors.toMap(
                            music -> music.get("id"),
                            music -> music.get("audio_url")
                    ));

            // 각 Music 엔티티에 fileUrl 설정
            for (Music music : musicList) {
                String audioUrl = idToUrlMap.get(music.getSunoAiMusicId());
                if (audioUrl != null) {
                    music.setFileUrl(audioUrl);
                }
            }

            // 업데이트된 Music 엔티티를 저장
            musicRepository.saveAll(musicList);
            log.info("총 {}개의 음악 파일 URL이 업데이트되었습니다.", musicList.size());
        } catch (Exception e) {
            log.error("음악 파일 URL 업데이트 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Music 삭제
     * @param musicId 삭제할 음악 ID
     */
    public void deleteMusic(Long musicId) {
        // Music을 찾고 없으면 예외 발생
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new MusicException("Music ID가 " + musicId + "인 데이터를 찾을 수 없습니다."));

        // Diary 삭제
        musicRepository.delete(music);

        // 삭제 후 다시 조회하여 확인
        Optional<Music> deletedMusic = musicRepository.findById(musicId);
        if (deletedMusic.isPresent()) {
            // 삭제 실패 시 예외 발생
            throw new MusicException("Music 삭제에 실패했습니다.");
        }
    }
}