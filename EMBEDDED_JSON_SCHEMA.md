# ReadMark 임베디드 JSON

받는 데이터 (응답)
```json
{
  "success": "boolean",
  "message": "string",
  "pageId": "number",
  "pageNumber": "number",
  "confidence": "number",
  "deviceInfo": "string",
  "capturedAt": "string",
  "date": "number",
  "readingPeriod": "string",
  "currentConsecutiveDays": "number"
}
```

### 일반 이미지 업로드 (`POST /api/image/upload`)

보내는 데이터 (요청)
```
userId: "number"           // 필수
bookId: "number"           // 필수
image: [이미지 파일]        // 필수
deviceInfo: "string"       // 선택
captureTime: "string"      // 선택
```

받는 데이터 (응답)
```json
{
  "success": "boolean",
  "message": "string",
  "page": {
    "pageId": "number",
    "pageNumber": "number",
    "confidence": "number",
    "language": "string",
    "numberCount": "number",
    "capturedAt": "string"
  },
  "deviceInfo": "string"
}
```

## 독서 세션 관리

### 세션 시작 (`POST /api/image/session/start`)

보내는 데이터 (요청)
```
userId: "number"    // 필수
bookId: "number"    // 필수
```
### 세션 종료 (`POST /api/image/session/end`)

보내는 데이터 (요청)
```
userId: "number"    // 필수
```

받는 데이터 (응답)
```json
{
  "success": "boolean",
  "message": "string",
  "totalPagesRead": "number",
  "totalWordsRead": "number",
  "readingDurationMinutes": "number",
  "endTime": "string"
}
```

## 독서 통계 조회

### 통계 조회 (`GET /api/image/stats/{userId}`)

보내는 데이터 (요청)
```
URL 파라미터: userId (예: /api/image/stats/1)
```

받는 데이터 (응답)
```json
{
  "success": "boolean",
  "stats": {
    "maxConsecutiveDays": "number",
    "totalReadingDays": "number",
    "currentConsecutiveDays": "number",
    "habitAnalysis": {
      "preferredTime": "string",
      "averageReadingTime": "number",
      "mostReadGenre": "string"
    }
  }
}
```

## 연결상태

### 상태체크 (`GET /upload/health`)

보내는 데이터 (요청)
```
없음 (GET 요청)
```

받는 데이터 (응답)
```json
{
  "status": "string",
  "message": "string",
  "timestamp": "string"
}
```

