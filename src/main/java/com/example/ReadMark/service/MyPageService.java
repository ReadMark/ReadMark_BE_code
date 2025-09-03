package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.FavoritePageDTO;
import com.example.ReadMark.model.dto.FavoriteQuoteDTO;
import com.example.ReadMark.model.dto.UserStatsDTO;
import com.example.ReadMark.model.entity.FavoritePage;
import com.example.ReadMark.model.entity.FavoriteQuote;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.repository.FavoritePageRepository;
import com.example.ReadMark.repository.FavoriteQuoteRepository;
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
    private final UserBookRepository userBookRepository;
    private final FavoritePageRepository favoritePageRepository;
    private final FavoriteQuoteRepository favoriteQuoteRepository;
    private final ReadingLogService readingLogService;
    
    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO stats = new UserStatsDTO();
        
        // 각 상태별 책 개수
        stats.setNowReadingCount((int) userBookRepository.countByUserIdAndStatus(userId, Status.NOW_READ));
        stats.setWannaReadCount((int) userBookRepository.countByUserIdAndStatus(userId, Status.WANNA_READ));
        stats.setReadDoneCount((int) userBookRepository.countByUserIdAndStatus(userId, Status.READ_DONE));
        
        // 독서 통계
        stats.setMaxConsecutiveDays(readingLogService.getMaxConsecutiveReadingDays(userId));
        stats.setTotalReadingDays(readingLogService.getTotalReadingDays(userId));
        
        // 도장 개수 (독서일 수와 동일하게 설정)
        stats.setTotalStamps(stats.getTotalReadingDays());
        
        return stats;
    }
    
    public List<FavoritePageDTO> getFavoritePages(Long userId) {
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
        // 실제 구현에서는 Book과 User 존재 여부 확인 필요
        FavoritePage favoritePage = new FavoritePage();
        // ... 구현 필요
        return favoritePageRepository.save(favoritePage);
    }
    
    public FavoriteQuote createFavoriteQuote(Long userId, Long bookId, Integer pageNumber, String content) {
        // 실제 구현에서는 Book과 User 존재 여부 확인 필요
        FavoriteQuote favoriteQuote = new FavoriteQuote();
        // ... 구현 필요
        return favoriteQuoteRepository.save(favoriteQuote);
    }
    
    private FavoritePageDTO convertToFavoritePageDTO(FavoritePage favoritePage) {
        FavoritePageDTO dto = new FavoritePageDTO();
        dto.setFavPageId(favoritePage.getFavPageId());
        dto.setPageNumber(favoritePage.getPageNumber());
        dto.setCreatedAt(favoritePage.getCreatedAt());
        
        // Book 정보도 포함
        BookDTO bookDTO = new BookDTO();
        bookDTO.setBookId(favoritePage.getBook().getBookId());
        bookDTO.setTitle(favoritePage.getBook().getTitle());
        bookDTO.setAuthor(favoritePage.getBook().getAuthor());
        bookDTO.setPublisher(favoritePage.getBook().getPublisher());
        bookDTO.setCoverImageUrl(favoritePage.getBook().getCoverImageUrl());
        bookDTO.setPublishedAt(favoritePage.getBook().getPublishedAt());
        
        dto.setBook(bookDTO);
        return dto;
    }
    
    private FavoriteQuoteDTO convertToFavoriteQuoteDTO(FavoriteQuote favoriteQuote) {
        FavoriteQuoteDTO dto = new FavoriteQuoteDTO();
        dto.setFavQuoteId(favoriteQuote.getFavQuoteId());
        dto.setPageNumber(favoriteQuote.getPageNumber());
        dto.setContent(favoriteQuote.getContent());
        dto.setCreatedAt(favoriteQuote.getCreatedAt());
        
        // Book 정보도 포함
        BookDTO bookDTO = new BookDTO();
        bookDTO.setBookId(favoriteQuote.getBook().getBookId());
        bookDTO.setTitle(favoriteQuote.getBook().getTitle());
        bookDTO.setAuthor(favoriteQuote.getBook().getAuthor());
        bookDTO.setPublisher(favoriteQuote.getBook().getPublisher());
        bookDTO.setCoverImageUrl(favoriteQuote.getBook().getCoverImageUrl());
        bookDTO.setPublishedAt(favoriteQuote.getBook().getPublishedAt());
        
        dto.setBook(bookDTO);
        return dto;
    }
}


