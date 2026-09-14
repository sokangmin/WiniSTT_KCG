import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.mqtt.client.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/*
* 토픽 -> call로 이름변경
* alaw -> mulaw로 변경
*
* 변수명에 대한 리팩토링 필요(caller, callee 너무 모호함 others, self 교체)
* */

public class Main {
    static final Logger logger = LogManager.getLogger(Main.class);
    static ExecutorService executor = Executors.newFixedThreadPool(2);

    static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        if (args.length != 12) {
            logger.error("The number of params must be 12.", new IllegalArgumentException());
            System.exit(0);
        }

        ObjectMapper mapper = new ObjectMapper();
        /*
          String sttHost = args[0];    // STT 엔진 url(192.168.220.94(한)@192.168.220.93(영)@192.168.220.93(중))
          int sttPort = Integer.parseInt(args[1]); // STT 엔진 port(9500(한)@5200(영)@5300(중))
          int codec = Integer.parseInt(args[2]);   // 음성코덱정보(ulaw:2, alaw:3)
          String srcMqttUrl = args[3]; // 내부연계 mqtt broker url(tcp://127.0.0.1:1883)
          String srcMqttId = args[4]; // 내부연계 mqtt broker id(cb119)
          String srcMqttPwd = args[5]; // 내부연계 mqtt broker pwd(cb119cb1191!)
          String sttMqttUrl = args[6]; // 외부연계 mqtt broker url(tcp://192.168.1.88:1883)
          String sttMqttId = args[7];  // 외부연계 mqtt broker id(wini)
          String sttMqttPwd = args[8]; // 외부연계 mqtt broker pwd(wini00)
          String calleeNo = args[9]; // 수보자 전화기 내선번호(4001)
          String deviceHost = args[10]; // 수보자 전화기 ip(192.168.3.192)
         */
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        String srcMqttUrl = args[3];
        String srcMqttId = args[4];
        String srcMqttPwd = args[5];
        String calleeNo = args[9];

        String lastCallInfo = null; // 언어변경일 경우, 이전 통화정보
        String session;
        String callerNo;
        String lang;
        int sid;


        while (true) {
            BlockingConnection callConn = null;
            try {
                if(Objects.isNull(lastCallInfo)) {  // 신규전화일 경우
                    /*
                     * 통화정보 수신 MQ cli
                     * CTI 연계모듈에서 CTI 로부터 전화수신정보가 연계되면 'call/info/수보자전회기내선번호' 토픽에 전화수신정보를 저장
                     */
                    MQTT callMqtts = new MQTT();
                    callMqtts.setHost(srcMqttUrl);
                    callMqtts.setUserName(srcMqttId);
                    callMqtts.setPassword(srcMqttPwd);
                    callMqtts.setSslContext(sslContext);
                    Topic[] callTopics = {new Topic("call2/+/info/" + calleeNo, QoS.AT_MOST_ONCE)};
                    callConn = callMqtts.blockingConnection();
                    callConn.connect();
                    callConn.subscribe(callTopics);

                    /*
                     * 간혹 mqtt connection 을 장시간 연결해 놓으면 receive 호출시 blocking 에서 반환안되는 경우가 있음
                     * 그래서 5분주기로 connection 을 새로 연결하도록 변경함
                     * 추후 mqtt library 교체 필요
                     */
                    Message callMsg = callConn.receive(5, TimeUnit.MINUTES);
                    if (callMsg == null) {
                        logger.warn("Heartbeat log : mqtt message is null");
                        continue;
                    }
                    Map<String, String> map = mapper.readValue(callMsg.getPayload(), Map.class);

                    session = map.get("session");     // 통화 ID
                    callerNo = map.get("callerNo");   // 신고자 전화번호
                    sid = 0;
                    lang = "ko";
                    WiniSTT_8k_16bit.setSID(0);
                } else {    // 언어변경일 경우
                    Map<String, String> map = mapper.readValue(lastCallInfo.getBytes(StandardCharsets.UTF_8), Map.class);
                    session = map.get("session");
                    callerNo = map.get("callerNo");
                    sid = WiniSTT_8k_16bit.getLastSID();
                    lang = map.get("lang");
                    logger.info(String.format("[통화언어변경] : [%s],[%s],[%s],[%d]", session, callerNo, lang, sid));
                }

                CallInfo callerInfo = new CallInfo(args, session, callerNo, CallInfo.CallerOrCallee.Caller, sid, lang);
                CallInfo calleeInfo = new CallInfo(args, session, callerNo, CallInfo.CallerOrCallee.Callee, sid, "ko");

                SttAdapter caller, callee;
                if(lang.equals("ko")) {
                    callee = new Selvy3_1_8_3_en();
                    caller = new Selvy3_1_8_3_en();
                } else if(lang.equals("en")) {
                    caller = new ReadSpeaker();
                    callee = new Selvy3_1_8_3_en();
                } else { // lang.equals("ch")
                    caller = new ReadSpeaker();
                    callee = new Selvy3_1_8_3_en();
                }

                // RT_DS
                /*
                caller = new RT_DS();
                callee = new RT_DS();
                */

                WiniSTT_8k_16bit sttCaller = new WiniSTT_8k_16bit(caller, callerInfo);
                WiniSTT_8k_16bit sttCallee = new WiniSTT_8k_16bit(callee, calleeInfo);

                Future<String> callerThread = executor.submit(sttCaller::callSTT);
                Future<String> calleeThread = executor.submit(sttCallee::callSTT);

                String str1 = callerThread.get();
                String str2 = calleeThread.get();
                if(Objects.isNull(str1)) {
                    lastCallInfo = null;
                } else {
                    lastCallInfo = str2;
                }
            } catch (Exception e) {
                logger.error("", e);
                break;
            } finally {
                if (callConn != null) try { callConn.kill(); callConn.disconnect(); } catch (Exception ignored) {}
            }
        }
    }
}