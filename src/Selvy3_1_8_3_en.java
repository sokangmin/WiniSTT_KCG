import com.selvasai.selvasstt.Lvcsr_Lib;
import com.selvasai.selvasstt.SelvyException;
import com.selvasai.selvasstt.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Selvy3_1_8_3_en implements SttAdapter {
    static final Logger logger = LogManager.getLogger(Selvy3_1_8_3_en.class);
    private Lvcsr_Lib lib;  // Stt Client API
    private LVCSR_RESULT ret;   // API 리턴 정보
    private LVCSR_EPD_INFO epdInfo; // 음성검출 정보
    private String bMidRst = ""; // 이전인식결과
    private String transactionId = "";

    @Override
    public void init(CallInfo info, String transactionId) throws SttException {
        lib = new Lvcsr_Lib();
        this.transactionId = transactionId;
        String errMsg;  // 에러메시지 정보

        // Selvas서버 연결
        try {
            ret = lib.SelvySTT_INIT(info.getSttHost(), info.getSttPort(), 10, 60);
            if (ret != LVCSR_RESULT.LVCSR_SUCCESS) {
                LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                errMsg = String.format("[SelvySTT_INIT ERROR] : [%s] [%s] [%s]", ret, err.getErrorCode(), err.getErrorMsg());
                logger.error(errMsg);
                throw new SttException(errMsg);
            }
        } catch (SelvyException e) {
            throw new SttException(e);
        }

        // 트랜잭션 ID 설정
        LVCSR_DATA_TRANSACTION tranInfo = new LVCSR_DATA_TRANSACTION();
        tranInfo.setStrTransactionId(transactionId);
        try {
            ret = lib.SelvySTT_SET_TRANS(tranInfo);
            if (ret != LVCSR_RESULT.LVCSR_SUCCESS) {
                LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                errMsg = String.format("[SelvySTT_SET_TRANS RETURN] : [%s] [%s] [%s] [%s]", ret, err.getErrorCode(), err.getErrorMsg(), transactionId);
                logger.error(errMsg);
                throw new SttException(errMsg);
            }
        } catch (SelvyException e) {
            throw new SttException(e);
        }

        LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
        reqtimeinfo.setSockTimeOut(10);
        reqtimeinfo.setReadTimeOut(240);
        try {
            ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
            if (ret != LVCSR_RESULT.LVCSR_SUCCESS) {
                LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                errMsg = String.format("[SelvySTT_SET_REQTIME ERROR] : [%s] [%s] [%s]", ret, err.getErrorCode(), err.getErrorMsg());
                logger.error(errMsg);
                throw new SttException(errMsg);
            }
        } catch (SelvyException e) {
            throw new SttException(e);
        }

        try {
            if (!openChannel(info, transactionId)) {
                throw new SttException(String.format("[CHANNEL_OPEN ERROR] : [%s]", transactionId));
            }
        } catch (SelvyException e) {
            throw new SttException(e);
        }
        epdInfo = new LVCSR_EPD_INFO();
    }

    @Override
    public RecogData send_data(byte[] bArr) {
        String sMidRst;    // 인식결과

        try {
            ret = lib.SelvySTT_SEND_DATA(bArr, bArr.length, 0, epdInfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
                LVCSR_EPD_STAT output = epdInfo.getOutput();
                if (output == LVCSR_EPD_STAT.RECEIV_OK_SPEECH || output == LVCSR_EPD_STAT.RECEIV_OK || output == LVCSR_EPD_STAT.SECTION_FOUND) {
                    LVCSR_RECOG_MID_RESULT resultMidInfo = new LVCSR_RECOG_MID_RESULT();
                    ret = lib.SelvySTT_GET_MIDRES(resultMidInfo);
                    if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
                        if (resultMidInfo.getEngineDetectionFlag() != 1) {
                            if (resultMidInfo.getResultLen() > 0) {
                                sMidRst = resultMidInfo.getStrResult();
                                if (!(bMidRst.equals(sMidRst) || sMidRst.trim().isEmpty())) {  // 이전결과와 동일하거나 결과가 빈값인 경우 제외
                                    bMidRst = sMidRst;
                                    return new RecogData(sMidRst, Epd.PARTIAL);
                                }
                            }
                        } else {
                            sMidRst = resultMidInfo.getStrResult();
                            if (sMidRst != null && !sMidRst.equals("$NO_RESULT$")) {
                                bMidRst = sMidRst;
                                return new RecogData(sMidRst, Epd.EPD_FOUND);
                            }
                        }
                    } else {
                        LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                        logger.error(String.format("SelvySTT_GET_MIDRES ERROR [%s] [%s] [%s] [%s]", ret, err.getErrorCode(), err.getErrorMsg(), transactionId));
                    }
                } else if (output == LVCSR_EPD_STAT.EPD_FOUND) {
                    LVCSR_RECOG_RESULT resultInfo = new LVCSR_RECOG_RESULT();
                    ret = lib.SelvySTT_GET_RES(resultInfo);

                    if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
                        if (resultInfo.getResultLen() > 0) {
                            sMidRst = resultInfo.getStrResult();
                            if (!sMidRst.equals("$NO_RESULT$")) {
                                bMidRst = sMidRst;
                                return new RecogData(sMidRst, Epd.EPD_FOUND);
                            }
                        }
                    } else {
                        LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                        logger.error(String.format("인식결과 REJCT2 [%s] [%s] [%s] [%s]", ret, err.getErrorCode(), err.getErrorMsg(), transactionId));
                    }
                } else {
                    LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                    if (output == LVCSR_EPD_STAT.DURATION_TIME_OVER || output == LVCSR_EPD_STAT.EPD_ERROR || output == LVCSR_EPD_STAT.START_TIME_OVER) {
                        logger.error(String.format("SelvySTT_SEND_DATA RESULT ERROR1 : [%s] [%s] [%s] [%s]", output.name(), err.getErrorCode(), err.getErrorMsg(), transactionId));
                    } else {
                        logger.error(String.format("SelvySTT_SEND_DATA RESULT ERROR2 : [%s] [%s] [%s] [%s]", output.name(), err.getErrorCode(), err.getErrorMsg(), transactionId));
                    }
                }
            } else {
                LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
                logger.error(String.format("SelvySTT_SEND_DATA ERROR : [%s] [%s] [%s] [%s]", ret.name(), err.getErrorCode(), err.getErrorMsg(), transactionId));
            }
        } catch (SelvyException e) {
            logger.error("", e);
        }
        return null;
    }

    @Override
    public RecogData send_final_data(byte[] bArr) {
        try {
            lib.SelvySTT_SEND_DATA(null, 0, 1, epdInfo);
            if (epdInfo.getOutput() == LVCSR_EPD_STAT.EPD_FOUND) {
                LVCSR_RECOG_MID_RESULT resultMidInfo = new LVCSR_RECOG_MID_RESULT();
                lib.SelvySTT_GET_MIDRES(resultMidInfo);

                LVCSR_RECOG_RESULT resultInfo = new LVCSR_RECOG_RESULT();
                ret = lib.SelvySTT_GET_RES(resultInfo);

                if(!resultMidInfo.getStrResult().trim().isEmpty())
                    return new RecogData(resultMidInfo.getStrResult(), Epd.EPD_FOUND);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    @Override
    public void close() {
        try {
            Objects.requireNonNull(lib).SelvySTT_CLOS();
        } catch (Exception ignored) {}
        try {
            Objects.requireNonNull(lib).SelvySTT_EXIT();
        } catch (Exception ignored) {}
    }

    private LVCSR_ERROR_RESULT getLvcsrErrorResult() {
        LVCSR_ERROR_RESULT err = new LVCSR_ERROR_RESULT();
        lib.SelvySTT_GET_ERROR(err);
        return err;
    }

    private boolean openChannel(CallInfo info, String transactionId) throws SelvyException {
        // 채널 설정 및 연결
        LVCSR_DATA_INFO dataInfo = new LVCSR_DATA_INFO();
        dataInfo.setModelID(0);
        dataInfo.setCodecType(LVCSR_TYPE_CODEC.getLvcsrTypeCodecById(info.getCodec()));
        dataInfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
        dataInfo.setEpdUsed(LVCSR_USED_EPD.CHUNK_EPD_USED_ON);
        dataInfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
        dataInfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_OFF);
        dataInfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_OFF);
        dataInfo.setKwdID(-1);
        dataInfo.setUserDictCnt(0);
        LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
        dataInfo.setUserDictID(userDictIDinfo);
//        dataInfo.setBoostWordCnt(0);
//        LVCSR_DATA_BOOSTWORD[] boostWordinfo = null;
//        dataInfo.setDataBoostWord(boostWordinfo);
        ret = lib.SelvySTT_OPEN(dataInfo);
        if (ret != LVCSR_RESULT.LVCSR_SUCCESS) {
            LVCSR_ERROR_RESULT err = getLvcsrErrorResult();
            logger.error(String.format("[SelvySTT_OPEN ERROR] : [%s] [%s] [%s] [%s]", ret, err.getErrorCode(), err.getErrorMsg(), transactionId));
            return false;
        }
        return true;
    }
}


