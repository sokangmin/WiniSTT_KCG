import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.fusesource.mqtt.client.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class WiniSTT {
    static final Logger logger = LogManager.getLogger(WiniSTT.class);
    private final SttAdapter stt;
    private final CallInfo info;
    private boolean bEpdEnd = true;
    private static final AtomicInteger gSID = new AtomicInteger(0);
    private int sid;
    private String sDate;

    public WiniSTT(SttAdapter stt, CallInfo info) {
        this.stt = stt;
        this.info = info;
    }

    public String callSTT() {
        BlockingConnection voiceNendConn = null, sttConn = null;
        ByteBuf buf = Unpooled.buffer(3200);
        int len = 1600; // 20ms : 160bytes => 200ms : 1600byte
        String transactionId= "";
        String lastCallInfo;
        /*
         * alaw
         * 샘플링 8KHZ, depth 8bit
         * 1초에 8bit * 8000 = 64000 bit(64kbps) = 1초당 8000bytes
         * G.711은 20ms마다 패킷 발생: 20ms마다 160byte 생성
         */

        try {
            /*
             * 음성 및 통화종료정보 수신 MQ cli
             * 교환기 연계모듈에서 교환기로부터 음성정보가 연계되면 'call/voice/수보자 전화기 ip/신고자,수보자 구분/코덱정보' 토픽에
             * 음성정보를 저장
             * CTI 연계모듈에서 CTI 로부터 통화종료정보가 연계되면 'call/end/수보자전회기내선번호' 토픽에 전화수신정보를 저장
             */
            MQTT voiceNendMqtt = new MQTT();
            voiceNendMqtt.setHost(info.getSrcMqttUrl());
            voiceNendMqtt.setUserName(info.getSrcMqttId());
            voiceNendMqtt.setPassword(info.getSrcMqttPwd());

            // RT_DS
            /*
            String str = "call2/voice/" + info.getCalleeNo() + "/" +
                    info.getCallerOrCallee().getValue() + "/#";

            Topic[] voiceNendTopics = {new Topic("call2/voice/" + info.getCalleeNo() + "/" +
                    info.getCallerOrCallee().getValue() + "/#", QoS.AT_MOST_ONCE),
                    new Topic("call2/end/" + info.getCalleeNo(), QoS.AT_MOST_ONCE),
                    new Topic("call2/change/" + info.getCalleeNo(), QoS.AT_MOST_ONCE)};
             */
            String str = "call2/voice/" + info.getCalleeNo() + "/" +
                    info.getCallerOrCallee().getValue() + "/#";

            Topic[] voiceNendTopics = {new Topic("call2/voice/" + info.getCalleeNo() + "/" +
                    info.getCallerOrCallee().getValue() + "/#", QoS.AT_MOST_ONCE),
                    new Topic("call2/end/" + info.getCalleeNo(), QoS.AT_MOST_ONCE),
                    new Topic("call2/change/" + info.getCalleeNo(), QoS.AT_MOST_ONCE)};
            voiceNendConn = voiceNendMqtt.blockingConnection();
            voiceNendConn.connect();
            voiceNendConn.subscribe(voiceNendTopics);

            /*
             * STT 결과정보 송신 MQ cli
             * STT 결과정보를 cs/stt 토픽으로 전송
             */
            MQTT sttMqtt = new MQTT();
            sttMqtt.setHost(info.getSttMqttUrl());
            sttMqtt.setUserName(info.getSttMqttId());
            sttMqtt.setPassword(info.getSttMqttPwd());
            sttConn = sttMqtt.blockingConnection();
            sttConn.connect();

            // STT 연결
            transactionId = "119_" + info.getSession() + "_" + info.getCallerOrCallee().getValue();
            init(info, transactionId);

            logger.info("call/start(" + transactionId + ")");
            // 음성메시지 처리
            logger.debug("Send Audio Data");
            while (true) {
                Message msg = voiceNendConn.receive();
                String topicNm = msg.getTopic();
                if(topicNm.startsWith("call2/end")) {
                    logger.info("call/end(119_" + info.getSession() + "_" + info.getCallerOrCallee().getValue() + ")");
                    return null;
                } else if (topicNm.startsWith("call2/change")) {
                    logger.info("call/change(119_" + info.getSession() + "_" + info.getCallerOrCallee().getValue() + ")");
                    lastCallInfo = new String(msg.getPayload(), StandardCharsets.UTF_8);
                    break;
                }

                byte[] payload = msg.getPayload();
                msg.ack();
                buf.writeBytes(payload);

                if(buf.readableBytes() >= len) {
                    byte[] bArr = new byte[buf.readableBytes()];
                    buf.readBytes(bArr);
                    buf.clear();
                    String result = send_data(bArr);
                    if (Strings.isNotEmpty(result)) {
                        logger.debug("call/stt(" + transactionId + ") : " + result);
                        sttConn.publish("cs/stt", result.getBytes(), QoS.AT_MOST_ONCE, false);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            return null;
        } finally {
            byte[] bArr = new byte[buf.readableBytes()];
            buf.readBytes(bArr);
            buf.clear();
            ReferenceCountUtil.release(buf);
            logger.debug("Send Audio Final Data");
            String result = send_final_data(bArr);
            if (Strings.isNotEmpty(result)) {
                logger.debug("call/stt(final)(" + transactionId + ") : " + result);
                try {
                    Objects.requireNonNull(sttConn).publish("cs/stt", result.getBytes(), QoS.AT_MOST_ONCE, false);
                } catch (Exception ignored) {
                }
            }
            close();
            if (voiceNendConn != null) try {
                voiceNendConn.kill();
                voiceNendConn.disconnect();
            } catch (Exception ignored) {
            }
            if (sttConn != null) try {
                sttConn.kill();
                sttConn.disconnect();
            } catch (Exception ignored) {
            }
        }

        return lastCallInfo;
    }

    private void init(CallInfo info, String transactionId) throws SttException {
        stt.init(info, transactionId);
    }

    private String send_data(byte[] bArr) {
        if (bArr.length > 1600)
            logger.debug("send_data len > 1600 : " + bArr.length);
        SttAdapter.RecogData recogData = stt.send_data(bArr);
        return getJson(recogData);
    }

    private String send_final_data(byte[] bArr) {
        logger.debug("send_final_data len : " + bArr.length);
        SttAdapter.RecogData recogData = stt.send_final_data(bArr);
        if (recogData != null) recogData.setEpd(SttAdapter.Epd.EPD_FOUND);
        return getJson(recogData);
    }

    private void close() {
        stt.close();
    }

    private String getJson(SttAdapter.RecogData recogData) {
        String sMidRst;
        try {
            switch(Objects.requireNonNull(recogData).epd) {
                case PARTIAL: {
                    if(bEpdEnd) {
                        sid = gSID.incrementAndGet();
                        sDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    }
                    bEpdEnd = false;
                    sMidRst = recogData.text;

                    return result2Json(info, sid, sMidRst, recogData.epd.getValue(), sDate);
                }
                case EPD_FOUND: {
                    if(bEpdEnd) {
                        sid = gSID.incrementAndGet();
                        sDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    }
                    bEpdEnd = true;
                    sMidRst = recogData.text;

                    return result2Json(info, sid, sMidRst, recogData.epd.getValue(), sDate);
                }
                default:
                    return null;
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    ObjectMapper mapper = new ObjectMapper();
    private String result2Json(CallInfo info, int sid, String sMidRst, int sType, String sDate) {
        if(Strings.isNotEmpty(sMidRst)) {
            HashMap<String, String> map = new HashMap<>();
            map.put("sitatnRcdCtnt", sMidRst);
            map.put("spkrDvsCd", String.valueOf(info.getCallerOrCallee().getValue()));
            map.put("telclOppntNo", info.getCallerNo());
            map.put("rcpcntrInlnno", info.getCalleeNo());
            map.put("telclSessionSn", info.getSession());
            map.put("voiceOccrDttm", sDate);
            map.put("cvstSeq", String.valueOf(sid));
            map.put("endPointDvsCd", String.valueOf(sType));
            //map.put("lang", info.getLang());
            /*map.put("stt", sMidRst);
            map.put("callerOrCallee", String.valueOf(info.getCallerOrCallee().getValue()));
            map.put("callerNo", info.getCallerNo());
            map.put("calleeNo", info.getCalleeNo());
            map.put("session", info.getSession());
            map.put("date", sDate);
            map.put("sid", String.valueOf(sid));
            map.put("stype", String.valueOf(sType));
            map.put("lang", info.getLang());*/

            String json = null;
            try {
                json = mapper.writeValueAsString(map);
            } catch (JsonProcessingException ignored) {}

            return json;
        } else {
            return null;
        }
    }

    public static int getLastSID() {
        return gSID.get();
    }

    public static void setSID(int sid) {
        gSID.set(sid);
    }
}
