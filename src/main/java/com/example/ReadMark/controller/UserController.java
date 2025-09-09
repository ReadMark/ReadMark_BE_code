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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<User>> join(@RequestBody UserJoinDTO joinDTO) {
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
    public ResponseEntity<ApiResponse<User>> login(@RequestBody UserLoginDTO loginDTO) {
        Optional<User> user = userService.login(loginDTO);
        
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USER_LOGIN_SUCCESS, user.get()));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USER_LOGIN_FAIL));
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> getUserInfo(@PathVariable Long userId) {
        // 실제 구현에서는 인증된 사용자만 접근 가능하도록 수정 필요
        return ResponseEntity.ok().build();
    }
}
