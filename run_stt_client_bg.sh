#!/bin/bash
###############################################################################
# STT 연계 클라이언트 실행 스크립트 (background / nohup 용)
#
# 사용법:
#   ./run_stt_client_bg.sh
#
# 기존 run_stt_client.sh 와의 차이점
#   - 실행 전에 기존 stt-client java 프로세스를 정리 (중복 실행 방지)
#   - 실행 후 프로세스가 실제로 살아있는지 확인
#
# 아래 변수 값을 환경에 맞게 수정한 뒤 실행하세요.
###############################################################################

set -e

# -----------------------------------------------------------------------------
# 1. 실행 파라미터 정의
# -----------------------------------------------------------------------------

# STT 엔진 접속 정보
STT_HOST="10.5.3.92"
STT_PORT=9999

# 음성 코덱 정보 (raw_8k:0, raw_16k:1, ulaw:2, alaw:3)
CODEC=0

# 내부연계 MQTT 브로커 (내부망 - 전화기/장비 연동용)
SRC_MQTT_URL="ssl://127.0.0.1:8883"
SRC_MQTT_ID="wini"
SRC_MQTT_PWD="wini00"

# 외부연계 MQTT 브로커 (외부망 - STT 엔진 연동용)
STT_MQTT_URL="ssl://127.0.0.1:8883"
STT_MQTT_ID="wini"
STT_MQTT_PWD="wini00"

# 수보자(내선) 정보
CALLEE_NO="3001"
DEVICE_HOST="192.168.3.192"

# 기관 정보
ORG_ID="019ced2f-34ad-7ff8-8681-cd8b77e32313"

# 기존 프로세스 종료 대기 시간(초)
KILL_WAIT_SEC=10

# 실행 후 생존 확인 대기 시간(초)
START_CHECK_SEC=3

# -----------------------------------------------------------------------------
# 2. 실행 환경 설정
# -----------------------------------------------------------------------------

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="${SCRIPT_DIR}/stt-client.jar"
MAIN_CLASS=""     # JAR 에 Main-Class 가 있으면 비워두고, 없으면 "com.example.Main" 형태로 지정
PID_FILE="${SCRIPT_DIR}/stt_client.pid"
JAR_NAME="$(basename "${JAR_PATH}")"

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

if ! [[ "${CODEC}" =~ ^[0-3]$ ]]; then
    echo "[오류] CODEC 값은 0(raw_8k), 1(raw_16k), 2(ulaw), 3(alaw) 중 하나여야 합니다. 현재값: ${CODEC}"
    exit 1
fi

# -----------------------------------------------------------------------------
# 4. 기존 java 프로세스 정리
# -----------------------------------------------------------------------------

# 프로세스가 살아있는지 확인 (좀비 상태는 종료된 것으로 간주)
is_alive() {
    local pid=$1
    kill -0 "${pid}" 2>/dev/null || return 1
    local state
    state="$(awk '{print $3}' "/proc/${pid}/stat" 2>/dev/null)"
    [ "${state}" != "Z" ]
}

# 해당 PID 가 우리 JAR 를 실행 중인 java 프로세스인지 확인 (PID 재사용 오인 방지)
is_stt_client() {
    local pid=$1
    [ -r "/proc/${pid}/cmdline" ] || return 1
    tr '\0' ' ' < "/proc/${pid}/cmdline" | grep -q "java.*${JAR_NAME}"
}

cleanup_old_processes() {
    local pids=()
    local pid

    # (1) PID 파일에 기록된 프로세스
    if [ -f "${PID_FILE}" ]; then
        pid="$(tr -dc '0-9' < "${PID_FILE}")"
        if [ -n "${pid}" ] && [ "${pid}" != "$$" ] && is_stt_client "${pid}"; then
            pids+=("${pid}")
        fi
        rm -f "${PID_FILE}"
    fi

    # (2) PID 파일에 없더라도 같은 JAR 를 실행 중인 java 프로세스 전부
    while read -r pid; do
        [ -z "${pid}" ] && continue
        [ "${pid}" = "$$" ] && continue
        is_stt_client "${pid}" || continue
        pids+=("${pid}")
    done < <(pgrep -f "java.*${JAR_NAME}" 2>/dev/null || true)

    if [ ${#pids[@]} -eq 0 ]; then
        echo "[정리] 기존 stt-client 프로세스 없음"
        return 0
    fi
    mapfile -t pids < <(printf '%s\n' "${pids[@]}" | sort -un)

    echo "[정리] 기존 프로세스 종료 시도 (SIGTERM): ${pids[*]}"
    kill -TERM "${pids[@]}" 2>/dev/null || true

    local i
    for i in $(seq 1 "${KILL_WAIT_SEC}"); do
        local alive=()
        for pid in "${pids[@]}"; do
            is_alive "${pid}" && alive+=("${pid}")
        done
        [ ${#alive[@]} -eq 0 ] && { echo "[정리] 정상 종료 완료"; return 0; }
        sleep 1
    done

    echo "[정리] 종료되지 않아 강제 종료 (SIGKILL): ${alive[*]}"
    kill -KILL "${alive[@]}" 2>/dev/null || true
    sleep 1
}

cleanup_old_processes

# -----------------------------------------------------------------------------
# 5. 설정값 출력 (실행 전 확인용)
# -----------------------------------------------------------------------------

echo "==================================================================="
echo " STT 연계 클라이언트 실행 설정 (background)"
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
# 6. Java 백그라운드 실행
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
    echo "[실행] java -cp ${JAR_PATH} ${MAIN_CLASS} ..."
    nohup java -cp "${JAR_PATH}" "${MAIN_CLASS}" "${JAVA_ARGS[@]}" > "${LOG_FILE}" 2>&1 &
else
    echo "[실행] java -jar ${JAR_PATH} ..."
    nohup java -jar "${JAR_PATH}" "${JAVA_ARGS[@]}" > "${LOG_FILE}" 2>&1 &
fi

PID=$!
echo "${PID}" > "${PID_FILE}"

# -----------------------------------------------------------------------------
# 7. 기동 확인
# -----------------------------------------------------------------------------

sleep "${START_CHECK_SEC}"

if is_alive "${PID}"; then
    echo "-------------------------------------------------------------------"
    echo " 실행 완료 (PID: ${PID})"
    echo " 로그 확인: tail -f ${LOG_FILE}"
    echo "-------------------------------------------------------------------"
else
    echo "-------------------------------------------------------------------"
    echo "[오류] 프로세스가 기동 직후 종료되었습니다. 로그를 확인하세요."
    echo " 로그 확인: tail -n 50 ${LOG_FILE}"
    echo "-------------------------------------------------------------------"
    rm -f "${PID_FILE}"
    exit 1
fi
