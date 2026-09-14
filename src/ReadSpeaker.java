import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class ReadSpeaker implements SttAdapter {
    static final Logger logger = LogManager.getLogger(ReadSpeaker.class);
    private Socket sockFd;
    private InputStream in;
    private OutputStream out;
    private final Queue<RecogData> queue = new LinkedList<>();
    private Thread thread;
    private int sendBlockX = 0;
    private String transactionId = "";
    private final ByteBuf sendBuf = Unpooled.buffer(5000);

    private boolean onlyEpd = false;
    public ReadSpeaker(boolean onlyEpd) {
        this.onlyEpd = onlyEpd;
    }

    public ReadSpeaker() {
        this.onlyEpd = false;
    }

    @Override
    public void init(CallInfo info, String transactionId) throws SttException {
        this.transactionId = transactionId;
        String sttIpAddr = info.getSttHost();
        int sttPort = info.getSttPort();

        // ALAW or MULAW
        String sttVoiceType = "MULAW";
        String sttLogKey = "TESTUSER";

        if(!connect2Server(sttIpAddr, sttPort)) {
            logger.error("STT Server connect Error...");
            throw new SttException("STT Server connect Error...");
        }

        if (sendHeaderData(out, sttLogKey, sttVoiceType, transactionId) != Constants.SUCCESS_SEND_DATA) {
            logger.error("Send Header Data Error...");
            throw new SttException("Send Header Data Error...");
        }

        this.thread = startRecvThread(in, queue);
    }

    @Override
    public RecogData send_data(byte[] byteSpeech) {
        sendBuf.clear();

        short sendByteNum = (short) byteSpeech.length;

        sendBuf.writeIntLE(sendBlockX);
        sendBuf.writeShortLE(sendByteNum);
        sendBuf.writeBytes(byteSpeech, 0, sendByteNum);

        /*
         * VST_Send_Audio_Data(Socket sockfd, byte[] data, short len)
         * sockfd : 소켓 아이디
         * data : 바이트 배열 형태의 음성 버퍼
         * len : 버퍼의 길이
         * */
        byte[] sendBytes = new byte[sendBuf.readableBytes()];
        sendBuf.readBytes(sendBytes);
        sendAudioData(out, sendBytes, sendByteNum);
        sendBlockX++;

        return queue.poll();
    }

    @Override
    public RecogData send_final_data(byte[] bArr) {
        sendBuf.clear();
        sendFinalData(out, sendBuf, Constants.AUDIO_TYPE_MULAW, thread);

        return queue.poll();
    }

    @Override
    public void close() {
        ReferenceCountUtil.release(sendBuf);
        if(sockFd != null) {
            disconnect(sockFd);
        }
    }

    private boolean connect2Server(String sttIpAddr, int sttPort) {
        try {
            sockFd = new Socket(sttIpAddr, sttPort);
            sockFd.setSoTimeout(1000*60*5);
            in = sockFd.getInputStream();
            out = sockFd.getOutputStream();
        } catch (IOException e) {
            logger.error("", e);
            return false;
        }
        return true;
    }

    private int sendHeaderData(OutputStream out, String user, String type, String fileName) {
        short headerSize;
        String header;

        ByteBuf headerBuf = Unpooled.buffer(1024);

        if (user.length() < 1 || user.length() > 16) {
            return Constants.ERR_INVALID_USER_LENGTH;
        }
        header = String.format("SLANG=UTF-8&FTYPE=%s&USER=%s&FILENAME=%s&PARTIAL=FALSE", type, user, fileName);
        headerSize = (short) header.length();

        headerBuf.writeShortLE(headerSize);
        headerBuf.writeCharSequence(header, StandardCharsets.UTF_8);

        byte[] data = new byte[headerBuf.readableBytes()];
        headerBuf.readBytes(data);

        try {
            out.write(data, 0, headerSize + 2);
            return Constants.SUCCESS_SEND_DATA;
        } catch (IOException e) {
            logger.error("", e);
            return Constants.ERR_SEND_DATA;
        } finally {
            ReferenceCountUtil.release(headerBuf);
        }
    }

    private Thread startRecvThread(InputStream in, Queue<RecogData> queue) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    recvThread(in);
                } catch (InterruptedException e) {
                    logger.info("RecvThread is interrupted - " + transactionId);
                }
            }

            private void recvThread(InputStream in) throws InterruptedException {
                int ret;
                int retryCount = 0;

                HeaderResult headerResult = new HeaderResult();
                RecogResult recogResult = new RecogResult();
                int prev_new_line = 0;
                String trimResult;

                while (true) {
                    if (retryCount > 5) break;

                    TimeUnit.MILLISECONDS.sleep(20);
                    /*
                     * VST_Recv_Header_Data(Socket sockfd, ref int code ,ref int size)
                     * sockfd : 소켓아이디
                     * code : 결과 코드
                     *   -> 100 : 에러일 경우
                     *   -> 900 : 정상 결과 리턴
                     *   -> 901 : Partial 결과 리턴
                     * size : 받아올 음성인식 결과의 사이즈
                     * */
                    ret = recvHeaderData(in, headerResult);
                    if (ret == Constants.ERR_RECV_RECOG_HEADER_DATA) {
                        retryCount ++;
                        continue;
                    }
                    recogResult.size = headerResult.size;
                    //if(retry_flag == 0); ?????

                    /*
                     * VST_Recv_Recog_Data(Socket sockfd, ref int size, ref string result)
                     * sockfd : 소켓 아이디
                     * size : 음성인식 결과 사이즈
                     * result : 음성인식 결과
                     * */
                    ret = recvRecogData(in, recogResult);
                    trimResult = recogResult.result.replace("\r\n", "");

                    if (ret == Constants.ERR_RECV_RECOG_DATA) {
                        retryCount ++;
                        continue;
                    }

                    if (headerResult.code == 600) {
                        logger.error("recv code : 600");
                    } else if (headerResult.code == 100 || headerResult.code == 900 || headerResult.code == 901) {
                        if (recogResult.size == 0 && prev_new_line == 0) {
                            prev_new_line = 1;
                        } else {
                            prev_new_line = 0;
                        }

                        String sMidRst;
                        // 마지막 일 경우
                        if(headerResult.code == 100) {
                            break;
                        } else if (!onlyEpd && headerResult.code == 901) {
                            if (trimResult.length() > 0) {
                                //logger.info("recvRecogData(901) : " + trimResult);
                                sMidRst = trimResult.split("\\|")[1].trim();
                                queue.offer(new RecogData(sMidRst,Epd.PARTIAL));
                            }
                        } else if (headerResult.code == 900) {
                            if (trimResult.length() > 0) {
                                //logger.info("recvRecogData(900) : " + trimResult);
                                sMidRst = trimResult.split("\t")[2];
                                queue.offer(new RecogData(sMidRst, Epd.EPD_FOUND));
                            }
                        }
                    } else {
                        logger.info("ASR DONE !");
                    }
                    retryCount = 0;
                }
            }
        });
        thread.start();
        return thread;
    }

    private int recvHeaderData(InputStream in, HeaderResult result) {
        ByteBuf byteBuf = null;

        try {
            byte[] bytes = in.readNBytes(4);
            if (bytes.length < 4) return Constants.ERR_RECV_RECOG_HEADER_DATA;
            byteBuf = Unpooled.wrappedBuffer(bytes);

            result.code = byteBuf.readShortLE();
            result.size = byteBuf.readShortLE();
            byteBuf.clear();

            return Constants.SUCCESS_RECV_HEADER_DATA;
        } catch (IOException e) {
            logger.error("", e);
            return Constants.ERR_RECV_RECOG_HEADER_DATA;
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private int recvRecogData(InputStream in, RecogResult result) {
        int remain = result.size;
        ByteBuf byteBuf = null;

        try {
            byte[] bytes = in.readNBytes(remain);
            byteBuf = Unpooled.wrappedBuffer(bytes);

            result.result = byteBuf.toString(StandardCharsets.UTF_8).trim();
            byteBuf.clear();

            return Constants.SUCCESS_RECV_RECOG_DATA;
        } catch (IOException e) {
            logger.error("", e);
            return Constants.ERR_RECV_RECOG_DATA;
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private int sendAudioData(OutputStream out, byte[] data, short len) {
        try {
            out.write(data, 0, len + 6);
            return Constants.SUCCESS_SEND_DATA;
        } catch (IOException e) {
            logger.error("", e);
            return Constants.ERR_SEND_DATA;
        }
    }

    private int sendFinalData(OutputStream out, ByteBuf sendBuf, int type, Thread thread) {
        int endBlockX = -1;
        int sendBlockType;

        short speedFrame = 0;

        if (type == Constants.AUDIO_TYPE_MULAW)
            sendBlockType = 160 * 25;
        else if (type == Constants.AUDIO_TYPE_PCM)
            sendBlockType = 320 * 20;
        else
            sendBlockType = 160 * 25;

        sendBuf.writeIntLE(endBlockX);
        sendBuf.writeShortLE(speedFrame);

        byte[] data = new byte[6 + (sendBlockType) * 4];
        sendBuf.readBytes(data, 0, 6);

        try {
            out.write(data, 0, 6 + (sendBlockType) * 4);
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            return Constants.SUCCESS_SEND_DATA;
        } catch (IOException e) {
            logger.error("", e);
            return Constants.ERR_SEND_DATA;
        }
    }

    private void disconnect(Socket sockFd) {
        try {
            in.close();
            out.close();
            sockFd.close();
        } catch (IOException e) {
            logger.error("",e);
        }
    }
    static class HeaderResult {
        int code;
        int size;
    }

    static class RecogResult {
        int size;
        String result;
    }

    static class Constants {

        static final int AUDIO_TYPE_MULAW = 1;
        static final int AUDIO_TYPE_PCM = 2;

        static final int ERR_INVALID_USER_LENGTH = -100;
        static final int ERR_SEND_DATA = -200;
        static final int ERR_RECV_RECOG_DATA = -300;
        static final int ERR_RECV_RECOG_HEADER_DATA = -310;

        static final int SUCCESS_SEND_DATA = 100;
        static final int SUCCESS_RECV_HEADER_DATA = 200;
        static final int SUCCESS_RECV_RECOG_DATA = 300;
    }
}
