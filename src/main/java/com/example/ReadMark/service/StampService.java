package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.StampDTO;
import com.example.ReadMark.model.entity.Stamp;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.StampRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StampService {
    
    private final StampRepository stampRepository;
    private final UserRepository userRepository;
    
    /**
     * 도장 획득 처리
     * 20페이지 이상 읽은 날에 도장을 부여
     */
    public StampDTO earnStamp(Long userId, LocalDate date, Integer pagesRead) {
        try {
            // 이미 해당 날짜에 도장을 받았는지 확인
            if (stampRepository.existsByUser_UserIdAndEarnedDate(userId, date)) {
                log.debug("이미 도장을 받은 날짜입니다: 사용자 {}, 날짜 {}", userId, date);
                return null;
            }
            
            // 20페이지 이상인지 확인
            if (pagesRead < 20) {
                log.debug("도장 획득 조건 미달: 사용자 {}, 날짜 {}, 페이지 수 {}", userId, date, pagesRead);
                return null;
            }
            
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 도장 생성
            Stamp stamp = new Stamp();
            stamp.setUser(user);
            stamp.setEarnedDate(date);
            stamp.setPagesRead(pagesRead);
            stamp.setDescription(String.format("%d페이지 읽기 달성!", pagesRead));
            
            // 저장
            Stamp savedStamp = stampRepository.save(stamp);
            
            log.info("도장 획득: 사용자 {}, 날짜 {}, 페이지 수 {}", userId, date, pagesRead);
            
            return convertToDTO(savedStamp);
            
        } catch (Exception e) {
            log.error("도장 획득 처리 중 오류 발생: 사용자 {}, 날짜 {}", userId, date, e);
            return null;
        }
    }
    
    /**
     * 사용자의 모든 도장 조회
     */
    @Transactional(readOnly = true)
    public List<StampDTO> getUserStamps(Long userId) {
        List<Stamp> stamps = stampRepository.findByUser_UserIdOrderByEarnedDateDesc(userId);
        return stamps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 최근 N개 도장 조회
     */
    public List<StampDTO> getRecentStamps(Long userId, int limit) {
        List<Stamp> stamps = stampRepository.findTopNByUser_UserIdOrderByEarnedDateDesc(userId);
        return stamps.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 기간의 도장 조회
     */
    public List<StampDTO> getStampsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Stamp> stamps = stampRepository.findByUser_UserIdAndEarnedDateBetween(userId, startDate, endDate);
        return stamps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 총 도장 개수
     */
    @Transactional(readOnly = true)
    public long getTotalStampCount(Long userId) {
        return stampRepository.countByUser_UserId(userId);
    }
    
    /**
     * 특정 날짜에 도장을 받았는지 확인
     */
    public boolean hasStampForDate(Long userId, LocalDate date) {
        return stampRepository.existsByUser_UserIdAndEarnedDate(userId, date);
    }
    
    /**
     * Stamp 엔티티를 DTO로 변환
     */
    private StampDTO convertToDTO(Stamp stamp) {
        StampDTO dto = new StampDTO();
        dto.setStampId(stamp.getStampId());
        dto.setUserId(stamp.getUser().getUserId());
        dto.setEarnedDate(stamp.getEarnedDate());
        dto.setPagesRead(stamp.getPagesRead());
        dto.setCreatedAt(stamp.getCreatedAt());
        dto.setDescription(stamp.getDescription());
        
        // 추가 정보 설정
        dto.setFormattedDate(stamp.getEarnedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dto.setAchievement(String.format("%d페이지 읽기", stamp.getPagesRead()));
        
        return dto;
    }
}
