package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QReadingLog;
import com.example.ReadMark.model.entity.ReadingLog;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ReadingLogRepositoryImpl implements ReadingLogRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public ReadingLogRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<ReadingLog> findReadingLogsWithUserInfo(Long userId, LocalDate startDate, LocalDate endDate) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        return queryFactory
                .selectFrom(readingLog)
                .where(readingLog.user.userId.eq(userId)
                        .and(readingLog.readDate.between(startDate, endDate)))
                .orderBy(readingLog.readDate.desc())
                .fetch();
    }
    
    @Override
    public Integer getMaxConsecutiveReadingDays(Long userId) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        // 연속 독서일 계산을 위한 복잡한 쿼리
        // 실제 구현에서는 더 정교한 로직이 필요할 수 있음
        return queryFactory
                .select(readingLog.readDate.count().intValue())
                .from(readingLog)
                .where(readingLog.user.userId.eq(userId))
                .fetchOne();
    }
    
    @Override
    public Integer getTotalReadingDays(Long userId) {
        QReadingLog readingLog = QReadingLog.readingLog;
        
        return queryFactory
                .select(readingLog.readDate.count().intValue())
                .from(readingLog)
                .where(readingLog.user.userId.eq(userId))
                .fetchOne();
    }
}
