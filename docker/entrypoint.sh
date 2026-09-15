#!/bin/bash
###############################################################################
# STT 연계 클라이언트 - Docker 엔트리포인트
#
# 기존 run_stt_client.sh와의 차이점:
#   - nohup / 백그라운드(&) 실행 제거 → 컨테이너는 포그라운드 프로세스가
#     종료되면 같이 종료되므로, Java 프로세스가 컨테이너의 PID 1이 되도록
#     exec로 실행합니다.
#   - stdout/stderr 리다이렉트 제거 → docker logs 로 바로 확인 가능하도록
#     콘솔에 그대로 출력합니다. (log4j 애플리케이션 로그는 기존과 동일하게
#     log4j 설정에 따라 별도 관리)
#   - 모든 파라미터는 시스템 환경변수(-e 옵션 또는 env_file)로 주입받습니다.
###############################################################################

set -e

# -----------------------------------------------------------------------------
# 1. 필수 환경변수 검증
#    기본값을 두지 않고, 컨테이너 실행 시 반드시 -e 로 넘기도록 강제합니다.
#    (환경마다 값이 달라야 하는 항목이므로 하드코딩 기본값을 두지 않는 것이 안전)
# -----------------------------------------------------------------------------

: "${STT_HOST:?STT_HOST 환경변수를 설정하세요 (예: etri-gpu.ngg.ai.kr)}"
: "${STT_PORT:?STT_PORT 환경변수를 설정하세요 (예: 9999)}"
: "${CODEC:?CODEC 환경변수를 설정하세요 (raw_8k:0, raw_16k:1, ulaw:2, alaw:3)}"
: "${SRC_MQTT_URL:?SRC_MQTT_URL 환경변수를 설정하세요 (예: ssl://broker-host:8883, tcp://brocker-host:1883)}"
: "${SRC_MQTT_ID:?SRC_MQTT_ID 환경변수를 설정하세요}"
: "${SRC_MQTT_PWD:?SRC_MQTT_PWD 환경변수를 설정하세요}"
: "${STT_MQTT_URL:?STT_MQTT_URL 환경변수를 설정하세요 (예: ssl://broker-host:8883, tcp://brocker-host:1883)}"
: "${STT_MQTT_ID:?STT_MQTT_ID 환경변수를 설정하세요}"
: "${STT_MQTT_PWD:?STT_MQTT_PWD 환경변수를 설정하세요}"
: "${CALLEE_NO:?CALLEE_NO 환경변수를 설정하세요 (예: 1001)}"
: "${DEVICE_HOST:?DEVICE_HOST 환경변수를 설정하세요 (예: 192.168.33.191)}"
: "${ORG_ID:?ORG_ID 환경변수를 설정하세요 (예: 019ced2f-34ad-7ff8-8681-cd8b77e32313)}"

# CODEC 값 범위 체크
if ! [[ "${CODEC}" =~ ^[0-3]$ ]]; then
    echo "[오류] CODEC 값은 0(raw_8k), 1(raw_16k), 2(ulaw), 3(alaw) 중 하나여야 합니다. 현재값: ${CODEC}"
    exit 1
fi

JAR_PATH="/app/stt-client.jar"
MAIN_CLASS="${MAIN_CLASS:-Main}"   # 필요 시 -e MAIN_CLASS=com.example.Main 으로 지정

# -----------------------------------------------------------------------------
# 2. 설정값 로그 출력 (비밀번호는 마스킹)
# -----------------------------------------------------------------------------

echo "==================================================================="
echo " STT 연계 클라이언트 (Docker) 실행 설정"
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
echo "==================================================================="

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

# JVM 옵션 (필요 시 -e JAVA_OPTS="-Xmx512m ..." 로 조정 가능)
# JAVA_OPTS="${JAVA_OPTS:--Dlog4j.configurationFile=/app/config/log4j.xml}"

# -----------------------------------------------------------------------------
# 3. Java 실행 (exec로 PID 1 교체)
#
#    exec를 사용해야 하는 이유:
#    - exec 없이 그냥 "java ..."로 실행하면, 이 bash 스크립트가 PID 1로 남고
#      java는 그 자식 프로세스가 됩니다. 이 경우 docker stop 시 보내는
#      SIGTERM이 bash에게만 전달되고 java 프로세스에는 전달되지 않아
#      graceful shutdown이 안 될 수 있습니다.
#    - exec는 현재 셸 프로세스를 java 프로세스로 "교체"하여, java가
#      직접 PID 1이 되고 SIGTERM을 올바르게 수신하게 합니다.
# -----------------------------------------------------------------------------

if [ -n "${MAIN_CLASS}" ]; then
    echo "[실행] java ${JAVA_OPTS} -cp ${JAR_PATH} ${MAIN_CLASS} ..."
    exec java ${JAVA_OPTS} -cp "${JAR_PATH}" "${MAIN_CLASS}" "${JAVA_ARGS[@]}"
else
    echo "[실행] java ${JAVA_OPTS} -jar ${JAR_PATH} ..."
    exec java ${JAVA_OPTS} -jar "${JAR_PATH}" "${JAVA_ARGS[@]}"
fi
