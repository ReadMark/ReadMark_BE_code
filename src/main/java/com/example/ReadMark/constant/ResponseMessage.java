package com.example.ReadMark.constant;

public final class ResponseMessage {
    
    // 사용자 관련 메시지
    public static final String USER_JOIN_SUCCESS = "회원가입이 완료되었습니다.";
    public static final String USER_LOGIN_SUCCESS = "로그인이 완료되었습니다.";
    public static final String USER_LOGIN_FAIL = "이메일 또는 비밀번호가 올바르지 않습니다.";
    public static final String USER_LIST_SUCCESS = "사용자 목록을 조회했습니다.";
    public static final String USER_LIST_FAIL = "사용자 목록 조회에 실패했습니다: ";
    
    // 책 관련 메시지
    public static final String BOOK_ADD_SUCCESS = "책이 등록되었습니다.";
    public static final String BOOK_ADD_FAIL = "책 등록에 실패했습니다: ";
    
    // 내 책장 관련 메시지
    public static final String USERBOOK_ADD_SUCCESS = "책이 내 책장에 추가되었습니다.";
    public static final String USERBOOK_ADD_FAIL = "책 추가에 실패했습니다: ";
    public static final String USERBOOK_STATUS_UPDATE_SUCCESS = "책 상태가 업데이트되었습니다.";
    public static final String USERBOOK_STATUS_UPDATE_FAIL = "책 상태 업데이트에 실패했습니다: ";
    public static final String USERBOOK_PAGE_UPDATE_SUCCESS = "현재 페이지가 업데이트되었습니다.";
    public static final String USERBOOK_PAGE_UPDATE_FAIL = "현재 페이지 업데이트에 실패했습니다: ";
    public static final String USERBOOK_LIST_SUCCESS = "사용자 책 목록을 조회했습니다.";
    public static final String USERBOOK_LIST_FAIL = "사용자 책 목록 조회에 실패했습니다: ";
    
    // 독서 기록 관련 메시지
    public static final String READINGLOG_CREATE_SUCCESS = "독서 기록이 저장되었습니다.";
    public static final String READINGLOG_CREATE_FAIL = "독서 기록 저장에 실패했습니다: ";
    public static final String READINGLOG_LIST_SUCCESS = "독서 기록을 조회했습니다.";
    public static final String READINGLOG_LIST_FAIL = "독서 기록 조회에 실패했습니다: ";
    public static final String READINGLOG_STATS_SUCCESS = "독서 통계를 조회했습니다.";
    public static final String READINGLOG_STATS_FAIL = "독서 통계 조회에 실패했습니다: ";
    
    private ResponseMessage() {
        // 유틸리티 클래스이므로 인스턴스 생성 방지
    }
}
