import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * 16bit, 8k
 * 20ms마다 320byte 생성
 */
public class WiniSTT_8k_16bit_file {
    static final Logger logger = LogManager.getLogger(WiniSTT_8k_16bit_file.class);
    private final SttAdapter stt;
    private final CallInfo info;
    private boolean bEpdEnd = true;
    private static final AtomicInteger gSID = new AtomicInteger(0);
    private int sid;
    private String sDate;
    private final int len = 3200; // 20ms : 320bytes => 200ms : 3200byte

    public WiniSTT_8k_16bit_file(SttAdapter stt, CallInfo info) {
        this.stt = stt;
        this.info = info;
    }

    public static byte[] loadFileToByteArray(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];

        try (FileInputStream in = new FileInputStream(file)) {
            in.read(fileBytes);
        }

        return fileBytes;
    }

    public String callSTT() {
        ByteBuf buf = Unpooled.buffer(len * 2);
        String transactionId= "";
        String lastCallInfo;
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;

        try {
            File file = new File("./temp/audio_file/" + "test_08k_030.pcm");
            fileInputStream = new FileInputStream(file);
            dataInputStream = new DataInputStream(fileInputStream);

            byte[] buff = new byte[3200]; // 버퍼 크기는 생성되는 크기에 따라 설정 가능
            int nLen = 0;

            // STT 연결
            transactionId = "119_" + info.getSession() + "_" + info.getCallerOrCallee().getValue();
            init(info, transactionId);

            logger.info("call/start({})", transactionId);
            // 음성메시지 처리
            logger.debug("Send Audio Data");
            while ((nLen = dataInputStream.read(buff)) != -1) {
                String result = send_data(buff);
                if (Strings.isNotEmpty(result)) {
                    logger.debug("call/stt({}) : {}", transactionId, result);
                }
            }

            dataInputStream.close();
            fileInputStream.close();

            //if ( -1 == nLen || 0 == nLen) {
            //}

            byte[] bArr = new byte[0];
            logger.debug("Send Audio Final Data");
            String result = send_final_data(bArr);
            if (Strings.isNotEmpty(result)) {
                logger.debug("call/stt(final)({}) : {}", transactionId, result);
            }
        } catch (Exception e) {
            logger.error("", e);
            return null;
        } finally {

            close();
        }

        return null;
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
            map.put("sitatnRcdCtnt", sMidRst);
            map.put("spkrDvsCd", String.valueOf(info.getCallerOrCallee().getValue()));
            map.put("telclOppntNo", info.getCallerNo());
            map.put("rcpcntrInlnno", info.getCalleeNo());
            map.put("telclSessionSn", info.getSession());
            map.put("voiceOccrDttm", sDate);
            map.put("cvstSeq", String.valueOf(sid));
            map.put("endPointDvsCd", String.valueOf(sType));
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
