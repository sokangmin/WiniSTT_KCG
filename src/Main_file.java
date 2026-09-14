import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
* 토픽 -> call로 이름변경
* alaw -> mulaw로 변경
*
* 변수명에 대한 리팩토링 필요(caller, callee 너무 모호함 others, self 교체)
* */

public class Main_file {
    static final Logger logger = LogManager.getLogger(Main_file.class);
    static ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void main(String[] args) {
        String lastCallInfo = null; // 언어변경일 경우, 이전 통화정보

        while (true) {
            try {
                CallInfo calleeInfo = new CallInfo("etri-gpu.ngg.ai.kr","9999", 0, "",
                        "", "", "", "", "", "4001","",
                        "s_test", "01012345678", CallInfo.CallerOrCallee.Callee, 0, "ko", "019ced2f-34ad-7ff8-8681-cd8b77e32313");

                SttAdapter callee = new Selvy3_1_8_3_en();

                WiniSTT_8k_16bit_file sttCallee = new WiniSTT_8k_16bit_file(callee, calleeInfo);

                Future<String> calleeThread = executor.submit(sttCallee::callSTT);

                lastCallInfo = calleeThread.get();

                break;
            } catch (Exception e) {
                logger.error("", e);
                break;
            } finally {
            }
        }
    }
}