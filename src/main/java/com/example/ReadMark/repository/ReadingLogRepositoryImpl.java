package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QReadingLog;
import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.ReadingLog;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Repository
public class ReadingLogRepositoryImpl implements ReadingLogRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public ReadingLogRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<ReadingLog> findByUser_UserIdAndReadDateBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        return queryFactory
                .selectFrom(readingLog)
                .where(readingLog.user.userId.eq(userId)
                        .and(readingLog.readDate.between(startDate, endDate)))
                .orderBy(readingLog.readDate.desc())
                .fetch();
    }
    
    @Override
    public Optional<ReadingLog> findByUserIdAndDate(Long userId, LocalDate date) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        ReadingLog result = queryFactory
                .selectFrom(readingLog)
                .where(readingLog.user.userId.eq(userId)
                        .and(readingLog.readDate.eq(date)))
                .fetchOne();
        
        return Optional.ofNullable(result);
    }
    
    @Override
    public Integer sumPagesReadByUserIdAndDate(Long userId, LocalDate date) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        // Integer 필드를 직접 sum
        Integer result = queryFactory
                .select(readingLog.pagesRead.sum())
                .from(readingLog)
                .where(readingLog.user.userId.eq(userId)
                        .and(readingLog.readDate.eq(date)))
                .fetchOne();
        
        return result != null ? result : 0;
    }
    
    @Override
    public List<ReadingLog> findReadingLogsWithUserInfo(Long userId, LocalDate startDate, LocalDate endDate) {
        QReadingLog readingLog = QReadingLog.readingLog;
        QUser user = QUser.user;
        
        return queryFactory
                .selectFrom(readingLog)
                .join(readingLog.user, user).fetchJoin()
                .where(readingLog.user.userId.eq(userId)
                        .and(readingLog.readDate.between(startDate, endDate)))
                .orderBy(readingLog.readDate.desc())
                .fetch();
    }
    
    @Override
    public List<ReadingLog> findReadingLogsByPageRange(Long userId, int minPages, int maxPages) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        return queryFactory
                .selectFrom(readingLog)
                .where(readingLog.user.userId.eq(userId)
                        .and(readingLog.pagesRead.between(minPages, maxPages)))
                .orderBy(readingLog.pagesRead.desc())
                .fetch();
    }
    
    @Override
    public List<ReadingLog> findReadingLogsWithPagination(Long userId, int offset, int limit) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        return queryFactory
                .selectFrom(readingLog)
                .where(readingLog.user.userId.eq(userId))
                .orderBy(readingLog.readDate.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
    
    @Override
    public List<LocalDate> findDistinctReadingDates(Long userId) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        return queryFactory
                .select(readingLog.readDate)
                .from(readingLog)
                .where(readingLog.user.userId.eq(userId))
                .distinct()
                .orderBy(readingLog.readDate.asc())
                .fetch();
    }
    
    @Override
    public Integer getMaxConsecutiveReadingDays(Long userId) {
        List<LocalDate> readingDates = findDistinctReadingDates(userId);
        
        if (readingDates.isEmpty()) {
            return 0;
        }
        
        int maxConsecutive = 1;
        int currentConsecutive = 1;
        
        for (int i = 1; i < readingDates.size(); i++) {
            LocalDate prevDate = readingDates.get(i - 1);
            LocalDate currentDate = readingDates.get(i);
            
            if (ChronoUnit.DAYS.between(prevDate, currentDate) == 1) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 1;
            }
        }
        
        return maxConsecutive;
    }
    
    @Override
    public Integer getTotalReadingDays(Long userId) {
        return findDistinctReadingDates(userId).size();
    }
}
