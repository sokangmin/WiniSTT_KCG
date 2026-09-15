# -----------------------------------------------------------------------------
# STT 연계 클라이언트 Docker 이미지
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy
# 필요한 Java 버전에 맞게 태그 변경 (예: 11-jre, 8-jre 등)

# 작업 디렉토리
WORKDIR /app

# 애플리케이션 JAR 복사
COPY app/WiniSTT.jar /app/stt-client.jar

# 엔트리포인트 스크립트 복사
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# 로그 디렉토리 (볼륨 마운트 대상)
RUN mkdir -p /app/logs

# 비 root 사용자로 실행 (보안 권장 사항)
RUN groupadd -r appuser && useradd -r -g appuser appuser \
    && chown -R appuser:appuser /app
USER appuser

# 컨테이너 헬스체크: java 프로세스 존재 여부 확인
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD pgrep -f 'stt-client.jar' || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
