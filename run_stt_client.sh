#!/bin/bash
###############################################################################
# STT 연계 클라이언트 실행 스크립트
#
# 사용법:
#   ./run_stt_client.sh
#
# 아래 변수 값을 환경에 맞게 수정한 뒤 실행하세요.
###############################################################################

set -e  # 명령어 실패 시 즉시 스크립트 종료

# -----------------------------------------------------------------------------
# 1. 실행 파라미터 정의
# -----------------------------------------------------------------------------

# STT 엔진 접속 정보
STT_HOST="etri-gpu.ngg.ai.kr"     # STT 엔진 url (한)
STT_PORT=9999                      # STT 엔진 port (한)

# 음성 코덱 정보 (raw_8k:0, raw_16k:1, ulaw:2, alaw:3)
CODEC=0

# 내부연계 MQTT 브로커 (내부망 - 전화기/장비 연동용)
SRC_MQTT_URL="ssl://127.0.0.1:8883"
SRC_MQTT_ID="wini"
SRC_MQTT_PWD="wini"

# 외부연계 MQTT 브로커 (외부망 - STT 엔진 연동용)
STT_MQTT_URL="ssl://127.0.0.1:8883"
STT_MQTT_ID="wini"
STT_MQTT_PWD="wini"

# 수보자(내선) 정보
CALLEE_NO="1001"                   # 수보자 전화기 내선번호
DEVICE_HOST="192.168.3.192"        # 수보자 전화기 IP

# 기관 정보
ORG_ID="019ced2f-34ad-7ff8-8681-cd8b77e32313"

# -----------------------------------------------------------------------------
# 2. 실행 환경 설정
# -----------------------------------------------------------------------------

# 스크립트가 위치한 디렉토리를 기준으로 JAR 경로 지정 (필요 시 절대경로로 수정)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="${SCRIPT_DIR}/stt-client.jar"     # 실제 JAR 파일명으로 변경하세요
MAIN_CLASS="Main"                                # JAR에 Main-Class가 명시되어 있으면 비워두고,
                                              # 없다면 "com.example.Main" 형태로 지정

LOG_DIR="${SCRIPT_DIR}/logs"
LOG_FILE="${LOG_DIR}/stt_client_$(date +%Y%m%d_%H%M%S).log"

mkdir -p "${LOG_DIR}"

# -----------------------------------------------------------------------------
# 3. 필수값 검증
# -----------------------------------------------------------------------------

check_required() {
    local name=$1
    local value=$2
    if [ -z "${value}" ]; then
        echo "[오류] ${name} 값이 비어 있습니다. 스크립트 상단 변수를 확인하세요."
        exit 1
    fi
}

check_required "STT_HOST" "${STT_HOST}"
check_required "STT_PORT" "${STT_PORT}"
check_required "CODEC" "${CODEC}"
check_required "SRC_MQTT_URL" "${SRC_MQTT_URL}"
check_required "SRC_MQTT_ID" "${SRC_MQTT_ID}"
check_required "SRC_MQTT_PWD" "${SRC_MQTT_PWD}"
check_required "STT_MQTT_URL" "${STT_MQTT_URL}"
check_required "STT_MQTT_ID" "${STT_MQTT_ID}"
check_required "STT_MQTT_PWD" "${STT_MQTT_PWD}"
check_required "CALLEE_NO" "${CALLEE_NO}"
check_required "DEVICE_HOST" "${DEVICE_HOST}"
check_required "ORG_ID" "${ORG_ID}"

if [ ! -f "${JAR_PATH}" ]; then
    echo "[오류] JAR 파일을 찾을 수 없습니다: ${JAR_PATH}"
    exit 1
fi

# CODEC 값 범위 체크 (0~3)
if ! [[ "${CODEC}" =~ ^[0-3]$ ]]; then
    echo "[오류] CODEC 값은 0(raw_8k), 1(raw_16k), 2(ulaw), 3(alaw) 중 하나여야 합니다. 현재값: ${CODEC}"
    exit 1
fi

# -----------------------------------------------------------------------------
# 4. 설정값 출력 (실행 전 확인용)
# -----------------------------------------------------------------------------

echo "==================================================================="
echo " STT 연계 클라이언트 실행 설정"
echo "==================================================================="
echo " STT_HOST       : ${STT_HOST}"
echo " STT_PORT       : ${STT_PORT}"
echo " CODEC          : ${CODEC}"
echo " SRC_MQTT_URL   : ${SRC_MQTT_URL}"
echo " SRC_MQTT_ID    : ${SRC_MQTT_ID}"
echo " SRC_MQTT_PWD   : ****** (마스킹)"
echo " STT_MQTT_URL   : ${STT_MQTT_URL}"
echo " STT_MQTT_ID    : ${STT_MQTT_ID}"
echo " STT_MQTT_PWD   : ****** (마스킹)"
echo " CALLEE_NO      : ${CALLEE_NO}"
echo " DEVICE_HOST    : ${DEVICE_HOST}"
echo " ORG_ID         : ${ORG_ID}"
echo " JAR_PATH       : ${JAR_PATH}"
echo " LOG_FILE       : ${LOG_FILE}"
echo "==================================================================="

# -----------------------------------------------------------------------------
# 5. Java 실행
# -----------------------------------------------------------------------------

JAVA_ARGS=(
    "${STT_HOST}"
    "${STT_PORT}"
    "${CODEC}"
    "${SRC_MQTT_URL}"
    "${SRC_MQTT_ID}"
    "${SRC_MQTT_PWD}"
    "${STT_MQTT_URL}"
    "${STT_MQTT_ID}"
    "${STT_MQTT_PWD}"
    "${CALLEE_NO}"
    "${DEVICE_HOST}"
    "${ORG_ID}"
)

if [ -n "${MAIN_CLASS}" ]; then
    # Main-Class를 JAR 내부 MANIFEST에서 못 찾는 경우, 클래스명을 명시하여 실행
    echo "[실행] java -cp ${JAR_PATH} ${MAIN_CLASS} ..."
    nohup java -cp "${JAR_PATH}" "${MAIN_CLASS}" "${JAVA_ARGS[@]}" > "${LOG_FILE}" 2>&1 &
else
    # JAR의 MANIFEST.MF에 Main-Class가 지정되어 있는 경우
    echo "[실행] java -jar ${JAR_PATH} ..."
    nohup java -jar "${JAR_PATH}" "${JAVA_ARGS[@]}" > "${LOG_FILE}" 2>&1 &
fi

PID=$!
echo "$PID" > "${SCRIPT_DIR}/stt_client.pid"

echo "-------------------------------------------------------------------"
echo " 실행 완료 (PID: ${PID})"
echo " 로그 확인: tail -f ${LOG_FILE}"
echo "-------------------------------------------------------------------"