package com.example.ReadMark.controller;

import com.example.ReadMark.constant.ResponseMessage;
import com.example.ReadMark.model.dto.ApiResponse;
import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.service.UserBookService;
import com.example.ReadMark.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/userbooks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class UserBookController {
    
    private final UserBookService userBookService;
    private final UserBookRepository userBookRepository;
    private final BookService bookService;
    
    /**
     * JSON 요청으로 책을 사용자에게 추가합니다.
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<ApiResponse<UserBook>> addBookToUserJson(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long bookId = Long.valueOf(request.get("bookId").toString());
            String statusStr = request.get("status").toString();
            Integer currentPage = request.get("currentPage") != null ? 
                Integer.valueOf(request.get("currentPage").toString()) : 0;
            
            // totalBook 처리 (여러 필드명 지원: totalBook, total_book, maxPage, totalPage)
            Object totalBookObj = request.get("totalBook");
            if (totalBookObj == null) {
                totalBookObj = request.get("total_book"); // snake_case
            }
            if (totalBookObj == null) {
                totalBookObj = request.get("maxPage"); // 프론트엔드에서 사용하는 필드명
            }
            if (totalBookObj == null) {
                totalBookObj = request.get("totalPage"); // 다른 가능한 필드명
            }
            Integer totalBook = totalBookObj != null ? Integer.valueOf(totalBookObj.toString()) : null;
            
            return addBookToUserInternal(userId, bookId, statusStr, currentPage, totalBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_ADD_FAIL + e.getMessage()));
        }
    }
    
    /**
     * Form-data 요청으로 책을 사용자에게 추가합니다.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserBook>> addBookToUserFormData(
            @RequestParam("userId") Long userId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("status") String statusStr,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "totalBook", required = false) Integer totalBook,
            @RequestParam(value = "total_book", required = false) Integer totalBookSnake,
            @RequestParam(value = "maxPage", required = false) Integer maxPage,
            @RequestParam(value = "totalPage", required = false) Integer totalPage) {
        try {
            if (currentPage == null) {
                currentPage = 0;
            }
            // 여러 필드명 지원: totalBook, total_book, maxPage, totalPage
            Integer finalTotalBook = totalBook != null ? totalBook : 
                                   (totalBookSnake != null ? totalBookSnake : 
                                   (maxPage != null ? maxPage : totalPage));
            return addBookToUserInternal(userId, bookId, statusStr, currentPage, finalTotalBook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_ADD_FAIL + e.getMessage()));
        }
    }
    
    /**
     * 책을 사용자에게 추가하는 공통 로직
     */
    private ResponseEntity<ApiResponse<UserBook>> addBookToUserInternal(Long userId, Long bookId, 
                                                                        String statusStr, Integer currentPage, 
                                                                        Integer totalBook) {
        try {
            // totalBook이 있으면 Book에 업데이트
            if (totalBook != null && totalBook > 0) {
                try {
                    bookService.updateBookTotalBook(bookId, totalBook);
                } catch (Exception e) {
                    // totalBook 업데이트 실패해도 계속 진행
                }
            }
            
            Status bookStatus = Status.valueOf(statusStr.toUpperCase());
            UserBook userBook = userBookService.addBookToUser(userId, bookId, bookStatus, currentPage);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_ADD_SUCCESS, userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_ADD_FAIL + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserBooks(@PathVariable Long userId) {
        try {
            List<UserBookDTO> userBooks = userBookService.getAllUserBooks(userId);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("books", userBooks);
            responseData.put("totalBooks", userBooks.size());  // 목록 조회 시 totalBooks 사용
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_LIST_SUCCESS, responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_LIST_FAIL + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<UserBookDTO>>> getUserBooksByStatus(@PathVariable Long userId, 
                                               @PathVariable String status) {
        try {
            Status bookStatus = Status.valueOf(status.toUpperCase());
            List<UserBookDTO> userBooks = userBookService.getUserBooksByStatus(userId, bookStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userBooks);  // 프론트엔드 호환성을 위해 data 필드 추가
            response.put("message", ResponseMessage.USERBOOK_LIST_SUCCESS);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_LIST_SUCCESS, userBooks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_LIST_FAIL + e.getMessage()));
        }
    }
    
    /**
     * userBookId로 특정 사용자-책 관계 조회
     */
    @GetMapping("/{userBookId}")
    public ResponseEntity<ApiResponse<UserBookDTO>> getUserBookById(@PathVariable Long userBookId) {
        try {
            UserBook userBook = userBookRepository.findById(userBookId)
                    .orElseThrow(() -> new RuntimeException("사용자-책 관계를 찾을 수 없습니다."));
            
            UserBookDTO userBookDTO = userBookService.convertToDTO(userBook);
            
            return ResponseEntity.ok(ApiResponse.success("사용자-책 관계 조회 성공", userBookDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("사용자-책 관계 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 상태별 책 카운트 조회 (NOW_READ, WANNA_READ, READ_DONE, TOTAL)
     */
    @GetMapping("/user/{userId}/counts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserBookCounts(@PathVariable Long userId) {
        try {
            Map<String, Long> counts = userBookService.getStatusCounts(userId);
            return ResponseEntity.ok(ApiResponse.success("상태별 책 카운트 조회 성공", counts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("상태별 책 카운트 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 다 읽은 책(READ_DONE) 카운트만 조회
     */
    @GetMapping("/user/{userId}/count/READ_DONE")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getReadDoneCount(@PathVariable Long userId) {
        try {
            long count = userBookService.countByStatus(userId, Status.READ_DONE);
            Map<String, Long> body = new HashMap<>();
            body.put("READ_DONE", count);
            return ResponseEntity.ok(ApiResponse.success("다 읽은 책 카운트 조회 성공", body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("다 읽은 책 카운트 조회 실패: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{userBookId}/status")
    public ResponseEntity<ApiResponse<UserBook>> updateBookStatus(@PathVariable Long userBookId, 
                                           @RequestBody Map<String, Object> request) {
        try {
            String statusStr = request.get("status").toString();
            Status bookStatus = Status.valueOf(statusStr.toUpperCase());
            UserBook userBook = userBookService.updateBookStatus(userBookId, bookStatus);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_STATUS_UPDATE_SUCCESS, userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_STATUS_UPDATE_FAIL + e.getMessage()));
        }
    }
    
    @PutMapping("/{userBookId}/page")
    public ResponseEntity<ApiResponse<UserBook>> updateCurrentPage(@PathVariable Long userBookId, 
                                            @RequestBody Map<String, Object> request) {
        try {
            Integer currentPage = Integer.valueOf(request.get("currentPage").toString());
            UserBook userBook = userBookService.updateCurrentPage(userBookId, currentPage);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_PAGE_UPDATE_SUCCESS, userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_PAGE_UPDATE_FAIL + e.getMessage()));
        }
    }
    
    /**
     * 읽고 있는 책 정보를 종합적으로 수정합니다. (JSON)
     */
    @PutMapping("/{userBookId}/edit")
    public ResponseEntity<ApiResponse<UserBook>> editUserBook(@PathVariable Long userBookId, 
                                                           @RequestBody Map<String, Object> request) {
        try {
            // 수정 가능한 필드들
            String statusStr = request.get("status") != null ? request.get("status").toString() : null;
            Integer currentPage = request.get("currentPage") != null ? 
                Integer.valueOf(request.get("currentPage").toString()) : null;
            Boolean favorite = request.get("favorite") != null ? 
                Boolean.valueOf(request.get("favorite").toString()) : null;
            
            UserBook userBook = userBookService.editUserBook(userBookId, statusStr, currentPage, favorite);
            
            // totalBook이 있으면 Book에 업데이트 (camelCase와 snake_case 둘 다 지원)
            Object totalBookObj = request.get("totalBook");
            if (totalBookObj == null) {
                totalBookObj = request.get("total_book"); // snake_case도 지원
            }
            if (totalBookObj != null) {
                try {
                    Integer totalBook = Integer.valueOf(totalBookObj.toString());
                    if (totalBook > 0) {
                        bookService.updateBookTotalBook(userBook.getBook().getBookId(), totalBook);
                        // 업데이트된 정보를 다시 가져오기
                        userBook = userBookRepository.findById(userBookId).orElse(userBook);
                    }
                } catch (Exception e) {
                    // totalBook 업데이트 실패해도 계속 진행
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success("읽고 있는 책 정보가 수정되었습니다.", userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("읽고 있는 책 정보 수정 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 책의 표지이미지, 제목, 작가, 전체 페이지 수를 수정합니다.
     */
    @PutMapping(value = "/{userBookId}/edit-book-info", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UserBook>> editBookInfo(@PathVariable Long userBookId,
                                                             @RequestParam(value = "title", required = false) String title,
                                                             @RequestParam(value = "author", required = false) String author,
                                                             @RequestParam(value = "totalBook", required = false) Integer totalBook,
                                                             @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        try {
            UserBook userBook = userBookRepository.findById(userBookId)
                    .orElseThrow(() -> new RuntimeException("사용자-책 관계를 찾을 수 없습니다."));
            
            Long bookId = userBook.getBook().getBookId();
            
            // 책 정보 수정 (제목, 작가)
            if (title != null || author != null) {
                userBookService.updateBookInfo(bookId, title, author);
            }
            
            // 전체 페이지 수 수정
            if (totalBook != null && totalBook > 0) {
                bookService.updateBookTotalBook(bookId, totalBook);
            }
            
            // 표지 이미지가 있으면 업데이트
            if (coverImage != null && !coverImage.isEmpty()) {
                String coverImageUrl = userBookService.updateBookCover(bookId, coverImage);
                userBook.getBook().setCoverImageUrl(coverImageUrl);
            }
            
            // 업데이트된 정보를 다시 가져오기
            userBook = userBookRepository.findById(userBookId).orElse(userBook);
            
            return ResponseEntity.ok(ApiResponse.success("책 정보가 수정되었습니다.", userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("책 정보 수정 실패: " + e.getMessage()));
        }
    }
    
    /**
     * CORS preflight 요청을 처리합니다.
     */
    @RequestMapping(value = "/{userBookId}/edit", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptionsRequest() {
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{userBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserBook(@PathVariable Long userBookId) {
        try {
            userBookService.deleteUserBook(userBookId);
            return ResponseEntity.ok(ApiResponse.success("사용자-책 관계가 삭제되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("사용자-책 관계 삭제 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 다 읽은 책 삭제 (상태별 삭제)
     */
    @DeleteMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<Void>> deleteUserBooksByStatus(@PathVariable Long userId, @PathVariable String status) {
        try {
            Status bookStatus = Status.valueOf(status.toUpperCase());
            userBookService.deleteUserBooksByStatus(userId, bookStatus);
            return ResponseEntity.ok(ApiResponse.success("다 읽은 책이 삭제되었습니다.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("다 읽은 책 삭제 실패: " + e.getMessage()));
        }
    }
}
