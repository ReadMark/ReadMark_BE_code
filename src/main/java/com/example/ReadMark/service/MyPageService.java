package com.example.ReadMark.service;

import com.example.ReadMark.entity.FavoritePage;
import com.example.ReadMark.entity.FavoriteQuote;
import com.example.ReadMark.entity.ReadingLog;
import com.example.ReadMark.entity.User;
import com.example.ReadMark.entity.UserBook;
import com.example.ReadMark.model.dto.MyPageResponseDTO;
import com.example.ReadMark.repository.FavoritePageRepository;
import com.example.ReadMark.repository.FavoriteQuoteRepository;
import com.example.ReadMark.repository.ReadingLogRepository;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class MyPageService {

	private final UserRepository userRepository;
	private final UserBookRepository userBookRepository;
	private final FavoritePageRepository favoritePageRepository;
	private final FavoriteQuoteRepository favoriteQuoteRepository;
	private final ReadingLogRepository readingLogRepository;

	public MyPageResponseDTO getMyPage(Long userId) {
		Optional<User> userOpt = userRepository.findById(userId);
		if (userOpt.isEmpty()) return null;
		User user = userOpt.get();

		MyPageResponseDTO dto = new MyPageResponseDTO();
		dto.setUserId(user.getUserId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());

		// 상태별 개수 및 목록
		dto.setNowReadCount(userBookRepository.countByUser_UserIdAndStatus(userId, UserBook.Status.NOW_READ));
		dto.setWannaReadCount(userBookRepository.countByUser_UserIdAndStatus(userId, UserBook.Status.WANNA_READ));
		dto.setReadDoneCount(userBookRepository.countByUser_UserIdAndStatus(userId, UserBook.Status.READ_DONE));
		dto.setNowReadingBooks(userBookRepository.findByUser_UserIdAndStatus(userId, UserBook.Status.NOW_READ));
		dto.setWannaReadBooks(userBookRepository.findByUser_UserIdAndStatus(userId, UserBook.Status.WANNA_READ));
		dto.setReadDoneBooks(userBookRepository.findByUser_UserIdAndStatus(userId, UserBook.Status.READ_DONE));

		// 즐겨찾기
		List<FavoritePage> favPages = favoritePageRepository.findByUser_UserId(userId);
		List<FavoriteQuote> favQuotes = favoriteQuoteRepository.findByUser_UserId(userId);
		dto.setFavoritePages(favPages);
		dto.setFavoriteQuotes(favQuotes);

		// 캘린더 통계: 최장 연속일, 총 도장일수
		List<ReadingLog> allLogs = readingLogRepository.findByUser(user);
		int totalStampDays = (int) allLogs.stream().map(ReadingLog::getReadDate).distinct().count();
		int longestStreak = calculateLongestStreak(allLogs);
		dto.setTotalStampDays(totalStampDays);
		dto.setLongestStreakDays(longestStreak);

		return dto;
	}

	private int calculateLongestStreak(List<ReadingLog> logs) {
		// 날짜 Set 정렬
		Set<LocalDate> dates = new TreeSet<>(Comparator.naturalOrder());
		for (ReadingLog log : logs) {
			if (log.getReadDate() != null) {
				dates.add(log.getReadDate());
			}
		}
		int longest = 0;
		int current = 0;
		LocalDate prev = null;
		for (LocalDate date : dates) {
			if (prev == null || date.equals(prev.plusDays(1))) {
				current++;
			} else if (!date.equals(prev)) {
				current = 1;
			}
			longest = Math.max(longest, current);
			prev = date;
		}
		return longest;
	}
}


