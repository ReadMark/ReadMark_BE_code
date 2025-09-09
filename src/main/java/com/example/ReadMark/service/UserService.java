package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.UserJoinDTO;
import com.example.ReadMark.model.dto.UserLoginDTO;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public User join(UserJoinDTO joinDTO) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(joinDTO.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        User user = new User();
        user.setName(joinDTO.getName());
        user.setEmail(joinDTO.getEmail());
        user.setPassword(joinDTO.getPassword()); // 실제로는 암호화 필요
        
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> login(UserLoginDTO loginDTO) {
        return userRepository.findByEmailAndPassword(loginDTO.getEmail(), loginDTO.getPassword());
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByEmailWithUserBooks(String email) {
        return userRepository.findByEmailWithUserBooks(email);
    }
}
