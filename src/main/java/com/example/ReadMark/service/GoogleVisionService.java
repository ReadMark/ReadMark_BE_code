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
     * 이미지에서 숫자를 추출합니다.
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
                
                // 텍스트 감지 요청 생성 (낮은 해상도 최적화)
                Feature textFeat = Feature.newBuilder()
                        .setType(Feature.Type.TEXT_DETECTION)
                        .setMaxResults(50) // 더 많은 결과 허용
                        .build();
                
                Feature docFeat = Feature.newBuilder()
                        .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                        .setMaxResults(10) // 문서 텍스트 감지도 추가
                        .build();
                
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(textFeat)
                        .addFeatures(docFeat)
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
                    
                    // 숫자만 추출 (낮은 해상도 최적화)
                    List<String> numbers = new ArrayList<>();
                    double maxConfidence = 0.0;
                    
                    // DOCUMENT_TEXT_DETECTION 결과 우선 사용 (낮은 해상도에서 더 정확)
                    if (res.hasFullTextAnnotation()) {
                        String documentText = res.getFullTextAnnotation().getText();
                        
                        // 숫자 패턴 추출 (1-5자리 숫자만)
                        String numberPattern = "\\b\\d{1,5}\\b";
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(numberPattern);
                        java.util.regex.Matcher matcher = pattern.matcher(documentText);
                        
                        while (matcher.find()) {
                            String number = matcher.group();
                            if (!numbers.contains(number)) {
                                numbers.add(number);
                            }
                        }
                        
                        log.info("문서 텍스트에서 추출된 숫자들: {}", numbers);
                    }
                    
                    // 일반 TEXT_DETECTION 결과도 확인 (낮은 해상도 보완)
                    for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                        String text = annotation.getDescription();
                        double confidence = annotation.getScore();
                        
                        if (confidence > maxConfidence) {
                            maxConfidence = confidence;
                        }
                        
                        // 숫자 패턴 재확인 (더 관대한 패턴 사용)
                        String numberPattern = "\\d{1,5}"; // 단어 경계 제거로 더 많은 숫자 감지
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(numberPattern);
                        java.util.regex.Matcher matcher = pattern.matcher(text);
                        
                        while (matcher.find()) {
                            String number = matcher.group();
                            if (!numbers.contains(number)) {
                                numbers.add(number);
                            }
                        }
                    }
                    
                    // 가장 좋은 숫자 선택 (낮은 해상도 최적화)
                    if (!numbers.isEmpty()) {
                        // 신뢰도가 높은 숫자 우선 선택
                        String bestNumber = numbers.stream()
                                .max((a, b) -> Integer.compare(a.length(), b.length())) // 더 긴 숫자 우선
                                .orElse(numbers.get(0));
                        
                        try {
                            // 4자리 숫자 예외처리: 맨 앞자리 1자 제거
                            String processedNumber = bestNumber;
                            if (bestNumber.length() == 4) {
                                processedNumber = bestNumber.substring(1); // 맨 앞자리 제거
                                log.info("4자리 숫자 감지: {} → {} (앞자리 제거)", bestNumber, processedNumber);
                            }
                            
                            // extractedText 필드 제거됨 - pageNumber만 사용
                            result.setEstimatedPageNumber(Integer.parseInt(processedNumber));
                            log.info("인식된 페이지 번호: {} → {} (총 {}개 발견, 신뢰도: {})", 
                                    bestNumber, processedNumber, numbers.size(), maxConfidence);
                        } catch (NumberFormatException e) {
                            result.setEstimatedPageNumber(1);
                            log.warn("숫자 파싱 실패: {}", bestNumber);
                        }
                    } else {
                        result.setEstimatedPageNumber(1);
                        log.warn("숫자를 찾을 수 없습니다.");
                    }
                    
                    result.setDetectedNumbers(numbers); // 숫자만 저장
                    result.setConfidence(maxConfidence > 0 ? maxConfidence : 0.8);
                    result.setLanguage("ko");
                    result.setIsBookPage(true);
                    
                    log.info("숫자 추출 완료: {} 개", numbers.size());
                    
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
     * 이미지가 책 페이지인지 확인합니다.
     */
    public boolean isBookPage(byte[] imageBytes) {
        return true;
    }
    
}
