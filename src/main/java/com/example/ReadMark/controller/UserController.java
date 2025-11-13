package com.example.ReadMark.controller;

import com.example.ReadMark.constant.ResponseMessage;
import com.example.ReadMark.model.dto.ApiResponse;
import com.example.ReadMark.model.dto.UserJoinDTO;
import com.example.ReadMark.model.dto.UserLoginDTO;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<User>> join(@RequestBody @Valid UserJoinDTO joinDTO) {
        try {
            User user = userService.join(joinDTO);
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_JOIN_SUCCESS, user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_LIST_SUCCESS, users, users.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USER_LIST_FAIL + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        Optional<User> user = userService.login(loginDTO);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_LOGIN_SUCCESS, user.get()));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USER_LOGIN_FAIL));
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserInfo(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_INFO_SUCCESS, userOpt.get()));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("사용자를 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("사용자 정보 조회 실패: " + e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        try {
            // 실제로는 JWT 토큰을 무효화하거나 세션을 종료하는 로직이 필요
            // 현재는 단순히 성공 응답만 반환
            return ResponseEntity.ok(ApiResponse.success("로그아웃 성공", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("로그아웃 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 로그인 상태 확인
     */
    @GetMapping("/check-login/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkLoginStatus(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("username", user.getUsername());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmail());
                userInfo.put("isLoggedIn", true);
                
                return ResponseEntity.ok(ApiResponse.success("로그인 상태 확인 성공", userInfo));
            } else {
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("isLoggedIn", false);
                errorInfo.put("message", "사용자를 찾을 수 없습니다.");
                
                return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                        .success(false)
                        .message("로그인 상태 확인 실패")
                        .data(errorInfo)
                        .build());
            }
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("isLoggedIn", false);
            errorInfo.put("message", "로그인 상태 확인 중 오류 발생: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("로그인 상태 확인 실패")
                    .data(errorInfo)
                    .build());
        }
    }
    
    /**
     * 프로필 사진 업로드
     */
    @PostMapping(value = "/{userId}/profile-image", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long userId, 
                                               @RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 이미지 유효성 검사
            if (image.isEmpty()) {
                response.put("success", false);
                response.put("message", "이미지 파일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이미지 크기 제한 (20MB)
            if (image.getSize() > 20 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "이미지 크기가 너무 큽니다. (최대 20MB)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이미지 타입 검증
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 프로필 사진 업로드 처리
            String profileImageUrl = userService.uploadProfileImage(userId, image);
            
            response.put("success", true);
            response.put("message", "프로필 사진이 업로드되었습니다.");
            response.put("profileImageUrl", profileImageUrl);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 사진 업로드 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 프로필 사진 삭제
     */
    @DeleteMapping("/{userId}/profile-image")
    public ResponseEntity<?> deleteProfileImage(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            userService.deleteProfileImage(userId);
            
            response.put("success", true);
            response.put("message", "프로필 사진이 삭제되었습니다.");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 사진 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
