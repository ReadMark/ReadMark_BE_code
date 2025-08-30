package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReadingLogDTO {
	private Long userId;
	private LocalDate readDate;
	private int pagesRead;
}


