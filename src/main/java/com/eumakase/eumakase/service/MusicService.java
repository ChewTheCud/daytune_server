package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.Diary;
import com.eumakase.eumakase.domain.Music;
import com.eumakase.eumakase.domain.PromptCategory;
import com.eumakase.eumakase.dto.music.MusicCreateRequestDto;
import com.eumakase.eumakase.exception.MusicException;
import com.eumakase.eumakase.repository.DiaryRepository;
import com.eumakase.eumakase.repository.MusicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MusicService {
    @Autowired
    private FileService fileService;

    private final MusicRepository musicRepository;
    private final DiaryRepository diaryRepository;

    public MusicService(MusicRepository musicRepository, DiaryRepository diaryRepository) {
        this.musicRepository = musicRepository;
        this.diaryRepository = diaryRepository;
    }

    /**
     * Music 생성
     */
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

        Music music = Music.builder()
                .diary(diary)
                .promptCategory(promptCategory)
                .build();

        musicRepository.save(music);
    }

    /**
     * file_url이 비어있는 Music 데이터 file_url 업데이트
     */
    @Transactional
    public void updateMusicFileUrls() {
        // file_url이 비어 있는 모든 Music 데이터 조회
        // 이는 아직 파일 URL이 할당되지 않은 음악 데이터를 식별하기 위함
        List<Music> emptyFileUrlMusics = musicRepository.findByFileUrlIsNull();

        // diaryId별로 Music 데이터 그룹화
        // 같은 일기에 속한 음악 데이터를 함께 처리하기 위함
        Map<Long, List<Music>> groupedByDiaryId = emptyFileUrlMusics.stream()
                .collect(Collectors.groupingBy(music -> music.getDiary().getId()));

        // 수정될 Music 객체들을 저장할 리스트를 초기화
        List<Music> musicsToUpdate = new ArrayList<>();

        // 각 diaryId 그룹에 대해서 반복 처리
        for (Map.Entry<Long, List<Music>> entry : groupedByDiaryId.entrySet()) {
            Long diaryId = entry.getKey(); // 현재 처리 중인 diaryId
            List<Music> musics = entry.getValue(); // 해당 diaryId에 속한 Music 데이터 목록

            // diaryId에 해당하는 모든 파일 URL 가져오기
            List<String> fileUrls = fileService.getFileDownloadUrlsByDiaryId(diaryId.toString());

            // 중복된 URL 제거 및 순서 유지
            // LinkedHashSet을 사용하여 중복을 제거하고, 원래의 순서를 유지
            LinkedHashSet<String> uniqueUrls = new LinkedHashSet<>(fileUrls);

            // 사용 가능한 URL 목록 생성
            // 중복이 제거된 URL 목록을 ArrayList로 변환하여 사용 가능한 URL 목록을 생성
            List<String> availableUrls = new ArrayList<>(uniqueUrls);

            // 각 Music 데이터에 URL 할당
            for (Music music : musics) {
                if (!availableUrls.isEmpty()) {
                    // 첫 번째 사용 가능한 URL 할당 및 목록에서 제거
                    // 이는 각 Music 데이터가 고유한 URL을 가지도록 하기 위함
                    String fileUrl = availableUrls.remove(0); // 사용 가능한 첫 번째 URL 할당
                    music.setFileUrl(fileUrl); // Music 데이터에 file_url 설정
                    musicsToUpdate.add(music); // 수정된 Music 객체를 업데이트 리스트에 추가
                }
            }
        }

        // 모든 수정된 Music 데이터를 데이터베이스에 한 번에 저장
        if (!musicsToUpdate.isEmpty()) {
            musicRepository.saveAll(musicsToUpdate);
        }
    }

    /**
     * Music 삭제
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