package com.example.ReadMark.model.dto;

import com.example.ReadMark.entity.FavoritePage;
import com.example.ReadMark.entity.FavoriteQuote;
import com.example.ReadMark.entity.UserBook;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyPageResponseDTO {
	// 기본 정보
	private Long userId;
	private String name;
	private String email;

	// 책 상태별 개수
	private long nowReadCount;
	private long wannaReadCount;
	private long readDoneCount;

	// 통계
	private int longestStreakDays;
	private int totalStampDays;

	// 즐겨찾기 섹션
	private List<FavoritePage> favoritePages;
	private List<FavoriteQuote> favoriteQuotes;

	// 섹션별 샘플 목록 (필요시 확장)
	private List<UserBook> nowReadingBooks;
	private List<UserBook> wannaReadBooks;
	private List<UserBook> readDoneBooks;
}


