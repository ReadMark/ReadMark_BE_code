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
    
    public FavoriteQuote createFavoriteQuote(Long userId, Long bookId, Integer pageNumber, String content) {
        // User 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // Book 존재 여부 확인
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        
        // 내용 유효성 검사
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("문장 내용은 필수입니다.");
        }
        
        // 중복 확인 (같은 사용자가 같은 책의 같은 페이지에 같은 내용을 이미 즐겨찾기했는지)
        Optional<FavoriteQuote> existingQuote = favoriteQuoteRepository
                .findByUser_UserIdAndBook_BookIdAndPageNumberAndContent(userId, bookId, pageNumber, content.trim());
        
        if (existingQuote.isPresent()) {
            throw new RuntimeException("이미 즐겨찾기한 문장입니다.");
        }
        
        FavoriteQuote favoriteQuote = new FavoriteQuote();
        favoriteQuote.setUser(user);
        favoriteQuote.setBook(book);
        favoriteQuote.setPageNumber(pageNumber);
        favoriteQuote.setContent(content.trim());
        
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


