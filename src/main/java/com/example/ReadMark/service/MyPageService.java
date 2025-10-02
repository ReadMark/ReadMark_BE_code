package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.model.dto.FavoritePageDTO;
import com.example.ReadMark.model.dto.FavoriteQuoteDTO;
import com.example.ReadMark.model.dto.UserStatsDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.model.entity.FavoritePage;
import com.example.ReadMark.model.entity.FavoriteQuote;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.repository.BookRepository;
import com.example.ReadMark.repository.FavoritePageRepository;
import com.example.ReadMark.repository.FavoriteQuoteRepository;
import com.example.ReadMark.repository.ReadingSessionRepository;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final FavoritePageRepository favoritePageRepository;
    private final FavoriteQuoteRepository favoriteQuoteRepository;
    private final ReadingLogService readingLogService;
    private final ReadingSessionRepository readingSessionRepository;
    
    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO stats = new UserStatsDTO();
        
        // 각 상태별 책 개수
        stats.setNowReadingCount((int) userBookRepository.countByUserIdAndStatus(userId, Status.NOW_READ));
        stats.setWannaReadCount((int) userBookRepository.countByUserIdAndStatus(userId, Status.WANNA_READ));
        stats.setReadDoneCount((int) userBookRepository.countByUserIdAndStatus(userId, Status.READ_DONE));
        
        // 독서 통계
        stats.setMaxConsecutiveDays(readingLogService.getMaxConsecutiveReadingDays(userId));
        stats.setTotalReadingDays(readingLogService.getTotalReadingDays(userId));
        
        // 도장 개수 (20페이지 이상 읽은 날 수)
        stats.setTotalStamps(readingLogService.getTotalStampDaysWithValidation(userId).intValue());
        
        return stats;
    }
    
    public List<FavoritePageDTO> getFavoritePages(Long userId) {
        // fetchJoin을 사용하여 Book과 User 정보를 함께 조회
        List<FavoritePage> favoritePages = favoritePageRepository.findFavoritePagesWithBookInfo(userId);
        return favoritePages.stream()
                .map(this::convertToFavoritePageDTO)
                .collect(Collectors.toList());
    }
    
    public List<FavoriteQuoteDTO> getFavoriteQuotes(Long userId) {
        List<FavoriteQuote> favoriteQuotes = favoriteQuoteRepository.findFavoriteQuotesWithBookInfo(userId);
        return favoriteQuotes.stream()
                .map(this::convertToFavoriteQuoteDTO)
                .collect(Collectors.toList());
    }
    
    public FavoritePage createFavoritePage(Long userId, Long bookId, int pageNumber) {
        // User 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // Book 존재 여부 확인
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        
        // 중복 확인 (같은 사용자가 같은 책의 같은 페이지를 이미 즐겨찾기했는지)
        Optional<FavoritePage> existingFavorite = favoritePageRepository
                .findByUser_UserIdAndBook_BookIdAndPageNumber(userId, bookId, pageNumber);
        
        if (existingFavorite.isPresent()) {
            throw new RuntimeException("이미 즐겨찾기한 페이지입니다.");
        }
        
        FavoritePage favoritePage = new FavoritePage();
        favoritePage.setUser(user);
        favoritePage.setBook(book);
        favoritePage.setPageNumber(pageNumber);
        
        return favoritePageRepository.save(favoritePage);
    }
    
    public FavoriteQuote createFavoriteQuote(Long userId, Integer pageNumber, String content, String bookTitle, String coverImageUrl) {
        // User 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 내용 유효성 검사
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("문장 내용은 필수입니다.");
        }
        
        // 중복 확인 (같은 사용자가 같은 페이지에 같은 내용을 이미 즐겨찾기했는지)
        Optional<FavoriteQuote> existingQuote = favoriteQuoteRepository
                .findByUser_UserIdAndPageNumberAndContent(userId, pageNumber, content.trim());
        
        if (existingQuote.isPresent()) {
            throw new RuntimeException("이미 즐겨찾기한 문장입니다.");
        }
        
        // 실제 책의 표지 이미지 조회
        String finalCoverImageUrl = getBookCoverImageUrl(userId, bookTitle, coverImageUrl);
        
        FavoriteQuote favoriteQuote = new FavoriteQuote();
        favoriteQuote.setUser(user);
        favoriteQuote.setPageNumber(pageNumber);
        favoriteQuote.setContent(content.trim());
        favoriteQuote.setBookTitle(bookTitle);
        favoriteQuote.setCoverImageUrl(finalCoverImageUrl);
        
        System.out.println("DEBUG: Final coverImageUrl = " + finalCoverImageUrl);
        
        return favoriteQuoteRepository.save(favoriteQuote);
    }
    
    public FavoritePage updateFavoritePage(Long favPageId, Integer pageNumber) {
        FavoritePage favoritePage = favoritePageRepository.findById(favPageId)
                .orElseThrow(() -> new RuntimeException("즐겨찾기한 페이지를 찾을 수 없습니다."));
        
        if (pageNumber != null) {
            favoritePage.setPageNumber(pageNumber);
        }
        
        return favoritePageRepository.save(favoritePage);
    }
    
    public FavoriteQuote updateFavoriteQuote(Long favQuoteId, Integer pageNumber, String content, String bookTitle, String coverImageUrl) {
        FavoriteQuote favoriteQuote = favoriteQuoteRepository.findById(favQuoteId)
                .orElseThrow(() -> new RuntimeException("즐겨찾기한 문장을 찾을 수 없습니다."));
        
        if (pageNumber != null) {
            favoriteQuote.setPageNumber(pageNumber);
        }
        
        if (content != null && !content.trim().isEmpty()) {
            favoriteQuote.setContent(content.trim());
        }
        
        if (bookTitle != null && !bookTitle.trim().isEmpty()) {
            favoriteQuote.setBookTitle(bookTitle.trim());
        }
        
        if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            favoriteQuote.setCoverImageUrl(coverImageUrl.trim());
        }
        
        return favoriteQuoteRepository.save(favoriteQuote);
    }
    
    public void deleteFavoritePage(Long favPageId) {
        if (!favoritePageRepository.existsById(favPageId)) {
            throw new RuntimeException("즐겨찾기한 페이지를 찾을 수 없습니다.");
        }
        favoritePageRepository.deleteById(favPageId);
    }
    
    public void deleteFavoriteQuote(Long favQuoteId) {
        if (!favoriteQuoteRepository.existsById(favQuoteId)) {
            throw new RuntimeException("즐겨찾기한 문장을 찾을 수 없습니다.");
        }
        favoriteQuoteRepository.deleteById(favQuoteId);
    }
    
    private FavoritePageDTO convertToFavoritePageDTO(FavoritePage favoritePage) {
        FavoritePageDTO dto = new FavoritePageDTO();
        dto.setFavPageId(favoritePage.getFavPageId());
        dto.setBookTitle(favoritePage.getBook().getTitle());
        dto.setAuthor(favoritePage.getBook().getAuthor());
        dto.setPageNumber(favoritePage.getPageNumber());
        dto.setCoverImageUrl(favoritePage.getBook().getCoverImageUrl());
        dto.setCreatedAt(favoritePage.getCreatedAt());
        return dto;
    }
    
    private FavoriteQuoteDTO convertToFavoriteQuoteDTO(FavoriteQuote favoriteQuote) {
        FavoriteQuoteDTO dto = new FavoriteQuoteDTO();
        dto.setFavQuoteId(favoriteQuote.getFavQuoteId());
        dto.setPageNumber(favoriteQuote.getPageNumber());        // 즐겨찾기 페이지
        dto.setContent(favoriteQuote.getContent());              // 내용
        dto.setCreatedAt(favoriteQuote.getCreatedAt());          // 저장날짜
        dto.setBookTitle(favoriteQuote.getBookTitle());          // 책 제목
        dto.setCoverImageUrl(favoriteQuote.getCoverImageUrl());  // 개별 표지 이미지 URL
        
        // 책 제목이 없는 경우 기본값 설정
        if (favoriteQuote.getBookTitle() == null || favoriteQuote.getBookTitle().trim().isEmpty()) {
            dto.setBookTitle("알 수 없는 책");
        }
        
        return dto;
    }
    
    
    /**
     * 특정 사용자가 특정 책을 읽은 총 시간을 계산합니다.
     */
    private String calculateBookReadingTime(Long userId, Long bookId) {
        try {
            // 최적화된 쿼리로 총 독서 시간을 직접 조회
            Long totalMinutes = readingSessionRepository.getTotalReadingMinutesByUserIdAndBookId(userId, bookId);
            
            if (totalMinutes == null || totalMinutes == 0) {
                return "0m";
            }
            
            // 시간 포맷팅 (예: 72분 -> 1h12m, 45분 -> 45m)
            return formatReadingTime(totalMinutes);
            
        } catch (Exception e) {
            return "0m";
        }
    }
    
    /**
     * 독서 시간을 포맷팅합니다 (예: 72분 -> 1h12m, 45분 -> 45m)
     */
    private String formatReadingTime(long totalMinutes) {
        if (totalMinutes < 60) {
            return totalMinutes + "m";
        } else {
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h" + minutes + "m";
            }
        }
    }
    
    /**
     * 즐겨찾기 문장 ID로 조회
     */
    public FavoriteQuote getFavoriteQuoteById(Long favQuoteId) {
        return favoriteQuoteRepository.findById(favQuoteId).orElse(null);
    }
    
    /**
     * 책의 표지 이미지 URL을 조회합니다.
     * 우선순위: 1) 업로드된 이미지, 2) 실제 책의 표지 이미지, 3) 기본 이미지
     */
    private String getBookCoverImageUrl(Long userId, String bookTitle, String uploadedCoverImageUrl) {
        // 1. 업로드된 이미지가 있으면 우선 사용
        if (uploadedCoverImageUrl != null && !uploadedCoverImageUrl.trim().isEmpty()) {
            return uploadedCoverImageUrl.trim();
        }
        
        // 2. 실제 책의 표지 이미지 조회
        try {
            // 사용자가 등록한 책 중에서 제목이 일치하는 책 찾기
            List<UserBook> userBooks = userBookRepository.findByUser_UserId(userId);
            for (UserBook userBook : userBooks) {
                if (userBook.getBook().getTitle().equals(bookTitle)) {
                    String bookCoverUrl = userBook.getBook().getCoverImageUrl();
                    if (bookCoverUrl != null && !bookCoverUrl.trim().isEmpty()) {
                        return bookCoverUrl.trim();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("책 표지 이미지 조회 중 오류: " + e.getMessage());
        }
        
        // 3. 기본 이미지 사용
        return "/images/default-book-cover.jpg";
    }
}


