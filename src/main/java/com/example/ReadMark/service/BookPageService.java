package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.model.dto.VisionAnalysisResultDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.model.entity.BookPage;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.BookPageRepository;
import com.example.ReadMark.repository.BookRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookPageService {
    
    private final BookPageRepository bookPageRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final GoogleVisionService visionService;
    
    /**
     * 새로운 책 페이지를 생성하고 저장합니다.
     */
    public BookPageDTO createBookPage(Long userId, Long bookId, byte[] imageBytes, 
                                    String deviceInfo, LocalDateTime captureTime) {
        try {
            // 사용자와 책 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
            
            // Google Vision API로 텍스트 추출
            VisionAnalysisResultDTO visionResult = visionService.extractTextFromImage(imageBytes);
            
            if (!visionResult.isSuccess()) {
                throw new RuntimeException("텍스트 추출 실패: " + visionResult.getErrorMessage());
            }
            
            // 페이지 번호 추출 (단순화)
            Integer pageNumber = visionResult.getEstimatedPageNumber();
            if (pageNumber == null) {
                pageNumber = 1; // 기본값으로 단순화
            }
            
            // 중복 페이지 확인
            Optional<BookPage> existingPage = bookPageRepository
                    .findByUser_UserIdAndBook_BookIdAndPageNumber(userId, bookId, pageNumber);
            if (existingPage.isPresent()) {
                log.warn("페이지 {}가 이미 존재합니다. 업데이트합니다.", pageNumber);
                return updateExistingPage(existingPage.get(), imageBytes, visionResult, deviceInfo, captureTime);
            }
            
            // 새 페이지 생성
            BookPage bookPage = new BookPage();
            bookPage.setUser(user);
            bookPage.setBook(book);
            bookPage.setPageNumber(pageNumber);
            bookPage.setImageData(imageBytes);
            bookPage.setCapturedAt(captureTime != null ? captureTime : LocalDateTime.now());
            bookPage.setConfidence(visionResult.getConfidence());
            bookPage.setDeviceInfo(deviceInfo);
            bookPage.setLanguage(visionResult.getLanguage());
            bookPage.setNumberCount(visionResult.getNumberCount());
            
            BookPage savedPage = bookPageRepository.save(bookPage);
            log.info("새 페이지 생성 완료: 사용자 {}, 책 {}, 페이지 {}", userId, bookId, pageNumber);
            
            return convertToDTO(savedPage);
            
        } catch (Exception e) {
            log.error("책 페이지 생성 실패: 사용자 {}, 책 {}", userId, bookId, e);
            throw new RuntimeException("책 페이지 생성 실패: " + e.getMessage());
        }
    }
    
    /**
     * 기존 페이지를 업데이트합니다.
     */
    private BookPageDTO updateExistingPage(BookPage existingPage, byte[] imageBytes, 
                                         VisionAnalysisResultDTO visionResult, 
                                         String deviceInfo, LocalDateTime captureTime) {
        existingPage.setImageData(imageBytes);
        existingPage.setCapturedAt(captureTime != null ? captureTime : LocalDateTime.now());
        existingPage.setConfidence(visionResult.getConfidence());
        existingPage.setDeviceInfo(deviceInfo);
        existingPage.setNumberCount(visionResult.getNumberCount());
        
        BookPage updatedPage = bookPageRepository.save(existingPage);
        log.info("기존 페이지 업데이트 완료: 페이지 ID {}", updatedPage.getPageId());
        
        return convertToDTO(updatedPage);
    }
    
    /**
     * 사용자와 책별로 페이지 목록을 조회합니다.
     */
    public List<BookPageDTO> getBookPages(Long userId, Long bookId) {
        List<BookPage> pages = bookPageRepository.findByUser_UserIdAndBook_BookIdOrderByPageNumberAsc(userId, bookId);
        return pages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 페이지를 조회합니다.
     */
    public BookPageDTO getBookPage(Long userId, Long bookId, Integer pageNumber) {
        BookPage page = bookPageRepository.findByUser_UserIdAndBook_BookIdAndPageNumber(userId, bookId, pageNumber)
                .orElseThrow(() -> new RuntimeException("페이지를 찾을 수 없습니다."));
        return convertToDTO(page);
    }
    
    /**
     * 페이지 범위로 조회합니다.
     */
    public List<BookPageDTO> getBookPagesByRange(Long userId, Long bookId, Integer startPage, Integer endPage) {
        List<BookPage> pages = bookPageRepository.findByPageRange(userId, bookId, startPage, endPage);
        return pages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 최근 페이지들을 조회합니다.
     */
    public List<BookPageDTO> getRecentPages(Long userId, Long bookId, int limit) {
        List<BookPage> pages = bookPageRepository.findRecentPagesByUserIdAndBookId(userId, bookId);
        return pages.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 페이지를 삭제합니다.
     */
    public void deleteBookPage(Long userId, Long bookId, Integer pageNumber) {
        BookPage page = bookPageRepository.findByUser_UserIdAndBook_BookIdAndPageNumber(userId, bookId, pageNumber)
                .orElseThrow(() -> new RuntimeException("페이지를 찾을 수 없습니다."));
        
        bookPageRepository.delete(page);
        log.info("페이지 삭제 완료: 사용자 {}, 책 {}, 페이지 {}", userId, bookId, pageNumber);
    }
    
    /**
     * 사용자의 총 페이지 수를 조회합니다.
     */
    public Long getTotalPageCount(Long userId, Long bookId) {
        return bookPageRepository.countByUserIdAndBookId(userId, bookId);
    }
    
    /**
     * BookPage를 DTO로 변환합니다.
     */
    private BookPageDTO convertToDTO(BookPage bookPage) {
        BookPageDTO dto = new BookPageDTO();
        dto.setPageId(bookPage.getPageId());
        dto.setBookId(bookPage.getBook().getBookId());
        dto.setUserId(bookPage.getUser().getUserId());
        dto.setPageNumber(bookPage.getPageNumber());
        dto.setImageUrl(bookPage.getImageUrl());
        dto.setCapturedAt(bookPage.getCapturedAt());
        dto.setCreatedAt(bookPage.getCreatedAt());
        dto.setConfidence(bookPage.getConfidence());
        dto.setDeviceInfo(bookPage.getDeviceInfo());
        dto.setLanguage(bookPage.getLanguage());
        dto.setNumberCount(bookPage.getNumberCount());
        return dto;
    }
}
