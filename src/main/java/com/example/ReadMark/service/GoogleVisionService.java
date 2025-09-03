package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.VisionAnalysisResultDTO;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleVisionService {
    
    /**
     * 이미지에서 텍스트를 추출합니다.
     */
    public VisionAnalysisResultDTO extractTextFromImage(byte[] imageBytes) {
        VisionAnalysisResultDTO result = new VisionAnalysisResultDTO();
        result.setAnalysisTime(LocalDateTime.now());
        
        try {
            // Vision API 클라이언트 생성
            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                
                // 이미지 데이터를 ByteString으로 변환
                ByteString imgBytes = ByteString.copyFrom(imageBytes);
                
                // 이미지 객체 생성
                Image img = Image.newBuilder().setContent(imgBytes).build();
                
                // 텍스트 감지 요청 생성
                Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feat)
                        .setImage(img)
                        .build();
                
                // API 호출
                BatchAnnotateImagesResponse response = client.batchAnnotateImages(
                        List.of(request));
                
                // 결과 처리
                List<AnnotateImageResponse> responses = response.getResponsesList();
                
                if (!responses.isEmpty()) {
                    AnnotateImageResponse res = responses.get(0);
                    
                    if (res.hasError()) {
                        result.setErrorMessage("Vision API 오류: " + res.getError().getMessage());
                        return result;
                    }
                    
                    // 텍스트 추출
                    StringBuilder fullText = new StringBuilder();
                    List<String> words = new ArrayList<>();
                    
                    for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                        String text = annotation.getDescription();
                        if (fullText.length() == 0) {
                            // 첫 번째 annotation은 전체 텍스트
                            fullText.append(text);
                        } else {
                            // 나머지는 개별 단어들
                            words.add(text);
                        }
                    }
                    
                    result.setExtractedText(fullText.toString());
                    result.setDetectedWords(words);
                    result.setConfidence(0.9); // 기본 신뢰도
                    result.setLanguage("ko"); // 한국어로 가정
                    result.setIsBookPage(true);
                    
                    // 페이지 번호 추정 (텍스트에서 숫자 패턴 찾기)
                    result.setEstimatedPageNumber(extractPageNumber(fullText.toString()));
                    
                    log.info("텍스트 추출 완료: {} 단어", words.size());
                    
                } else {
                    result.setErrorMessage("텍스트를 찾을 수 없습니다.");
                }
                
            }
            
        } catch (IOException e) {
            log.error("Vision API 호출 중 오류 발생", e);
            result.setErrorMessage("Vision API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("텍스트 추출 중 예상치 못한 오류 발생", e);
            result.setErrorMessage("텍스트 추출 실패: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 텍스트에서 페이지 번호를 추출합니다.
     */
    private Integer extractPageNumber(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // 페이지 번호 패턴 찾기 (우선순위 순)
        String[] patterns = {
            // 명시적 페이지 표시
            "p\\.\\s*(\\d+)",           // p.123
            "페이지\\s*(\\d+)",          // 페이지 123
            "(\\d+)\\s*페이지",          // 123페이지
            "page\\s*(\\d+)",           // page 123
            "(\\d+)\\s*page",           // 123 page
            
            // 챕터/절 표시
            "제\\s*(\\d+)\\s*장",        // 제 1 장
            "chapter\\s*(\\d+)",        // chapter 1
            "chap\\.\\s*(\\d+)",        // chap. 1
            
            // 일반적인 숫자 (1-4자리, 페이지 번호로 추정)
            "\\b(\\d{1,4})\\b"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, 
                java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                try {
                    int pageNum = Integer.parseInt(m.group(1));
                    
                    // 페이지 번호로 적절한지 검증
                    if (isValidPageNumber(pageNum, text)) {
                        return pageNum;
                    }
                } catch (NumberFormatException e) {
                    // 숫자 변환 실패 시 계속 진행
                }
            }
        }
        
        return null;
    }
    
    /**
     * 추출된 숫자가 페이지 번호로 적절한지 검증합니다.
     */
    private boolean isValidPageNumber(int number, String text) {
        // 0보다 커야 함
        if (number <= 0) return false;
        
        // 너무 큰 페이지 번호는 제외 (일반적으로 1000페이지 이하)
        if (number > 1000) return false;
        
        // 텍스트에서 해당 숫자 주변의 컨텍스트 확인
        String numberStr = String.valueOf(number);
        int index = text.indexOf(numberStr);
        
        if (index >= 0) {
            // 숫자 앞뒤로 페이지 관련 키워드가 있는지 확인
            String before = index > 0 ? text.substring(Math.max(0, index - 10), index) : "";
            String after = index + numberStr.length() < text.length() ? 
                          text.substring(index + numberStr.length(), 
                                       Math.min(text.length(), index + numberStr.length() + 10)) : "";
            
            String context = (before + after).toLowerCase();
            
            // 페이지 관련 키워드가 있으면 더 신뢰할 수 있음
            if (context.contains("페이지") || context.contains("page") || 
                context.contains("장") || context.contains("chapter")) {
                return true;
            }
            
            // 숫자만 있는 경우, 텍스트 길이와 비교하여 판단
            if (text.length() > 100) { // 충분한 텍스트가 있는 경우
                return true;
            }
        }
        
        return true; // 기본적으로 허용
    }
    
    /**
     * 이미지가 책 페이지인지 확인합니다.
     */
    public boolean isBookPage(byte[] imageBytes) {
        try {
            VisionAnalysisResultDTO result = extractTextFromImage(imageBytes);
            
            if (!result.isSuccess()) {
                return false;
            }
            
            String text = result.getExtractedText().toLowerCase();
            
            // 책 페이지 특징 확인
            boolean hasText = text.length() > 50; // 충분한 텍스트
            boolean hasPageNumber = result.getEstimatedPageNumber() != null;
            boolean hasBookLikeContent = text.contains("장") || text.contains("chapter") || 
                                       text.contains("절") || text.contains("section") ||
                                       text.matches(".*[가-힣]{10,}.*"); // 한글 문장
            
            return hasText && (hasPageNumber || hasBookLikeContent);
            
        } catch (Exception e) {
            log.error("책 페이지 확인 중 오류", e);
            return false;
        }
    }
    
    /**
     * 추출된 텍스트의 품질을 평가합니다.
     */
    public double evaluateTextQuality(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        double quality = 0.0;
        
        // 텍스트 길이 점수 (0-30점)
        int length = text.length();
        if (length > 100) quality += 30;
        else if (length > 50) quality += 20;
        else if (length > 20) quality += 10;
        
        // 한글 비율 점수 (0-40점)
        long koreanChars = text.chars().filter(ch -> ch >= 0xAC00 && ch <= 0xD7AF).count();
        double koreanRatio = (double) koreanChars / length;
        quality += koreanRatio * 40;
        
        // 문장 구조 점수 (0-30점)
        String[] sentences = text.split("[.!?。]");
        if (sentences.length > 1) quality += 30;
        else if (sentences.length == 1 && sentences[0].length() > 10) quality += 20;
        
        return Math.min(100.0, quality);
    }
}
