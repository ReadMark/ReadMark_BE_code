package com.example.ReadMark.controller;

import com.example.ReadMark.entity.ReadingLog;
import com.example.ReadMark.model.dto.ReadingLogDTO;
import com.example.ReadMark.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reading-logs")
@RequiredArgsConstructor
public class ReadingLogController {

	private final ReadingLogService readingLogService;

	@PostMapping
	public ReadingLog create(@RequestBody ReadingLogDTO dto) {
		return readingLogService.create(dto);
	}

	@GetMapping("/user/{userId}")
	public List<ReadingLog> getUserLogs(
			@PathVariable Long userId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
	) {
		return readingLogService.findByUserAndRange(userId, from, to);
	}
}


