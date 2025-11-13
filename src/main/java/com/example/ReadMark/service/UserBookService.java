package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.repository.BookRepository;
import com.example.ReadMark.repository.ReadingSessionRepository;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserBookService {
    
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingSessionRepository readingSessionRepository;
    
    public UserBook addBookToUser(Long userId, Long bookId, Status status) {
        return addBookToUser(userId, bookId, status, 0);
    }
    
    public UserBook addBookToUser(Long userId, Long bookId, Status status, Integer currentPage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        
        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        userBook.setStatus(status);
        userBook.setCurrentPage(currentPage != null ? currentPage : 0);
        
        return userBookRepository.save(userBook);
    }
    
    public List<UserBookDTO> getUserBooksByStatus(Long userId, Status status) {
        List<UserBook> userBooks = userBookRepository.findUserBooksWithBookInfo(userId, status);
        return userBooks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<UserBookDTO> getAllUserBooks(Long userId) {
        List<UserBook> userBooks = userBookRepository.findUserBooksWithBookInfo(userId);
        return userBooks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public UserBook updateBookStatus(Long userBookId, Status status) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new RuntimeException("사용자 책을 찾을 수 없습니다."));
        
        userBook.setStatus(status);
        return userBookRepository.save(userBook);
    }
    
    public UserBook updateCurrentPage(Long userBookId, int currentPage) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new RuntimeException("사용자 책을 찾을 수 없습니다."));
        
        userBook.setCurrentPage(currentPage);
        return userBookRepository.save(userBook);
    }
    
    /**
     * 사용자 ID와 책 ID로 현재 페이지를 업데이트합니다.
     * ESP32-CAM에서 페이지를 촬영할 때 자동으로 호출됩니다.
     */
    public UserBook updateCurrentPageByUserIdAndBookId(Long userId, Long bookId, int currentPage) {
        Optional<UserBook> userBookOpt = userBookRepository.findByUser_UserIdAndBook_BookId(userId, bookId);
        
        if (userBookOpt.isEmpty()) {
            log.warn("사용자-책 관계를 찾을 수 없습니다. userId={}, bookId={}", userId, bookId);
            // 자동으로 사용자-책 관계 생성
            try {
                UserBook newUserBook = addBookToUser(userId, bookId, Status.NOW_READ, currentPage);
                log.info("자동으로 사용자-책 관계를 생성했습니다. userBookId={}", newUserBook.getUserBookId());
                return newUserBook;
            } catch (Exception e) {
                log.error("자동 사용자-책 관계 생성 실패: userId={}, bookId={}", userId, bookId, e);
                throw new RuntimeException("사용자-책 관계를 찾을 수 없고 생성도 실패했습니다: " + e.getMessage());
            }
        }
        
        UserBook userBook = userBookOpt.get();
        
        // 최신 페이지 번호로 업데이트 (더 큰 값만)
        if (currentPage > userBook.getCurrentPage()) {
            userBook.setCurrentPage(currentPage);
            return userBookRepository.save(userBook);
        }
        
        return userBook;
    }
    
    public void deleteUserBook(Long userBookId) {
        if (!userBookRepository.existsById(userBookId)) {
            throw new RuntimeException("사용자 책을 찾을 수 없습니다.");
        }
        userBookRepository.deleteById(userBookId);
    }
    
    public UserBookDTO convertToDTO(UserBook userBook) {
        UserBookDTO dto = new UserBookDTO();
        dto.setUserBookId(userBook.getUserBookId());
        dto.setStatus(userBook.getStatus().name());
        dto.setCurrentPage(userBook.getCurrentPage());
        dto.setCreatedAt(userBook.getCreatedAt());
        dto.setUpdatedAt(userBook.getUpdatedAt());
        
        // Book 정보도 포함
        BookDTO bookDTO = new BookDTO();
        bookDTO.setBookId(userBook.getBook().getBookId());
        bookDTO.setTitle(userBook.getBook().getTitle());
        bookDTO.setAuthor(userBook.getBook().getAuthor());
        bookDTO.setTotalBook(userBook.getBook().getTotalBook());
        
        // 이미지 URL 설정 (null이거나 빈 문자열인 경우 기본 이미지 사용)
        String coverImageUrl = userBook.getBook().getCoverImageUrl();
        if (coverImageUrl == null || coverImageUrl.trim().isEmpty()) {
            coverImageUrl = "https://via.placeholder.com/200x280/f0f0f0/666666?text=책+표지";
        }
        bookDTO.setCoverImageUrl(coverImageUrl);
        
        dto.setBook(bookDTO);
        
        // 각 책별로 읽은 시간 계산
        Long userId = userBook.getUser().getUserId();
        Long bookId = userBook.getBook().getBookId();
        String readingTime = calculateBookReadingTime(userId, bookId);
        dto.setReadingTime(readingTime);
        
        // 완독 날짜 계산 (READ_DONE 상태일 때만)
        if (userBook.getStatus() == Status.READ_DONE && userBook.getUpdatedAt() != null) {
            dto.setCompletionDate(userBook.getUpdatedAt().toLocalDate());
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
            log.warn("독서 시간 계산 실패: userId={}, bookId={}", userId, bookId, e);
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
     * 특정 상태의 책 개수 조회
     */
    public long countByStatus(Long userId, Status status) {
        return userBookRepository.countByUserIdAndStatus(userId, status);
    }

    /**
     * 상태별 책 개수 및 총합 조회
     */
    public java.util.Map<String, Long> getStatusCounts(Long userId) {
        long nowRead = countByStatus(userId, Status.NOW_READ);
        long wannaRead = countByStatus(userId, Status.WANNA_READ);
        long readDone = countByStatus(userId, Status.READ_DONE);
        long total = nowRead + wannaRead + readDone;

        java.util.Map<String, Long> counts = new java.util.LinkedHashMap<>();
        counts.put("NOW_READ", nowRead);
        counts.put("WANNA_READ", wannaRead);
        counts.put("READ_DONE", readDone);
        counts.put("TOTAL", total);
        return counts;
    }
    
    /**
     * 상태별로 사용자-책 관계를 삭제합니다.
     */
    public void deleteUserBooksByStatus(Long userId, Status status) {
        List<UserBook> userBooks = userBookRepository.findByUser_UserIdAndStatus(userId, status);
        userBookRepository.deleteAll(userBooks);
    }
    
    /**
     * 읽고 있는 책 정보를 종합적으로 수정합니다.
     */
    public UserBook editUserBook(Long userBookId, String statusStr, Integer currentPage, Boolean favorite) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new RuntimeException("사용자-책 관계를 찾을 수 없습니다."));
        
        // 상태 수정
        if (statusStr != null) {
            Status bookStatus = Status.valueOf(statusStr.toUpperCase());
            userBook.setStatus(bookStatus);
        }
        
        // 현재 페이지 수정
        if (currentPage != null) {
            userBook.setCurrentPage(currentPage);
        }
        
        // 즐겨찾기 수정
        if (favorite != null) {
            userBook.setFavorite(favorite);
        }
        
        return userBookRepository.save(userBook);
    }
    
    /**
     * 책의 기본 정보(제목, 작가)를 업데이트합니다.
     */
    public void updateBookInfo(Long bookId, String title, String author) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        
        if (title != null && !title.trim().isEmpty()) {
            book.setTitle(title.trim());
        }
        
        if (author != null && !author.trim().isEmpty()) {
            book.setAuthor(author.trim());
        }
        
        bookRepository.save(book);
    }
    
    /**
     * 책의 표지 이미지를 업데이트합니다.
     */
    public String updateBookCover(Long bookId, MultipartFile coverImage) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
            
            // 이미지 유효성 검사
            if (coverImage.isEmpty()) {
                throw new RuntimeException("표지 이미지가 필요합니다.");
            }
            
            // 파일 크기 제한 (20MB)
            if (coverImage.getSize() > 20 * 1024 * 1024) {
                throw new RuntimeException("표지 이미지 크기는 20MB를 초과할 수 없습니다.");
            }
            
            // 이미지 타입 검증
            String contentType = coverImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
            }
            
            // 업로드 디렉토리 생성
            String uploadDir = "uploads/book-covers";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            
            // 파일명 생성
            String originalFilename = coverImage.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "book_" + bookId + "_" + java.util.UUID.randomUUID().toString() + fileExtension;
            
            // 파일 저장
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            java.nio.file.Files.copy(coverImage.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // URL 생성
            String coverImageUrl = "/uploads/book-covers/" + filename;
            
            // 데이터베이스 업데이트
            book.setCoverImageUrl(coverImageUrl);
            bookRepository.save(book);
            
            return coverImageUrl;
            
        } catch (Exception e) {
            throw new RuntimeException("표지 이미지 업데이트 실패: " + e.getMessage());
        }
    }
}
