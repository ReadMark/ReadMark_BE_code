package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class ImageUploadDTO {
    
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
    
    @NotNull(message = "책 ID는 필수입니다.")
    private Long bookId;
    
    @NotNull(message = "이미지는 필수입니다.")
    private MultipartFile image;
    
    private String deviceInfo; // 임베디드 기기 정보
    private String captureTime; // 촬영 시간 (ISO 8601 형식)
}
