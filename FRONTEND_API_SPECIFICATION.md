# 📱 ReadMark 프론트엔드 API 명세서

## 🎯 개요
이 문서는 ReadMark 프론트엔드 개발을 위한 API 명세서입니다. 웹/앱에서 사용할 모든 API를 JSON 형태로 제공합니다.

## 🔗 기본 정보
```json
{
  "baseUrl": "http://43.200.102.14:5000",
  "contentType": "application/json (대부분), multipart/form-data (이미지 업로드)",
  "responseFormat": "JSON",
  "maxFileSize": "16MB",
  "supportedFormats": ["JPG", "PNG", "GIF", "BMP", "WEBP"],
  "target": "프론트엔드 팀 (웹/앱 개발)"
}
```

---

## 📸 이미지 업로드 API

### 프론트엔드용 이미지 업로드
```json
{
  "endpoint": {
    "method": "POST",
    "url": "/api/image/upload",
    "fullUrl": "http://43.200.102.14:5000/api/image/upload",
    "description": "프론트엔드용 복잡한 이미지 업로드 API. 사용자 ID, 책 ID 등 추가 정보를 받고 DB에 저장합니다.",
    "target": "프론트엔드 팀"
  },
  "request": {
    "contentType": "multipart/form-data",
    "body": {
      "userId": {
        "type": "Long",
        "required": true,
        "description": "사용자 ID"
      },
      "bookId": {
        "type": "Long",
        "required": true,
        "description": "책 ID"
      },
      "image": {
        "type": "File",
        "required": true,
        "description": "업로드할 이미지 파일"
      },
      "captureTime": {
        "type": "String",
        "required": false,
        "description": "촬영 시간 (yyyy-MM-dd HH:mm:ss 형식)"
      }
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책 페이지가 성공적으로 저장되었습니다.",
        "page": {
          "pageId": 1,
          "pageNumber": 123,
          "confidence": 0.95,
          "language": "ko",
          "numberCount": 150,
          "capturedAt": "2025-01-14T12:30:00"
        },
        "userId": 1,
        "bookId": 1
      }
    },
    "error": {
      "statusCode": 400,
      "body": {
        "success": false,
        "message": "이미지가 비어있습니다."
      }
    }
  },
  "examples": {
    "javascript": "const formData = new FormData(); formData.append('userId', '1'); formData.append('bookId', '1'); formData.append('image', imageFile); fetch('http://43.200.102.14:5000/api/image/upload', { method: 'POST', body: formData }).then(response => response.json()).then(data => console.log(data));",
    "curl": "curl -X POST http://43.200.102.14:5000/api/image/upload -F \"userId=1\" -F \"bookId=1\" -F \"image=@test_image.jpg\""
  }
}
```

---

## 👤 사용자 관리 API

### 1. 사용자 회원가입
```json
{
  "method": "POST",
  "url": "/api/users/join",
  "description": "사용자 회원가입",
  "request": {
    "contentType": "application/json",
    "body": {
      "name": "String (required)",
      "username": "String (required)",
      "email": "String (required)",
      "password": "String (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "회원가입 성공",
        "data": {
          "userId": 1,
          "name": "홍길동",
          "username": "hong123",
          "email": "hong@example.com"
        }
      }
    }
  }
}
```

### 2. 사용자 로그인
```json
{
  "method": "POST",
  "url": "/api/users/login",
  "description": "사용자 로그인",
  "request": {
    "contentType": "application/json",
    "body": {
      "username": "String (required)",
      "password": "String (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "로그인 성공",
        "data": {
          "userId": 1,
          "name": "홍길동",
          "username": "hong123",
          "email": "hong@example.com"
        }
      }
    }
  }
}
```

---

## 📚 책 관리 API

### 1. 책 등록
```json
{
  "method": "POST",
  "url": "/api/books",
  "description": "새 책 등록",
  "request": {
    "contentType": "application/json",
    "body": {
      "title": "String (required)",
      "author": "String (required)",
      "publisher": "String (required)",
      "coverImageUrl": "String (optional)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책이 등록되었습니다.",
        "bookId": 1
      }
    }
  }
}
```

### 2. 책 검색
```json
{
  "method": "GET",
  "url": "/api/books/search?keyword={keyword}",
  "description": "책 제목/저자로 검색",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "books": [
          {
            "bookId": 1,
            "title": "자바의 정석",
            "author": "남궁성",
            "publisher": "도우출판"
          }
        ],
        "count": 1
      }
    }
  }
}
```

---

## 📊 독서 통계 API

### 1. 독서 통계 조회
```json
{
  "method": "GET",
  "url": "/api/image/stats/{userId}",
  "description": "사용자 독서 통계 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "stats": {
          "maxConsecutiveDays": 7,
          "totalReadingDays": 15,
          "currentConsecutiveDays": 3,
          "habitAnalysis": {
            "averagePagesPerDay": 5.2,
            "preferredReadingTime": "오후 2-4시",
            "readingConsistency": "보통"
          }
        }
      }
    }
  }
}
```

---

## 🔍 응답 코드 및 메시지

### 성공 응답
```json
{
  "statusCode": 200,
  "description": "요청 성공",
  "body": {
    "success": true,
    "pageNumber": 123
  }
}
```

### 오류 응답들
```json
{
  "errors": [
    {
      "statusCode": 400,
      "description": "이미지가 비어있음",
      "body": {
        "success": false,
        "message": "이미지가 비어있습니다."
      }
    },
    {
      "statusCode": 400,
      "description": "파일 크기 초과",
      "body": {
        "success": false,
        "message": "이미지 크기가 너무 큽니다. (최대 16MB)"
      }
    },
    {
      "statusCode": 400,
      "description": "잘못된 파일 형식",
      "body": {
        "success": false,
        "message": "올바른 이미지 형식이 아닙니다."
      }
    },
    {
      "statusCode": 500,
      "description": "서버 내부 오류",
      "body": {
        "success": false,
        "message": "이미지 처리 중 오류가 발생했습니다: [오류 상세 내용]"
      }
    }
  ]
}
```

---

## 🛠️ 프론트엔드 구현 예시

### React 컴포넌트 - 이미지 업로드
```json
{
  "framework": "React",
  "description": "책 페이지 이미지 업로드 컴포넌트",
  "code": "import React, { useState } from 'react';\n\nconst ImageUpload = () => {\n  const [selectedFile, setSelectedFile] = useState(null);\n  const [result, setResult] = useState(null);\n  const [loading, setLoading] = useState(false);\n  const [userId, setUserId] = useState('1');\n  const [bookId, setBookId] = useState('1');\n\n  const handleFileChange = (event) => {\n    setSelectedFile(event.target.files[0]);\n  };\n\n  const handleUpload = async () => {\n    if (!selectedFile) {\n      alert('이미지를 선택해주세요.');\n      return;\n    }\n\n    setLoading(true);\n    const formData = new FormData();\n    formData.append('userId', userId);\n    formData.append('bookId', bookId);\n    formData.append('image', selectedFile);\n\n    try {\n      const response = await fetch('http://43.200.102.14:5000/api/image/upload', {\n        method: 'POST',\n        body: formData\n      });\n\n      const data = await response.json();\n      setResult(data);\n    } catch (error) {\n      console.error('업로드 오류:', error);\n      setResult({ success: false, message: '업로드 실패' });\n    } finally {\n      setLoading(false);\n    }\n  };\n\n  return (\n    <div>\n      <div>\n        <label>사용자 ID: </label>\n        <input type=\"number\" value={userId} onChange={(e) => setUserId(e.target.value)} />\n      </div>\n      <div>\n        <label>책 ID: </label>\n        <input type=\"number\" value={bookId} onChange={(e) => setBookId(e.target.value)} />\n      </div>\n      <input type=\"file\" accept=\"image/*\" onChange={handleFileChange} />\n      <button onClick={handleUpload} disabled={loading}>\n        {loading ? '처리 중...' : '업로드'}\n      </button>\n      \n      {result && (\n        <div>\n          {result.success ? (\n            <div>\n              <p>페이지 ID: {result.page.pageId}</p>\n              <p>페이지 번호: {result.page.pageNumber}</p>\n              <p>신뢰도: {result.page.confidence}</p>\n              <p>언어: {result.page.language}</p>\n              <p>단어 수: {result.page.numberCount}</p>\n            </div>\n          ) : (\n            <p>오류: {result.message}</p>\n          )}\n        </div>\n      )}\n    </div>\n  );\n};\n\nexport default ImageUpload;"
}
```

### React 컴포넌트 - 사용자 로그인
```json
{
  "framework": "React",
  "description": "사용자 로그인 컴포넌트",
  "code": "import React, { useState } from 'react';\n\nconst LoginForm = () => {\n  const [username, setUsername] = useState('');\n  const [password, setPassword] = useState('');\n  const [loading, setLoading] = useState(false);\n  const [result, setResult] = useState(null);\n\n  const handleLogin = async (e) => {\n    e.preventDefault();\n    setLoading(true);\n\n    try {\n      const response = await fetch('http://43.200.102.14:5000/api/users/login', {\n        method: 'POST',\n        headers: {\n          'Content-Type': 'application/json'\n        },\n        body: JSON.stringify({ username, password })\n      });\n\n      const data = await response.json();\n      setResult(data);\n      \n      if (data.success) {\n        // 로그인 성공 처리\n        localStorage.setItem('user', JSON.stringify(data.data));\n        console.log('로그인 성공:', data.data);\n      }\n    } catch (error) {\n      console.error('로그인 오류:', error);\n      setResult({ success: false, message: '로그인 실패' });\n    } finally {\n      setLoading(false);\n    }\n  };\n\n  return (\n    <form onSubmit={handleLogin}>\n      <div>\n        <label>사용자명: </label>\n        <input \n          type=\"text\" \n          value={username} \n          onChange={(e) => setUsername(e.target.value)} \n          required \n        />\n      </div>\n      <div>\n        <label>비밀번호: </label>\n        <input \n          type=\"password\" \n          value={password} \n          onChange={(e) => setPassword(e.target.value)} \n          required \n        />\n      </div>\n      <button type=\"submit\" disabled={loading}>\n        {loading ? '로그인 중...' : '로그인'}\n      </button>\n      \n      {result && (\n        <div>\n          {result.success ? (\n            <p>로그인 성공: {result.data.name}</p>\n          ) : (\n            <p>오류: {result.message}</p>\n          )}\n        </div>\n      )}\n    </form>\n  );\n};\n\nexport default LoginForm;"
}
```

### Vue.js 컴포넌트
```json
{
  "framework": "Vue.js",
  "template": "<template>\n  <div>\n    <input type=\"file\" accept=\"image/*\" @change=\"handleFileChange\" />\n    <button @click=\"handleUpload\" :disabled=\"loading\">\n      {{ loading ? '처리 중...' : '업로드' }}\n    </button>\n    \n    <div v-if=\"result\">\n      <p v-if=\"result.success\">페이지 번호: {{ result.pageNumber }}</p>\n      <p v-else>오류: {{ result.message }}</p>\n    </div>\n  </div>\n</template>",
  "script": "export default {\n  data() {\n    return {\n      selectedFile: null,\n      result: null,\n      loading: false\n    };\n  },\n  methods: {\n    handleFileChange(event) {\n      this.selectedFile = event.target.files[0];\n    },\n    async handleUpload() {\n      if (!this.selectedFile) {\n        alert('이미지를 선택해주세요.');\n        return;\n      }\n\n      this.loading = true;\n      const formData = new FormData();\n      formData.append('image', this.selectedFile);\n\n      try {\n        const response = await fetch('http://43.200.102.14:5000/upload/', {\n          method: 'POST',\n          body: formData\n        });\n\n        this.result = await response.json();\n      } catch (error) {\n        console.error('업로드 오류:', error);\n        this.result = { success: false, message: '업로드 실패' };\n      } finally {\n        this.loading = false;\n      }\n    }\n  }\n};"
}
```

### Vanilla JavaScript
```json
{
  "framework": "Vanilla JavaScript",
  "html": "<input type=\"file\" id=\"imageInput\" accept=\"image/*\" />\n<button id=\"uploadBtn\">업로드</button>\n<div id=\"result\"></div>",
  "javascript": "document.getElementById('uploadBtn').addEventListener('click', async () => {\n  const fileInput = document.getElementById('imageInput');\n  const resultDiv = document.getElementById('result');\n  \n  if (!fileInput.files[0]) {\n    resultDiv.innerHTML = '<p>이미지를 선택해주세요.</p>';\n    return;\n  }\n\n  const formData = new FormData();\n  formData.append('image', fileInput.files[0]);\n\n  try {\n    const response = await fetch('http://43.200.102.14:5000/upload/', {\n      method: 'POST',\n      body: formData\n    });\n\n    const data = await response.json();\n    \n    if (data.success) {\n      resultDiv.innerHTML = `<p>페이지 번호: ${data.pageNumber}</p>`;\n    } else {\n      resultDiv.innerHTML = `<p>오류: ${data.message}</p>`;\n    }\n  } catch (error) {\n    console.error('업로드 오류:', error);\n    resultDiv.innerHTML = '<p>업로드 실패</p>';\n  }\n});"
}
```

---

## 🔧 개발 도구 및 테스트

### Postman 설정 - 이미지 업로드
```json
{
  "tool": "Postman",
  "description": "프론트엔드용 이미지 업로드 테스트",
  "settings": {
    "method": "POST",
    "url": "http://43.200.102.14:5000/api/image/upload",
    "body": {
      "type": "form-data",
      "fields": {
        "userId": {
          "type": "Text",
          "value": "1"
        },
        "bookId": {
          "type": "Text", 
          "value": "1"
        },
        "image": {
          "type": "File",
          "value": "[이미지 파일 선택]"
        }
      }
    }
  }
}
```

### Postman 설정 - 사용자 로그인
```json
{
  "tool": "Postman",
  "description": "사용자 로그인 테스트",
  "settings": {
    "method": "POST",
    "url": "http://43.200.102.14:5000/api/users/login",
    "headers": {
      "Content-Type": "application/json"
    },
    "body": {
      "type": "raw",
      "format": "JSON",
      "data": {
        "username": "hong123",
        "password": "password123"
      }
    }
  }
}
```

### cURL 명령어
```json
{
  "imageUpload": "curl -X POST http://43.200.102.14:5000/api/image/upload -F \"userId=1\" -F \"bookId=1\" -F \"image=@test_image.jpg\"",
  "userLogin": "curl -X POST http://43.200.102.14:5000/api/users/login -H \"Content-Type: application/json\" -d '{\"username\":\"hong123\",\"password\":\"password123\"}'",
  "bookSearch": "curl -X GET \"http://43.200.102.14:5000/api/books/search?keyword=자바\"",
  "userStats": "curl -X GET http://43.200.102.14:5000/api/image/stats/1"
}
```

---

## 📝 주의사항
```json
{
  "cors": "서버에서 CORS가 활성화되어 있어 브라우저에서 직접 호출 가능",
  "fileSize": "16MB 이하의 이미지만 업로드 가능",
  "imageFormat": "이미지 파일만 업로드 가능",
  "responseHandling": "success 필드로 성공/실패 여부 확인",
  "errorHandling": "네트워크 오류와 서버 오류를 구분하여 처리",
  "authentication": "현재는 기본 인증, 향후 JWT 토큰 기반 인증 예정",
  "userId": "프론트엔드에서 사용자 ID를 관리하고 API 호출 시 전달",
  "bookId": "책 등록 후 반환되는 bookId를 사용하여 이미지 업로드"
}
```

---

## 🚀 프론트엔드 개발 가이드

### 필수 API 호출 순서
```json
{
  "typicalFlow": [
    "1. 사용자 회원가입/로그인 (/api/users/join, /api/users/login)",
    "2. 책 등록 (/api/books)",
    "3. 이미지 업로드 (/api/image/upload)",
    "4. 독서 통계 조회 (/api/image/stats/{userId})"
  ]
}
```

### 상태 관리 권장사항
```json
{
  "stateManagement": {
    "user": "로그인한 사용자 정보 (userId, name, email)",
    "currentBook": "현재 읽고 있는 책 정보 (bookId, title)",
    "uploadResult": "이미지 업로드 결과 (pageNumber, confidence)",
    "readingStats": "독서 통계 정보 (연속 독서일, 총 독서일)"
  }
}
```

### 에러 처리 패턴
```json
{
  "errorHandling": {
    "networkError": "네트워크 연결 실패 시 재시도 로직 구현",
    "serverError": "서버 오류 시 사용자에게 적절한 메시지 표시",
    "validationError": "입력값 검증 실패 시 필드별 오류 메시지 표시",
    "fileError": "파일 크기/형식 오류 시 업로드 버튼 비활성화"
  }
}
```
