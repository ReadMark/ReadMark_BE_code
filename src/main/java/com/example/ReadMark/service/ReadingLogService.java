package com.example.ReadMark.service;

import com.example.ReadMark.entity.ReadingLog;
import com.example.ReadMark.entity.User;
import com.example.ReadMark.model.dto.ReadingLogDTO;
import com.example.ReadMark.repository.ReadingLogRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReadingLogService {

	private final ReadingLogRepository readingLogRepository;
	private final UserRepository userRepository;

	public ReadingLog create(ReadingLogDTO dto) {
		Optional<User> userOpt = userRepository.findById(dto.getUserId());
		if (userOpt.isEmpty()) return null;
		ReadingLog log = new ReadingLog();
		log.setUser(userOpt.get());
		log.setReadDate(dto.getReadDate());
		log.setPagesRead(dto.getPagesRead());
		return readingLogRepository.save(log);
	}

	public List<ReadingLog> findByUserAndRange(Long userId, LocalDate from, LocalDate to) {
		Optional<User> userOpt = userRepository.findById(userId);
		if (userOpt.isEmpty()) return List.of();
		User user = userOpt.get();
		if (from != null && to != null) {
			return readingLogRepository.findByUserAndReadDateBetween(user, from, to);
		}
		return readingLogRepository.findByUser(user);
	}
}


