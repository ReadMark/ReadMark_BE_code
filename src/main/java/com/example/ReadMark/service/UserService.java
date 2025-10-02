package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.UserJoinDTO;
import com.example.ReadMark.model.dto.UserLoginDTO;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public User join(UserJoinDTO joinDTO) {
        // 필수 필드 null 체크
        if (joinDTO.getName() == null || joinDTO.getName().trim().isEmpty()) {
            throw new RuntimeException("이름은 필수입니다.");
        }
        if (joinDTO.getUsername() == null || joinDTO.getUsername().trim().isEmpty()) {
            throw new RuntimeException("사용자명은 필수입니다.");
        }
        if (joinDTO.getEmail() == null || joinDTO.getEmail().trim().isEmpty()) {
            throw new RuntimeException("이메일은 필수입니다.");
        }
        if (joinDTO.getPassword() == null || joinDTO.getPassword().trim().isEmpty()) {
            throw new RuntimeException("비밀번호는 필수입니다.");
        }

        // 이메일 중복 체크
        if (userRepository.existsByEmail(joinDTO.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // username 중복 체크
        if (userRepository.existsByUsername(joinDTO.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }

        User user = new User();
        user.setName(joinDTO.getName().trim());
        user.setUsername(joinDTO.getUsername().trim());
        user.setEmail(joinDTO.getEmail().trim());
        user.setPassword(joinDTO.getPassword()); // 실제로는 암호화 필요

        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> login(UserLoginDTO loginDTO) {
        // 필수 필드 null 체크
        if (loginDTO.getUsername() == null || loginDTO.getUsername().trim().isEmpty()) {
            throw new RuntimeException("사용자명은 필수입니다.");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
            throw new RuntimeException("비밀번호는 필수입니다.");
        }
        
        // username으로 로그인
        return userRepository.findByUsernameAndPassword(loginDTO.getUsername().trim(), loginDTO.getPassword());
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByEmailWithUserBooks(String email) {
        return userRepository.findByEmailWithUserBooks(email);
    }
    
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 프로필 이미지 업로드
     */
    public String uploadProfileImage(Long userId, MultipartFile image) throws IOException {
        System.out.println("=== 프로필 이미지 업로드 시작 ===");
        System.out.println("사용자 ID: " + userId);
        System.out.println("이미지 크기: " + image.getSize() + " bytes");
        System.out.println("이미지 타입: " + image.getContentType());
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        System.out.println("사용자 찾음: " + user.getUsername());
        
        // 업로드 디렉토리 생성
        String uploadDir = "uploads/profile-images";
        Path uploadPath = Paths.get(uploadDir);
        System.out.println("업로드 경로: " + uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("디렉토리 생성됨: " + uploadPath);
        } else {
            System.out.println("디렉토리 이미 존재: " + uploadPath);
        }
        
        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = image.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = "profile_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
        System.out.println("생성된 파일명: " + filename);
        
        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        System.out.println("파일 저장 경로: " + filePath.toAbsolutePath());
        
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("파일 저장 완료");
        
        // 프로필 이미지 URL 생성
        String profileImageUrl = "/uploads/profile-images/" + filename;
        System.out.println("생성된 URL: " + profileImageUrl);
        
        // 사용자 정보 업데이트
        user.setProfileImageUrl(profileImageUrl);
        userRepository.save(user);
        System.out.println("사용자 정보 업데이트 완료");
        
        System.out.println("=== 프로필 이미지 업로드 완료 ===");
        return profileImageUrl;
    }
    
    /**
     * 프로필 이미지 삭제
     */
    public void deleteProfileImage(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 기존 프로필 이미지 파일 삭제
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                String filename = user.getProfileImageUrl().substring(user.getProfileImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get("uploads/profile-images/" + filename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // 파일 삭제 실패는 무시 (로그만 출력)
                System.err.println("프로필 이미지 파일 삭제 실패: " + e.getMessage());
            }
        }
        
        // 사용자 정보에서 프로필 이미지 URL 제거
        user.setProfileImageUrl(null);
        userRepository.save(user);
    }
}
