package com.example.ReadMark.controller;

import com.example.ReadMark.entity.User;
import com.example.ReadMark.model.dto.LoginRequestDTO;
import com.example.ReadMark.model.dto.LoginResponseDTO;
import com.example.ReadMark.model.dto.UserDTO;
import com.example.ReadMark.model.dto.MyPageResponseDTO;
import com.example.ReadMark.service.UserService;
import com.example.ReadMark.service.MyPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MyPageService myPageService;

    // 사용자 생성
    @PostMapping
    public User createUser(@RequestBody UserDTO dto) {
        return userService.createUser(dto);
    }

    // 전체 사용자 조회
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 로그인 (단순 비교, 인증/인가 없음)
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return userService.login(request);
    }

    // 마이페이지 통합 응답
    @GetMapping("/{id}/mypage")
    public MyPageResponseDTO getMyPage(@PathVariable Long id) {
        return myPageService.getMyPage(id);
    }

    // 사용자 업데이트
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        return userService.updateUser(id, dto);
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
