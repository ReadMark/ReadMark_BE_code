package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.UserJoinDTO;
import com.example.ReadMark.model.dto.UserLoginDTO;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody UserJoinDTO joinDTO) {
        try {
            User user = userService.join(joinDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("userId", user.getUserId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO loginDTO) {
        Optional<User> user = userService.login(loginDTO);
        
        if (user.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인이 완료되었습니다.");
            response.put("userId", user.get().getUserId());
            response.put("name", user.get().getName());
            response.put("email", user.get().getEmail());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId) {
        // 실제 구현에서는 인증된 사용자만 접근 가능하도록 수정 필요
        return ResponseEntity.ok().build();
    }
}
