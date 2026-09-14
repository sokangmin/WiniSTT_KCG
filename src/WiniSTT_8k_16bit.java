import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.fusesource.mqtt.client.*;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 16bit, 8k
 * 20ms마다 320byte 생성
 */
public class WiniSTT_8k_16bit {
    static final Logger logger = LogManager.getLogger(WiniSTT_8k_16bit.class);
    private final SttAdapter stt;
    private final CallInfo info;
    private boolean bEpdEnd = true;
    private static final AtomicInteger gSID = new AtomicInteger(0);
    private int sid;
    private String sDate;
    private final int len = 3200; // 20ms : 320bytes => 200ms : 3200byte

    public WiniSTT_8k_16bit(SttAdapter stt, CallInfo info) {
        this.stt = stt;
        this.info = info;
    }

    public String callSTT() throws NoSuchAlgorithmException, KeyManagementException {
        BlockingConnection voiceNendConn = null, sttConn = null;
        ByteBuf buf = Unpooled.buffer(len * 2);
        String transactionId= "";
        String lastCallInfo;
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, Main.trustAllCerts, new SecureRandom());

        try {
            MQTT voiceNendMqtts = new MQTT();
            voiceNendMqtts.setHost(info.getSrcMqttUrl());
            voiceNendMqtts.setUserName(info.getSrcMqttId());
            voiceNendMqtts.setPassword(info.getSrcMqttPwd());
            voiceNendMqtts.setSslContext(sslContext);

            Topic[] voiceNendTopics = {new Topic("call2/+/voice/" + info.getCalleeNo() + "/" +
                    info.getCallerOrCallee().getValue() + "/4", QoS.AT_MOST_ONCE),
                    new Topic("call2/+/stop/" + info.getCalleeNo(), QoS.AT_MOST_ONCE),
                    new Topic("call2/+/change/" + info.getCalleeNo(), QoS.AT_MOST_ONCE)};
            voiceNendConn = voiceNendMqtts.blockingConnection();
            voiceNendConn.connect();
            voiceNendConn.subscribe(voiceNendTopics);

            /*
             * STT 결과정보 송신 MQ cli
             * STT 결과정보를 cs/stt 토픽으로 전송
             */
            MQTT sttMqtts = new MQTT();
            sttMqtts.setHost(info.getSttMqttUrl());
            sttMqtts.setUserName(info.getSttMqttId());
            sttMqtts.setPassword(info.getSttMqttPwd());
            sttMqtts.setSslContext(sslContext);
            sttConn = sttMqtts.blockingConnection();
            sttConn.connect();

            // STT 연결
            transactionId = "122_" + info.getSession() + "_" + info.getCallerOrCallee().getValue();
            init(info, transactionId);

            logger.info("call/start({})", transactionId);
            // 음성메시지 처리
            logger.debug("Send Audio Data");
            while (true) {
                Message msg = voiceNendConn.receive();
                String topicNm = msg.getTopic();
                if(topicNm.matches("^call2/[^/]+/stop/\\d+$")) {
                    logger.info("call/end(122_{}_{})", info.getSession(), info.getCallerOrCallee().getValue());
                    return null;
                } else if (topicNm.matches("^call2/[^/]+/change/\\d+$")) {
                    logger.info("call/change(122_{}_{})", info.getSession(), info.getCallerOrCallee().getValue());
                    lastCallInfo = new String(msg.getPayload(), StandardCharsets.UTF_8);
                    break;
                }

                byte[] payload = msg.getPayload();
                msg.ack();
                buf.writeBytes(payload);

                if(buf.readableBytes() >= len) {
                    byte[] bArr = new byte[buf.readableBytes()];
                    buf.readBytes(bArr);
                    String result = send_data(bArr);
                    if (Strings.isNotEmpty(result)) {
                        logger.debug("call/stt({}) : {}", transactionId, result);
                        sttConn.publish("call2/"+ info.getOrgId()+"/stt/"+info.getCalleeNo()+"/"+info.getCallerOrCallee().getValue(), result.getBytes(), QoS.AT_MOST_ONCE, false);
                    }
                    buf.clear();
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
                logger.debug("call/stt(final)({}) : {}", transactionId, result);
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
        if(bArr.length > len) {
            logger.debug("send_data len > {} : {}", len, bArr.length);
        }
        SttAdapter.RecogData recogData = stt.send_data(bArr);
        return getJson(recogData);
    }

    private String send_final_data(byte[] bArr) {
        logger.debug("send_final_data len: {}", bArr.length);
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
            map.put("stt", sMidRst);
            map.put("callerOrCallee", String.valueOf(info.getCallerOrCallee().getValue()));
            map.put("callerNo", info.getCallerNo());
            map.put("calleeNo", info.getCalleeNo());
            map.put("session", info.getSession());
            map.put("date", sDate);
            map.put("sid", String.valueOf(sid));
            map.put("stype", String.valueOf(sType));
//            map.put("lang", info.getLang());

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
