## 주요 MQTT 메시지 인터페이스

### 용어 정의
- **기관ID**: 소방서/경찰서 등 기관 식별자 (UUID 형식)
  - 예: `550e8400-e29b-41d4-a716-446655440001` (서울소방서)
  - 예: `550e8400-e29b-41d4-a716-446655440002` (경기소방서)
- **callerNo**: 발신전화번호 (신고자)
- **calleeNo**: 수신전화번호 (수보대 내선번호)
- **session**: 통화ID (UUID 형식)
  - 예: `0bc596f0-faa2-48ef-acdb-7caf5c22d253`
- **callerOrCallee**: `0` = 발신자(신고자), `1` = 수신자(수보자)
- **stype**: `0` = epd 미검출 (중간 문장), `1` = epd 검출 (완료 문장)
- **sid**: 대화 순서
- **수보대 내선번호**: 수신자(접수자)의 내선번호
---
### 0. 음성 패킷 전송 
**Topic**: `call2/{기관ID}/voice/{내선 번호}/{접수자:0, 신고자:1}/4`

**예시**: `call2/00000000-0000-0000-0000-000000000000/voice/1001/1/4`, 

```MQTT
{-1234,-1230,5,4321,12345,-90,-1800, ···} //PCM 16bit 320byte
```
---
### 1. START 메시지
**Topic**: `call2/{기관ID}/info/{수보대 내선번호}`

**예시**: `call2/550e8400-e29b-41d4-a716-446655440001/info/1001` (기관ID=UUID, 내선번호=1001)

```json
{
  "date": "20240812104807",
  "callerNo": "1000",
  "calleeNo": "1001",
  "session": "0bc596f0-faa2-48ef-acdb-7caf5c22d253",
  "type": "start"
}
```

**설명**:
- `date`: 전화받은 날짜 (yyyyMMddHHmmss)
- `callerNo`: 발신전화번호 (신고자)
- `calleeNo`: 수신전화번호 (수보대 내선번호)
- `session`: 통화ID
- `type`: "start"

---

### 2. END 메시지
**Topic**: `call2/{기관ID}/end/{수보대 내선번호}`

**예시**: `call2/550e8400-e29b-41d4-a716-446655440001/end/1001` (기관ID=UUID, 내선번호=1001)

```json
{
  "date": "20240812104918",
  "callerNo": "1000",
  "calleeNo": "1001",
  "session": "0bc596f0-faa2-48ef-acdb-7caf5c22d253",
  "type": "end"
}
```

**설명**:
- `date`: 전화 종료 날짜 (yyyyMMddHHmmss)
- `callerNo`: 발신전화번호
- `calleeNo`: 수신전화번호
- `session`: 통화ID
- `type`: "end"

---

### 3. STT 결과 메시지
**Topic**: `call2/{기관ID}/stt/{수보대 내선번호}/{구분}`
- 구분: `0` = 발신자(신고자), `1` = 수신자(수보자)

#### 3.1. 기본 STT 결과 (한국어)
**Topic 예시**: `call2/550e8400-e29b-41d4-a716-446655440001/stt/1002/0` (기관ID=UUID, 내선번호=1002, 신고자)
**Topic 예시**: `call2/550e8400-e29b-41d4-a716-446655440001/stt/1002/1` (기관ID=UUID, 내선번호=1002, 수보자)

```json
{
  "stt": "안녕하세요.",
  "callerOrCallee": "0",
  "callerNo": "1000",
  "calleeNo": "1001",
  "session": "0bc596f0-faa2-48ef-acdb-7caf5c22d253",
  "date": "20240812104918",
  "sid": "0",
  "stype": "1"
}
```

**설명**:
- `stt`: 전사 결과
- `callerOrCallee`: `0` = 발신자(신고자), `1` = 수신자(수보자)
- `callerNo`: 발신전화번호
- `calleeNo`: 수신전화번호 (수보대 내선번호)
- `session`: 통화ID
- `date`: 전화받은 날짜 (yyyyMMddHHmmss)
- `sid`: 문장순번
- `stype`: `0` = epd 미검출 (중간 문장), `1` = epd 검출 (완료 문장)

## Docker 사용법
```bash
cd docker
docker-compose build
docker-compose up -d

# 상태확인
docker-compose ps
docker logs -f stt-client-1001

# 특정 내선만 재시작
docker-compose restart stt-client-1002

# 전체 종료
docker-compose down
```
