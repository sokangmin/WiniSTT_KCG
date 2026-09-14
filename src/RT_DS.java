import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.mqtt.client.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class RT_DS implements SttAdapter {
    static final Logger logger = LogManager.getLogger(RT_DS.class);
    private BlockingConnection rstConn = null;
    private final Queue<RecogData> queue = new LinkedList<>();
    private Thread thread;
    private String transactionId = "";

    @Override
    public void init(CallInfo info, String transactionId) throws SttException {
        this.transactionId = transactionId;

        try {
            MQTT rstMqtt = new MQTT();
            rstMqtt.setHost(info.getSttMqttUrl());
            rstMqtt.setUserName(info.getSttMqttId());
            rstMqtt.setPassword(info.getSttMqttPwd());
            rstConn = rstMqtt.blockingConnection();
            rstConn.connect();
            Topic[] rstTopics = {new Topic("call2/result/" + info.getCalleeNo() + "/" +
                    info.getCallerOrCallee().getValue(), QoS.AT_MOST_ONCE)};
            rstConn.subscribe(rstTopics);
        } catch (Exception e) {
            logger.error("", e);
        }

        this.thread = startRecvThread(rstConn, queue);
    }

    @Override
    public RecogData send_data(byte[] bArr) {
        return queue.poll();
    }

    @Override
    public RecogData send_final_data(byte[] bArr) {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        return queue.poll();
    }

    @Override
    public void close() {
        if (rstConn != null) try {
            rstConn.kill();
            rstConn.disconnect();
        } catch (Exception ignored) {}
    }

    private Thread startRecvThread(BlockingConnection conn, Queue<RecogData> queue) {
        Thread thread = new Thread(new Runnable() {
            final ObjectMapper mapper = new ObjectMapper();

            @Override
            public void run() {
                try {
                    recvThread(conn);
                } catch(InterruptedException e) {
                    logger.info("RecvThread is interrupted = {}", transactionId);
                } catch(Exception e) {
                    logger.error("",e);
                }
            }

            private void recvThread(BlockingConnection conn) throws Exception {
                while (true) {
                    TimeUnit.MICROSECONDS.sleep(5);
                    Message rstMsg = conn.receive(10, TimeUnit.MILLISECONDS);
                    if (rstMsg != null) {
                        Map<String, String> map = mapper.readValue(rstMsg.getPayload(), Map.class);
                        String epd = String.valueOf(map.get("epd"));
                        String str = map.get("str");
                        if(str != null && str.length() > 0) {
                            try {
                                if(Objects.requireNonNull(epd).equals("1")) {
                                    queue.offer(new RecogData(str, Epd.EPD_FOUND));
                                } else {
                                    queue.offer(new RecogData(str, Epd.PARTIAL));
                                }
                            } catch(NullPointerException epd_null) {
                                queue.offer(new RecogData(str, Epd.PARTIAL));
                            }
                        }
                        rstMsg.ack();
                    }
                }
            }
        });

        thread.start();
        return thread;
    }
}
