package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatsDTO {
    private int maxConsecutiveDays;
    private int totalStamps;
    private int totalReadingDays;
    private int nowReadingCount;
    private int wannaReadCount;
    private int readDoneCount;
}
