package com.example.ReadMark.service;

import com.example.ReadMark.entity.User;
import com.example.ReadMark.model.dto.UserDTO;
import com.example.ReadMark.model.dto.LoginRequestDTO;
import com.example.ReadMark.model.dto.LoginResponseDTO;
import com.example.ReadMark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 사용자 생성
    public User createUser(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        return userRepository.save(user);
    }

    // 전체 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 사용자 업데이트
    public User updateUser(Long id, UserDTO dto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword());
            return userRepository.save(user);
        }
        return null;
    }

    // 사용자 삭제
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 로그인 (단순 비교, 암호화/인증 미사용)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<User> optional = userRepository.findByEmail(request.getEmail());
        if (optional.isPresent()) {
            User user = optional.get();
            if (user.getPassword().equals(request.getPassword())) {
                return new LoginResponseDTO(user.getUserId(), user.getName(), user.getEmail());
            }
        }
        return null;
    }
}
