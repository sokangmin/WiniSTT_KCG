import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.selvasai.selvasstt.Lvcsr_Lib;
import com.selvasai.selvasstt.SelvyException;
import com.selvasai.selvasstt.common.Utils;
import com.selvasai.selvasstt.model.*;

/**
 * @author kkkim
 *
 */
public class AsrLibSampleTest {

	public Lvcsr_Lib lib = null;

	public String pcmPath = "./temp/audio_file/";

	/**
	  * @Method Name : doTestBaseSvc
	  * @작성일 : 2022. 11. 15. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 기본 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestBaseSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 3600);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
	
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}

			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + pTransaction + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
			
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(600);
			reqtimeinfo.setReadTimeOut(600);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);            
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }

            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		Thread.sleep(100);
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							//continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}
            	
            	if (LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
            		LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
					ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
						log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
						if(resultmidinfo.getResultLen() > 0) {
							log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
						}
						int rsltCnt = resultmidinfo.getDataCnt();
						if(rsltCnt > 0) {
							for(int i=0 ; i < rsltCnt ; i++) {
								log(String.format("인식 중간 상세 정보 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
							}
							
							if (LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput())
							{
								log(String.format("인식 구간 정보 [%d, %d, %s, %s]", resultmidinfo.getResultTimeStamp().getStart(), resultmidinfo.getResultTimeStamp().getEnd(), resultmidinfo.getResultTimeStamp().getStartDateTime(), resultmidinfo.getResultTimeStamp().getEndDateTime()));
							}
						}								
					} else {
						log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
					}
            	}            	
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
           
       		while (true) { 
	            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
	            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
	            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
	        		log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
	        		int rsltLen = resultinfo.getResultLen();
	        		if (rsltLen > 0) {
	        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
	        		}
	        		
	        		int rsltCnt = resultinfo.getDataCnt();
	        		if(rsltCnt > 0) {
	        			for(int i=0 ; i < rsltCnt ; i++) {
	        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
	        			}
	        		}
	        		break;
	            } else if (proc_ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	            	log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
	            	Thread.sleep(100);
	            } else {
	            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
	            	break;
	            }
    		}
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	

	/**
	  * @Method Name : doTestBaseSDSvc
	  * @작성일 : 2022. 11. 15. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 기본 결과 기능 함수 소스 - 화자분리
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestBaseSDSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 3600);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
	
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(360);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }

            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }

            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		Thread.sleep(100);
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}
            	
            	if (LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
            		LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
					ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
						log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
						if(resultmidinfo.getResultLen() > 0) {
							log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
						}
						int rsltCnt = resultmidinfo.getDataCnt();
						if(rsltCnt > 0) {
							for(int i=0 ; i < rsltCnt ; i++) {
								log(String.format("인식 중간 상세 정보 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
							}
							
							if (LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput())
							{
								log(String.format("인식 구간 정보 [%d, %d, %s, %s]", resultmidinfo.getResultTimeStamp().getStart(), resultmidinfo.getResultTimeStamp().getEnd(), resultmidinfo.getResultTimeStamp().getStartDateTime(), resultmidinfo.getResultTimeStamp().getEndDateTime()));
							}
						}								
					} else {
						log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
					}
            	}            	
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
           
	  		while (true) {             
	            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
	            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
	            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
	        		log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
	        		int rsltLen = resultinfo.getResultLen();
	        		if (rsltLen > 0) {
	        			log(String.format("인식 결과 [%s] 화자 개수[%d] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getSpkCnt(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
	        		}
	        		
	        		int rsltCnt = resultinfo.getDataCnt();
	        		if(rsltCnt > 0) {
	        			for(int i=0 ; i < rsltCnt ; i++) {
	        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getSpkId(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
	        			}
	        		}
	        		break;
	            } else if (proc_ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	            	log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
	            	Thread.sleep(100);
	            } else {
	            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
	            	break;
	            }
	  		} 
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}
	
	/**
	  * @Method Name : doTestRtGwSvc
	  * @작성일 : 2024. 06. 24. 오전 5:53:03
	  * @작성자 : kkkim
	  * @변경이력 : RtGw 연동  기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestRtGwSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
	
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}

			LVCSR_DATA_CALLINFO callinfo = new LVCSR_DATA_CALLINFO();
			UUID ConnId = Utils.generateUUID();
			String pConnIdStr = ConnId.toString();
			callinfo.setConnIdStr(pConnIdStr);
			//LVCSR_DATE_TIMESTAMP pCallStartTime = new LVCSR_DATE_TIMESTAMP(2024, 6, 5, 14, 30, 0, 0);  // 기준 시간 설정
        	LVCSR_DATE_TIMESTAMP pCallDataTime = new LVCSR_DATE_TIMESTAMP();
        	LVCSR_USED_DIRECTION nDirection = LVCSR_USED_DIRECTION.DIRECTION_USED_UNK;
        	LVCSR_USED_SEG nSpeakerSegmentation = LVCSR_USED_SEG.SEG_USED_RX;
			callinfo.setCallDateTime(pCallDataTime);
			callinfo.setCallDirection(nDirection);
			callinfo.setSpeakerSegmentation(nSpeakerSegmentation);
			ret = lib.SelvySTT_SET_CALLINFO(callinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_CALLINFO RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_CALLINFO ERROR] : " + ret);
				return;
			}
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}        			
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);          
			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }

            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		Thread.sleep(100);
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 상세 정보 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
									
									if (LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput())
									{
										log(String.format("인식 구간 정보 [%d, %d, %s, %s]", resultmidinfo.getResultTimeStamp().getStart(), resultmidinfo.getResultTimeStamp().getEnd(), resultmidinfo.getResultTimeStamp().getStartDateTime(), resultmidinfo.getResultTimeStamp().getEndDateTime()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	} 
            	
            	if (LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
            		LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
					ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
						log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
						if(resultmidinfo.getResultLen() > 0) {
							log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
						}
						int rsltCnt = resultmidinfo.getDataCnt();
						if(rsltCnt > 0) {
							for(int i=0 ; i < rsltCnt ; i++) {
								log(String.format("인식 중간 상세 정보 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
							}
							
							if (LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput())
							{
								log(String.format("인식 구간 정보 [%d, %d, %s, %s]", resultmidinfo.getResultTimeStamp().getStart(), resultmidinfo.getResultTimeStamp().getEnd(), resultmidinfo.getResultTimeStamp().getStartDateTime(), resultmidinfo.getResultTimeStamp().getEndDateTime()));
							}
						}								
					} else {
						log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
					}
            	}
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  

            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        			
					log(String.format("인식 구간 정보 [%d, %d, %s, %s]", resultinfo.getResultTimeStamp().getStart(), resultinfo.getResultTimeStamp().getEnd(), resultinfo.getResultTimeStamp().getStartDateTime(), resultinfo.getResultTimeStamp().getEndDateTime()));
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	
	/**
	  * @Method Name : doTestBatchSvc
	  * @작성일 : 2024. 06. 24. 오전 5:53:03
	  * @작성자 : kkkim
	  * @변경이력 : RtGw 연동  기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestBatchSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
	
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}

			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}        			
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }

            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            byte[] fileBytes = loadFileToByteArray(pcmPath + fileName);
    		int bButtonComplete = 1;
    		ret = lib.SelvySTT_SEND_DATA(fileBytes, fileBytes.length, bButtonComplete, epdinfo);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
        		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
        			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
        			return;
        		} else {
        			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
        		}
    		} else {
            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
            	return;
    		}    
                
    		while (true) { 
	            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
	            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
	            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
	        		log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
	        		int rsltLen = resultinfo.getResultLen();
	        		if (rsltLen > 0) {
	        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
	        		}
	        		
	        		int rsltCnt = resultinfo.getDataCnt();
	        		if(rsltCnt > 0) {
	        			for(int i=0 ; i < rsltCnt ; i++) {
	        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
	        			}
	        		}
	        		break;
	            } else if (proc_ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	            	log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
	            	Thread.sleep(100);
	            } else {
	            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
	            	break;
	            }
    		}
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
		
	/**
	  * @Method Name : doTestEngSvc
	  * @작성일 : 2022. 11. 15. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 영어 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestEngSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}  					
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }

			LVCSR_DATA_SAMPLERATECONV dataSamplerateinfo = new LVCSR_DATA_SAMPLERATECONV();
			dataSamplerateinfo.setConvUsed(LVCSR_USED_CONVERTER.CONVERTER_USED_ON);
			dataSamplerateinfo.setSrcSampleRate(LVCSR_USED_SAMPLERATE.SAMPLE_ALAW_8K);
			dataSamplerateinfo.setDestSampleRate(LVCSR_USED_SAMPLERATE.SAMPLE_16K);	
            ret = lib.SelvySTT_SET_SAMPLERATE(dataSamplerateinfo);
            if (ret == LVCSR_RESULT.LVCSR_FAIL) {
            	log("[SelvySTT_SET_SAMPLERATE ERROR] : " + ret);	
            	return;
            } else {
            	log("[SelvySTT_SET_SAMPLERATE RETURN] : " + ret);
            }	
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_16K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }

            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  

            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	

	/**
	  * @Method Name : doTestBaseSvc
	  * @작성일 : 2022. 11. 15. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 기본 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestBaseLogInfoSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }        
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			String tloItemSId = "01012341234";
			String tloItemDevInfo = "PHONE";
			String tloItemOsInfo = "ios_6";
			String tloItemNwInfo = "4G";
			String tloItemDevModel = "LE-E250";
			String tloItemCarrierType = "L";
			String tloItemScnName = "서비스시나리오";
			
		    long l = System.currentTimeMillis();
		    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("YYYYMMddHH24mmss");
		    String tloItemCallId = "CALL_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";		    
		    String tloItemTransactionId = "TR_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";		    
		    String tloItemStartMessage = localSimpleDateFormat.format(Long.valueOf(l));
		      
			LVCSR_DATA_LOGINFO loginfo = new LVCSR_DATA_LOGINFO();
			loginfo.setSID(tloItemSId);
			loginfo.setDevInfo(tloItemDevInfo);
			loginfo.setOsInfo(tloItemOsInfo);
			loginfo.setNwInfo(tloItemNwInfo);
			loginfo.setDevModel(tloItemDevModel);
			loginfo.setCarrierType(tloItemCarrierType);
			loginfo.setScnName(tloItemScnName);
			loginfo.setCallID(tloItemCallId);
			loginfo.setTransactionID(tloItemTransactionId);
			loginfo.setStartMessage(tloItemStartMessage);    
		      
			ret = lib.SelvySTT_SET_LOGINFO(loginfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_LOGINFO RETURN] : " + ret);	
            } else if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_LOGINFO ERROR] : " + ret);	
            	return;
            }
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
			LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
            	}
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
            datainfo.setUserDictCnt(0);          
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                               
            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	

	/**
	  * @Method Name : doTestTPSvc
	  * @작성일 : 2024. 03. 13. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 후처리 적용 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestTPSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd, LVCSR_TYPE_CODEC nCodec)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			}
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}        			
        		}
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(nCodec);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
            int[] nUserDictID = { 0 };
            datainfo.setUserDictCnt(nUserDictID.length);          
            LVCSR_DATA_USERDICTID[] userDictIDinfo = new LVCSR_DATA_USERDICTID[(int) datainfo.getUserDictCnt()];
            for (int nDictCnt = 0; nDictCnt < datainfo.getUserDictCnt(); nDictCnt++) {
            	LVCSR_DATA_USERDICTID userDictinfoItem = new LVCSR_DATA_USERDICTID();
            	userDictinfoItem.setUserDictID(nUserDictID[nDictCnt]);
            	userDictIDinfo[nDictCnt] = userDictinfoItem;
    		}   
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            LVCSR_DATA_TP_PROC tpproc = new LVCSR_DATA_TP_PROC();
            tpproc.setSpmUsed(LVCSR_USED_SPM.SPM_USED_OFF);
            tpproc.setItnUsed(LVCSR_USED_ITN.ITN_USED_ON);	// ITN
            tpproc.setDidUsed(LVCSR_USED_DID.DID_USED_OFF);	// de-identification
            tpproc.setFillerUsed(LVCSR_USED_FILLER.FILLER_USED_ON);
            tpproc.setSentUsed(LVCSR_USED_SENT.SENT_USED_ON);
            tpproc.setUDictUsed(LVCSR_USED_UDICT.UDICT_USED_ON);	
            ret = lib.SelvySTT_SET_TPPROC(tpproc);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_TPPROC RETURN] : " + ret);
            } else {
            	log("[SelvySTT_SET_TPPROC ERROR] : " + ret);	
            	return;            	
            }
            
            LVCSR_RULE_LIST datarule = new LVCSR_RULE_LIST();            
            datarule.setRuleType(LVCSR_TYPE_RULE.RULE_AGE.getValue());
            datarule.setRuleUsed(LVCSR_USED_RULE.RULE_USED_ON);
            ret = lib.SelvySTT_SET_ITNRULE(datarule);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_ITNRULE RETURN] : " + ret);
            } else {
            	log("[SelvySTT_SET_ITNRULE ERROR] : " + ret);	
            	return;
            }
            
            datarule = new LVCSR_RULE_LIST();            
            datarule.setRuleType(LVCSR_TYPE_RULE.RULE_AGE.getValue());
            datarule.setRuleUsed(LVCSR_USED_RULE.RULE_USED_ON);
            ret = lib.SelvySTT_SET_DIDRULE(datarule);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_DIDRULE RETURN] : " + ret);
            } else {
            	log("[SelvySTT_SET_DIDRULE ERROR] : " + ret);	
            	return;
            }       
            
        	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();
        	int nCnt = 0;
        	
            while (true) {
            	nCnt++;
            	if (nCnt > 15)
            	{
            		break;
            	}
            	
                datainfo = new LVCSR_DATA_INFO();
                datainfo.setModelID(ModelID);
                datainfo.setCodecType(nCodec);
                datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
                datainfo.setEpdUsed(bEpd);  
                datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
                datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_OFF);
                datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
    			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
                //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
                datainfo.setKwdID(-1);
                ret = lib.SelvySTT_SET_INFO(datainfo);  
                if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
                	log("[SelvySTT_SET_INFO RETURN] : " + ret);
                } else {
                	log("[SelvySTT_SET_INFO ERROR] : " + ret);	
                	return;
	        	}
	
                if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
	            {
		            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
		            if (nCnt <= 3 || nCnt > 10)
		            {
		                datatimeout.setStartTimeout(1);
		            }
		            else
		            {
		                datatimeout.setStartTimeout(20);
		            }

		            datatimeout.setDurationTimeout(60);
		            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
		            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
		            } else {
		            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
		            	return;
		            }     
		            
		            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
		            datamargin.setEpdMargin(0.7f);
		            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
		            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
		            } else {
		            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
		            	return;
		            }  
	            }
	            
	            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
	            datathreshold.setEpdThreshold(8);
	            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
	            	return;
	            }
	            
	            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
	            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
	            	return;
	            }	            
	
	            FileInputStream fileInputStream = null;
	            DataInputStream dataInputStream = null;  
	            try { 
	            	File file = new File(pcmPath + fileName);
	            	fileInputStream = new FileInputStream(file);
	            	dataInputStream = new DataInputStream(fileInputStream);
	            	
	            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	            	
	            	epdinfo = new LVCSR_EPD_INFO();
	            	int nLen = 0;
	            	int bButtonComplete = 0;
	            	
	            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
	            		Thread.sleep(100);
	            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
						if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
							if (LVCSR_EPD_STAT.START_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
								log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
								break;
							}
							if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
								LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
								ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
								if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
									log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
									if(resultmidinfo.getResultLen() > 0) {
										log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
									}
									int rsltCnt = resultmidinfo.getDataCnt();
									if(rsltCnt > 0) {
										for(int i=0 ; i < rsltCnt ; i++) {
											log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
										}
									}
								} else {
									log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
								}
								log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
								continue;
							}
						} else {
			            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
			            	return;
						}
	            	}                
	            	dataInputStream.close();
	            	fileInputStream.close();
	            	            	
	            	if (LVCSR_EPD_STAT.START_TIME_OVER == epdinfo.getOutput())
	            	{
	            		continue;
	            	}
	            	else
	            	{
		            	if (-1 == nLen || 0 == nLen) {
		            		bButtonComplete = 1;
		            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
		            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
			            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
			            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
			            			break;
			            		} else {
			            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
			            		}
		            		} else {
				            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
				            	break;
		            		}            		
		            	}  
	            	}	            	
	            } catch ( IOException e ) {
	            	throw e;
	            } finally {
	            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
	            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
	            }  
	                               
	            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
	            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
	            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
	        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
	        		int rsltLen = resultinfo.getResultLen();
	        		if (rsltLen > 0) {
	        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
	        		}
	        		
	        		int rsltCnt = resultinfo.getDataCnt();
	        		if(rsltCnt > 0) {
	        			for(int i=0 ; i < rsltCnt ; i++) {
	        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
	        			}
	        		}
	            } else {
	            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
	                continue;
	            }
            }            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
			
	/**
	  * @Method Name : doTestNbestSvc
	  * @작성일 : 2022. 11. 15. 오후 1:12:09
	  * @작성자 : kkkim
	  * @변경이력 : nBEST 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  */
	public void doTestNbestSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
    		if (bSSLUsed) {
	    		ret = lib.SelvySTT_SSL(null, null);
	    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	    	    	log("[SelvySTT_SSL RETURN] : " + ret);
	    	    } else {
	    	    	log("[SelvySTT_SSL ERROR] : " + ret);
	    	    	return;
	    	    }
    		}
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
            
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);	
			} else {
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;
			}
			
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
			
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);            	
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();             
            datainfo.setModelID(0);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
            datainfo.setEpdUsed(LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
               
          	if (datainfo.getEpdUsed() == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || datainfo.getEpdUsed() == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || datainfo.getEpdUsed() == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)            	
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(60);
	            datatimeout.setDurationTimeout(300);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);	
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);	
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            
            
            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {            		
	            		if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
	            			break;
	            		}
	            		if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput()) {
	                        LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
	                        ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
	                        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
	                    		log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
	                            if(resultmidinfo.getResultLen() > 0) {
	                    			log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
	                    		}  
	                            
	                    		int rsltCnt = resultmidinfo.getDataCnt();
	                    		if(rsltCnt > 0) {
	                    			for(int i=0 ; i < rsltCnt ; i++) {
	                    				log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
	                    			}
	                    		}
	                        } else {
	                        	log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
	                        } 
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
	            			continue;
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {   
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
            
          	LVCSR_RECOG_NBEST_RESULT nBestResultinfo = new LVCSR_RECOG_NBEST_RESULT();
        	LVCSR_RESULT nbest_ret = lib.SelvySTT_GET_NBESTRES(nBestResultinfo);
        	if (nbest_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
        		log("[SelvySTT_GET_NBESTRES RETURN] : " + ret + " 음성파일:"+ fileName +" 개수:" + nBestResultinfo.getNbestCnt());
        		int nbestCnt = nBestResultinfo.getNbestCnt();
        		if(nbestCnt > 0) {
        			for(int resCnt=0 ; resCnt < nbestCnt ; resCnt++) {
        				log(String.format("인식 결과 #%d[%s, %s, %f]", resCnt+1, fileName, nBestResultinfo.getDataNbestResult()[resCnt].getStrResult(), nBestResultinfo.getDataNbestResult()[resCnt].getConfidScore()));
        				int rsltCnt = nBestResultinfo.getDataNbestResult()[resCnt].getDataCnt();
                		if(rsltCnt > 0) {
                			for(int dataCnt=0 ; dataCnt < rsltCnt ; dataCnt++) {
                				log(String.format("인식 결과 #%d-%d[%s, %d, %d, %4.3f]", resCnt+1, dataCnt+1, nBestResultinfo.getDataNbestResult()[resCnt].getDataResult()[dataCnt].getStrToken(), nBestResultinfo.getDataNbestResult()[resCnt].getDataResult()[dataCnt].getStart(), nBestResultinfo.getDataNbestResult()[resCnt].getDataResult()[dataCnt].getEnd(), nBestResultinfo.getDataNbestResult()[resCnt].getDataResult()[dataCnt].getScore()));               				
                			}
                		}
        			}
        		}        		
        		log(String.format("시작Frame[%d] 끝Frame[%d]", nBestResultinfo.getDataEPD().getStart(), nBestResultinfo.getDataEPD().getEnd()));
        	} else {
        		log(String.format("SelvySTT_GET_NBESTRES [%s]", fileName));
        		return;
        	}        	
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	
	/**
	 * @MethodName 	: doTestPhonicsSvc
	 * @Date   		: 2019. 06. 01. 오후 11:18:30 
	 * @Author		: kkkim
	 * @Description : 파닉스 결과 기능 함수 소스
	 * 
	 * @param cHost
	 * @param uPort
	 * @param fileName
	 * @param bSSLUsed
	 * @param ModelID
	 * @param bAsync
	 * @param bEpd
	 * @param bSavannaUsed
	 */
	public void doTestPhonicsSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_ASYNC bAsync, LVCSR_USED_EPD bEpd, boolean bSavannaUsed, boolean bPhonicsMethod, String StrPhonics)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
    		if (bSSLUsed) {
	    		ret = lib.SelvySTT_SSL(null, null);
	    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	    	    	log("[SelvySTT_SSL RETURN] : " + ret);
	    	    } else {
	    	    	log("[SelvySTT_SSL ERROR] : " + ret);
	    	    	return;
	    	    }
    		}
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
            
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);	
			} else {
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;
			}
			
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String nameWithoutExt = Paths.get(fileName).getFileName().toString().replaceFirst("[.][^.]+$", "");
			System.out.println(nameWithoutExt);
			String pTransaction = nameWithoutExt + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			}
			
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
			LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();
			ret = lib.SelvySTT_GET_MODEL(modelinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_GET_MODEL RETURN] : " + ret);
				log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));
				boolean bSpacingUsed = false;
				boolean bSentUsed = false;
	            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
					
				boolean bOtfUsed = false;  
				boolean bQtmUsed = false;
				LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
					switch((int)nModelType & 0x00F0)
					{
						case 0x0010:
							bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
							break;
						case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
			       			break;
						case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
							break;
						case 0x0040:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = true;
			      			bDidUsed = false;
							break;
						case 0x0050:
							bKwdUsed = false;
			 				bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = true;
							bDidUsed = true;
							break;
						default:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
							break;
					}
		        	        
					switch ((int)nModelType & 0xF000) 
					{
						case 0x1000:
							bOtfUsed = true;
							bQtmUsed = false;
							break;
						case 0x2000:
							bOtfUsed = false;
							bQtmUsed = true;
							break;
						default:
							bOtfUsed = false;
							bQtmUsed = false;
							break;
					}
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
					for(int j=0 ; j < nKwdCnt ; j++) {
						int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
						String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
						log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
					} 
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_16K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(bAsync);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);			
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
            datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            datainfo.setLimitTokenCnt(0);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);
            	return;
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(10);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            
            
            LVCSR_DATA_PHONICS phonicsinfo = new LVCSR_DATA_PHONICS();
            if (bSavannaUsed) {
	            LVCSR_DATA_PHONICS_TYPE pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
	            LVCSR_TYPE_PHONICS nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE;
	            pDataPhonicsType.setPhonicsType(nPhonicsType);
	            ret = lib.SelvySTT_GET_PHONICS(pDataPhonicsType, phonicsinfo);  
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_GET_PHONICS RETURN] : " + ret);	
	            	log(String.format("PHONICS 단어 [%d, %d, %s]", phonicsinfo.getPhonicsType().getValue(), phonicsinfo.getPhonicsID(), phonicsinfo.getPhonicsStr()));
	            } else {
	            	log("[SelvySTT_GET_PHONICS ERROR] : " + ret);
	            	return;
	            }    
	            
	            LVCSR_DATA_PHONICS_WORD phonicsWordinfo = new LVCSR_DATA_PHONICS_WORD();
	            ret = lib.SelvySTT_GET_PHONICSWORD(phonicsWordinfo);  
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_GET_PHONICSWORD RETURN] : " + ret);	
	                if(phonicsWordinfo.getPhonicsWordCnt() > 0) {
	                	int rsltCnt = phonicsWordinfo.getPhonicsWordCnt();
	            		for(int i=0 ; i < rsltCnt ; i++) {
	            			int nPhonicsWordID = phonicsWordinfo.getPhonicsInfo()[i].getPhonicsWordID();
	            			String pPhonicsWord = phonicsWordinfo.getPhonicsInfo()[i].getStrPhonicsWord();
	            			log(String.format("PhonicsWord 결과 #%d[%d, %s]", i+1, nPhonicsWordID, pPhonicsWord));            		}            		
	            	}       
	            } else {
	            	log("[SelvySTT_GET_PHONICSWORD ERROR] : " + ret);
	            	return;
	            }   
            }
		          
            phonicsinfo = new LVCSR_DATA_PHONICS();
            String pPhonicsStr = "";
            if (bPhonicsMethod == false)	// 파닉스 ID
            {
            	int nPhonicsID = Integer.parseInt(StrPhonics);
            	phonicsinfo.setPhonicsID(nPhonicsID);	           	
            }
            else if (bPhonicsMethod == true)	// 파닉스 문자열
            {
                if (bSavannaUsed) {
                	pPhonicsStr = StrPhonics;
                } else {
                	pPhonicsStr = "{";
    			    pPhonicsStr +="\"model_id\":1,";
    			    pPhonicsStr +="\"mod\": \"phonics_ybm\",";
    			    pPhonicsStr +="\"text\": \"hose\",";
    			    pPhonicsStr +="\"syll_index\": 0,";
    			    pPhonicsStr +="\"evaluation\": \"a-nt\"";
    			    pPhonicsStr += " }";
                }
                phonicsinfo.setPhonicsStr(pPhonicsStr);            	
            }
            phonicsinfo.setPhonicsType(LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE);
            ret = lib.SelvySTT_SET_PHONICS(phonicsinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_PHONICS RETURN] : " + ret + " Str: " + pPhonicsStr);	
            } else {
            	log("[SelvySTT_SET_PHONICS ERROR] : " + ret);
            	return;
            }

            if (bSavannaUsed) {	
	            LVCSR_DATA_PHONICS_TYPE pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
	            LVCSR_TYPE_PHONICS nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE;
	            pDataPhonicsType.setPhonicsType(nPhonicsType);		        
		        phonicsinfo = new LVCSR_DATA_PHONICS();
		        ret = lib.SelvySTT_GET_PHONICS(pDataPhonicsType, phonicsinfo);  
		        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		        	log("[SelvySTT_GET_PHONICS RETURN] : " + ret);	
		        	log(String.format("PHONICS 단어 [%d, %s]", phonicsinfo.getPhonicsID(), phonicsinfo.getPhonicsStr()));
		        } else {
		        	log("[SelvySTT_GET_PHONICS ERROR] : " + ret);
		        	return;
		        }

	            phonicsinfo = new LVCSR_DATA_PHONICS();
	            if (bPhonicsMethod == false)
	            {
	            	int nPhonicsID = Integer.parseInt(StrPhonics);
	            	phonicsinfo.setPhonicsID(nPhonicsID);	           	
	            }
	            else if (bPhonicsMethod == true)
	            {
	            	pPhonicsStr = StrPhonics;
	            	phonicsinfo.setPhonicsStr(pPhonicsStr);
	            }
	            phonicsinfo.setPhonicsType(LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE);            	
	            ret = lib.SelvySTT_SET_PHONICS(phonicsinfo);  
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_PHONICS RETURN] : " + ret + " Str: " + pPhonicsStr);	
	            } else {
	            	log("[SelvySTT_SET_PHONICS ERROR] : " + ret);
	            	return;
	            }  
	            
	            pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
	            nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE;
	            pDataPhonicsType.setPhonicsType(nPhonicsType);
	            phonicsinfo = new LVCSR_DATA_PHONICS();
	            ret = lib.SelvySTT_GET_PHONICS(pDataPhonicsType, phonicsinfo);  
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_GET_PHONICS RETURN] : " + ret);	
	            	log(String.format("PHONICS 단어 [%d, %d, %s]", phonicsinfo.getPhonicsType().getValue(), phonicsinfo.getPhonicsID(), phonicsinfo.getPhonicsStr()));
	            } else {
	            	log("[SelvySTT_GET_PHONICS ERROR] : " + ret);
	            	return;
	            }    
            }

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
	            			break;
	            		}
	            		if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput()) {
	            			LVCSR_RECOG_WORD_PHO_MID_RESULT resultwordmidinfo = new LVCSR_RECOG_WORD_PHO_MID_RESULT();
							ret = lib.SelvySTT_GET_PHONICS_WORD_MIDRES(resultwordmidinfo);
                    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) { 
                    			log("[SelvySTT_GET_PHONICS_WORD_MIDRES RETURN] : " + ret + " 문장길이:" + resultwordmidinfo.getJsonLen());
                    			if(resultwordmidinfo.getJsonLen() > 0) {
                    				log(String.format("인식 중간 결과 문장 [%s][%f]", resultwordmidinfo.getStrJson(), resultwordmidinfo.getCorrScore()));
                    			}                    			
                    		} else {
                    			log(String.format("[SelvySTT_GET_PHONICS_WORD_MIDRES ERROR] : " + ret));
                    		}

                    		if (bSavannaUsed) {
								LVCSR_DATA_PHONICS_TYPE pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
								LVCSR_TYPE_PHONICS nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE;
								pDataPhonicsType.setPhonicsType(nPhonicsType);
								LVCSR_RECOG_PHONICS_MID_RESULT resultmidinfo = new LVCSR_RECOG_PHONICS_MID_RESULT();
								ret = lib.SelvySTT_GET_PHONICS_MIDRES(pDataPhonicsType, resultmidinfo);
								if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
									log("[SelvySTT_GET_PHONICS_MIDRES RETURN] : " + ret + " 파닉스 구조체 개수:" + resultmidinfo.getPhonicsCnt());
									int jsonDataCnt = resultmidinfo.getPhonicsCnt();
									if (jsonDataCnt > 0) {
										for(int i=0 ; i < jsonDataCnt ; i++) {
											log(String.format("파닉스 결과 JSON [%s][%f]", resultmidinfo.getDataPhonicsResult()[i].getStrJson(), resultmidinfo.getDataPhonicsResult()[i].getCorrScore()));
										}        			
									}
								} else {
									log(String.format("[SelvySTT_GET_PHONICS_MIDRES ERROR] : " + ret));
								}
                    		} else {
								if (bAsync == LVCSR_USED_ASYNC.ASYNC_USED_OFF ) {
									LVCSR_RECOG_WORD_PHO_RESULT phonicsResultinfo = new LVCSR_RECOG_WORD_PHO_RESULT();
									ret = lib.SelvySTT_GET_PHONICS_WNRRES(phonicsResultinfo);
									if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
										log("[SelvySTT_GET_PHONICS_WNRRES RETURN] : " + ret + " 문장길이:" + phonicsResultinfo.getResultLen() + " 개수:" + phonicsResultinfo.getDataCnt());
										if (phonicsResultinfo.getResultLen() > 0) {
											log(String.format("파닉스 결과 문장 [%s] 시작Frame[%d] 끝Frame[%d] 점수[%f]", phonicsResultinfo.getStrResult(), phonicsResultinfo.getDataEPD().getStart(), phonicsResultinfo.getDataEPD().getEnd(), phonicsResultinfo.getConfidScore()));
										}
										if (phonicsResultinfo.getJsonLen() > 0) {
											log(String.format("파닉스 결과 JSON [%s]", phonicsResultinfo.getStrJson()));
										}           		
										int rsltDataCnt = phonicsResultinfo.getDataCnt();
										if(rsltDataCnt > 0) {
											for(int i=0 ; i < rsltDataCnt ; i++) {
												log(String.format("인식 결과 #%d[%s, %s, %d, %d]", i+1, fileName, phonicsResultinfo.getDataResult()[i].getStrToken(), phonicsResultinfo.getDataResult()[i].getStart(), phonicsResultinfo.getDataResult()[i].getEnd()));
											}
										}                    				
									} else  {                           
										log(String.format("[SelvySTT_GET_PHONICS_WNRRES ERROR] : " + ret));
									}
								}
                    		}
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
	            			continue;
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {   
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());

			            	LVCSR_RECOG_WORD_PHO_MID_RESULT resultwordmidinfo = new LVCSR_RECOG_WORD_PHO_MID_RESULT();
							ret = lib.SelvySTT_GET_PHONICS_WORD_MIDRES(resultwordmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_PHONICS_WORD_MIDRES RETURN] : " + ret + " 문장길이:" + resultwordmidinfo.getJsonLen());
								if(resultwordmidinfo.getJsonLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultwordmidinfo.getStrJson()));
								}    
							}
							
							if (bSavannaUsed) {
				        	    LVCSR_DATA_PHONICS_TYPE pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
				        	    LVCSR_TYPE_PHONICS nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE;
				        	    pDataPhonicsType.setPhonicsType(nPhonicsType);
								LVCSR_RECOG_PHONICS_MID_RESULT resultmidinfo = new LVCSR_RECOG_PHONICS_MID_RESULT();
								ret = lib.SelvySTT_GET_PHONICS_MIDRES(pDataPhonicsType, resultmidinfo);
								if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
									log("[SelvySTT_GET_PHONICS_MIDRES RETURN] : " + ret + " 파닉스 구조체 개수:" + resultmidinfo.getPhonicsCnt());
									int jsonDataCnt = resultmidinfo.getPhonicsCnt();
									if (jsonDataCnt > 0) {
										for(int i=0 ; i < jsonDataCnt ; i++) {
											log(String.format("파닉스 결과 JSON [%s][%f]", resultmidinfo.getDataPhonicsResult()[i].getStrJson(), resultmidinfo.getDataPhonicsResult()[i].getCorrScore()));
										}
									}
								}
								else
								{
									log(String.format("[SelvySTT_GET_PHONICS_MIDRES ERROR] : " + ret));
								}
							}
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
            
            LVCSR_RECOG_WORD_PHO_RESULT phonicsResultWordinfo = new LVCSR_RECOG_WORD_PHO_RESULT();
            LVCSR_RESULT phonics_word_ret = lib.SelvySTT_GET_PHONICS_WORD_RES(phonicsResultWordinfo);
            if (phonics_word_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
        		log("[SelvySTT_GET_PHONICS_WORD_RES RETURN] : " + phonics_word_ret);       		
        		int rsltLen = phonicsResultWordinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("파닉스 결과 문장 [%s] 시작Frame[%d] 끝Frame[%d] 점수[%f]", phonicsResultWordinfo.getStrResult(), phonicsResultWordinfo.getDataEPD().getStart(), phonicsResultWordinfo.getDataEPD().getEnd(), phonicsResultWordinfo.getConfidScore()));
        		}
        		
        		int rsltDataCnt = phonicsResultWordinfo.getDataCnt();
        		if(rsltDataCnt > 0) {
        			for(int i=0 ; i < rsltDataCnt ; i++) {
        				log(String.format("인식 결과 #%d[%s, %s, %d, %d]", i+1, fileName, phonicsResultWordinfo.getDataResult()[i].getStrToken(), phonicsResultWordinfo.getDataResult()[i].getStart(), phonicsResultWordinfo.getDataResult()[i].getEnd()));
        			}
        		}
        		
        		int jsonLen = phonicsResultWordinfo.getJsonLen();
        		if (jsonLen > 0) {
        			log(String.format("파닉스 결과 JSON [%s][%f]", phonicsResultWordinfo.getStrJson(), phonicsResultWordinfo.getCorrScore()));
        		}
        	} else {
        		log(String.format("PHONICS 인식 결과 REJCT2 [%s]", fileName));
        		return;
        	}
		
            if (bSavannaUsed) {
	            LVCSR_DATA_PHONICS_TYPE pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
	            LVCSR_TYPE_PHONICS nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_SENTENCE;
	            pDataPhonicsType.setPhonicsType(nPhonicsType);
				LVCSR_RECOG_PHONICS_RESULT phonicsResultinfo = new LVCSR_RECOG_PHONICS_RESULT();
				LVCSR_RESULT phonics_ret = lib.SelvySTT_GET_PHONICS_RES(pDataPhonicsType, phonicsResultinfo);
				if (phonics_ret == LVCSR_RESULT.LVCSR_FAIL) {
					log(String.format("PHONICS 인식 결과 REJCT2 [%s]", fileName));
					return;
				} else if (phonics_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_GET_PHONICS_RES RETURN] : " + ret);       		
					int rsltLen = phonicsResultinfo.getResultLen();
					if (rsltLen > 0) {
						log(String.format("파닉스 결과 문장 [%s] 시작Frame[%d] 끝Frame[%d] 점수[%f]", phonicsResultinfo.getStrResult(), phonicsResultinfo.getDataEPD().getStart(), phonicsResultinfo.getDataEPD().getEnd(), phonicsResultinfo.getConfidScore()));
					}
	        		
					int rsltDataCnt = phonicsResultinfo.getDataCnt();
					if(rsltDataCnt > 0) {
						for(int i=0 ; i < rsltDataCnt ; i++) {
							log(String.format("인식 결과 #%d[%s, %s, %d, %d]", i+1, fileName, phonicsResultinfo.getDataResult()[i].getStrToken(), phonicsResultinfo.getDataResult()[i].getStart(), phonicsResultinfo.getDataResult()[i].getEnd()));
						}
					}
	        		
					int jsonDataCnt = phonicsResultinfo.getPhonicsCnt();
					if (jsonDataCnt > 0) {
						for(int i=0 ; i < jsonDataCnt ; i++) {
							log(String.format("파닉스 결과 JSON [%s][%f]", phonicsResultinfo.getDataPhonicsResult()[i].getStrJson(), phonicsResultinfo.getDataPhonicsResult()[i].getCorrScore()));
						}
					}
				}
				
	            phonicsinfo = new LVCSR_DATA_PHONICS();
	            if (bPhonicsMethod == false)
	            {
	            	int nPhonicsID = Integer.parseInt(StrPhonics);
	            	phonicsinfo.setPhonicsID(nPhonicsID);	           	
	            }
	            else if (bPhonicsMethod == true)
	            {
	            	pPhonicsStr = StrPhonics;
	            	phonicsinfo.setPhonicsStr(pPhonicsStr);
	            }	            

	            pPhonicsStr = "mother";
	            phonicsinfo.setPhonicsType(LVCSR_TYPE_PHONICS.PHONICS_TYPE_WORD);
	            phonicsinfo.setPhonicsStr(pPhonicsStr);
	            ret = lib.SelvySTT_SET_PHONICS(phonicsinfo);  
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_PHONICS(WORD) RETURN] : " + ret + " Str: " + pPhonicsStr);	
	            } else {
	            	log("[SelvySTT_SET_PHONICS(WORD) ERROR] : " + ret);
	            	return;
	            }  
            
	            pDataPhonicsType = new LVCSR_DATA_PHONICS_TYPE();
	            nPhonicsType = LVCSR_TYPE_PHONICS.PHONICS_TYPE_WORD;
	            pDataPhonicsType.setPhonicsType(nPhonicsType);
	            phonicsResultinfo = new LVCSR_RECOG_PHONICS_RESULT();
	            phonics_ret = lib.SelvySTT_GET_PHONICS_RES(pDataPhonicsType, phonicsResultinfo);
				if (phonics_ret == LVCSR_RESULT.LVCSR_FAIL) {
					log(String.format("PHONICS 인식 결과 REJCT2 [%s]", fileName));
					return;
				} else if (phonics_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_GET_PHONICS_RES(WORD) RETURN] : " + ret);       		
					int rsltLen = phonicsResultinfo.getResultLen();
					if (rsltLen > 0) {
						log(String.format("파닉스 결과 문장 [%s] 시작Frame[%d] 끝Frame[%d] 점수[%f]", phonicsResultinfo.getStrResult(), phonicsResultinfo.getDataEPD().getStart(), phonicsResultinfo.getDataEPD().getEnd(), phonicsResultinfo.getConfidScore()));
					}
	        		
					int rsltDataCnt = phonicsResultinfo.getDataCnt();
					if(rsltDataCnt > 0) {
						for(int i=0 ; i < rsltDataCnt ; i++) {
							log(String.format("인식 결과 #%d[%s, %s, %d, %d]", i+1, fileName, phonicsResultinfo.getDataResult()[i].getStrToken(), phonicsResultinfo.getDataResult()[i].getStart(), phonicsResultinfo.getDataResult()[i].getEnd()));
						}
					}
	        		
					int jsonDataCnt = phonicsResultinfo.getPhonicsCnt();
					if (jsonDataCnt > 0) {
						for(int i=0 ; i < jsonDataCnt ; i++) {
							log(String.format("파닉스 결과 JSON [%s][%f]", phonicsResultinfo.getDataPhonicsResult()[i].getStrJson(), phonicsResultinfo.getDataPhonicsResult()[i].getCorrScore()));
						}
					}
				}
            }
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}
	

	/**
	  * @Method Name : doTestKwdSvc
	  * @작성일 : 2022. 11. 16.
	  * @작성자 : kkkim
	  * @변경이력 : KWD 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestKwdSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, int KWDID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    	return;
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(KWDID);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);        
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
            
            LVCSR_RECOG_KWD_RESULT resultKwdinfo = new LVCSR_RECOG_KWD_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_KWDRES(resultKwdinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_KWDRES RETURN] : " + ret + " 문장길이:" + resultKwdinfo.getResultLen() + " 개수:" + resultKwdinfo.getDataCnt());	            	
        		long rsltLen = resultKwdinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultKwdinfo.getStrResult(), resultKwdinfo.getConfidScore(), resultKwdinfo.getDataEPD().getStart(), resultKwdinfo.getDataEPD().getEnd()));
        		}
        		int rsltCnt = resultKwdinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d, %f]", i+1, fileName, resultKwdinfo.getDataResult()[i].getStrToken(), resultKwdinfo.getDataResult()[i].getStart(), resultKwdinfo.getDataResult()[i].getEnd(), resultKwdinfo.getDataResult()[i].getScore()));
        			}
        		}
    			
    			int kwdCnt = resultKwdinfo.getKWDCnt();
    			if(kwdCnt > 0) {
    				for(int x=0 ; x < kwdCnt ; x++) {
    					String kwdToken = resultKwdinfo.getDataKwd()[x].getToken().toString();
    					log(String.format("KWD 결과 #%d[%s, %s, %d, %d, %4.3f]", x+1, kwdToken, resultKwdinfo.getDataKwd()[x].getSymbol(), resultKwdinfo.getDataKwd()[x].getStart(), resultKwdinfo.getDataKwd()[x].getEnd(), resultKwdinfo.getDataKwd()[x].getScore()));	
    				}
    			}
            } else {
            	log(String.format("SelvySTT_GET_KWDRES [%s]", fileName));
                return;
            }            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
		
	/**
	  * @Method Name : doTestAddrSvc
	  * @작성일 : 2022. 11. 16.
	  * @작성자 : kkkim
	  * @변경이력 : 주소 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param KWDID
	  * @param bEpd
	  */
	public void doTestAddrSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    	return;
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);            	
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);   
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }

            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                  
	        LVCSR_RECOG_ADDR_RESULT resultAddrinfo = new LVCSR_RECOG_ADDR_RESULT();
	        LVCSR_RESULT proc_ret = lib.SelvySTT_GET_ADDRRES(resultAddrinfo);
	        if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
	      		log("[SelvySTT_GET_ADDRRES RETURN] : " + ret + " 문장길이:" + resultAddrinfo.getResultLen() + " 개수:" + resultAddrinfo.getDataCnt());
	      		int rsltLen = resultAddrinfo.getResultLen();
	      		if (rsltLen > 0) {
	      			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultAddrinfo.getStrResult(), resultAddrinfo.getConfidScore(), resultAddrinfo.getDataEPD().getStart(), resultAddrinfo.getDataEPD().getEnd()));
	      		}
	      		int rsltCnt = resultAddrinfo.getDataCnt();
	      		if(rsltCnt > 0) {
	      			for(int i=0 ; i < rsltCnt ; i++) {
	      				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d, %f]", i+1, fileName, resultAddrinfo.getDataResult()[i].getStrToken(), resultAddrinfo.getDataResult()[i].getStart(), resultAddrinfo.getDataResult()[i].getEnd(), resultAddrinfo.getDataResult()[i].getScore()));
	      			}
	      		}
	
	  			int addrUsed = resultAddrinfo.getADDRUsed();   
	  			//System.err.println("addrUsed: " + addrUsed);
	  			if (addrUsed > 0) {
	  				log(String.format("주소 결과 보정 유형 [%b] 보정 주소[%s] 기본주소[%s] 상세주소[%s]", resultAddrinfo.getDataAddr().getCorrectType(), resultAddrinfo.getDataAddr().getZipcode(), resultAddrinfo.getDataAddr().getStrMainAddress(), resultAddrinfo.getDataAddr().getStrAuxAddress()));
	  				log(String.format("주소 결과 시/도 [%s] 시/군/구 main[%s] 시/군/구 sub[%s] 읍/면/동[%s] 리[%s] 지번[%s] 도로명[%s] 건물번호[%s] POI명[%s]", resultAddrinfo.getDataAddr().getSepAddr().getStrSido(), resultAddrinfo.getDataAddr().getSepAddr().getStrSigunguMain(), resultAddrinfo.getDataAddr().getSepAddr().getStrSigunguSub(), resultAddrinfo.getDataAddr().getSepAddr().getStrDong(), resultAddrinfo.getDataAddr().getSepAddr().getStrLi(), resultAddrinfo.getDataAddr().getSepAddr().getStrJibun(), resultAddrinfo.getDataAddr().getSepAddr().getStrRoadName(), resultAddrinfo.getDataAddr().getSepAddr().getStrBldgNo(), resultAddrinfo.getDataAddr().getSepAddr().getStrPoiName()));
	  			}
            } else {
            	log(String.format("SelvySTT_GET_ADDRRES ERROR ["+ proc_ret +"] [%s]", fileName));
                return;
            } 
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	


	/**
	  * @Method Name : doTestSimpleSvc
	  * @작성일 : 2022. 11. 17.
	  * @작성자 : kkkim
	  * @변경이력 : 연동 초소화 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestSimpleSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {	
			LVCSR_DATA_PREPARE dataprepare = new LVCSR_DATA_PREPARE();
			dataprepare.setModelID(ModelID);
			dataprepare.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            dataprepare.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            dataprepare.setEpdUsed(bEpd);  
            dataprepare.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            dataprepare.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            dataprepare.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
			dataprepare.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //dataprepare.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            dataprepare.setKwdID(-1);
            dataprepare.setTransFlag(LVCSR_USED_TRANS_FLAG.TRANS_FLAG_TRANS);
            dataprepare.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            dataprepare.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            dataprepare.setDataSpkDiar(pDataSpkDiar);
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
            dataprepare.setDataAuthentication(authInfo);
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);	
			dataprepare.setDataTransaction(transInfo);
			
			String tloItemSId = "01012341234";
			String tloItemDevInfo = "PHONE";
			String tloItemOsInfo = "ios_6";
			String tloItemNwInfo = "4G";
			String tloItemDevModel = "LE-E250";
			String tloItemCarrierType = "L";
			String tloItemScnName = "서비스시나리오";
			
		    long l = System.currentTimeMillis();
		    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("YYYYMMddHH24mmss");
		    String tloItemCallId = "CALL_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";		    
		    String tloItemTransactionId = "TR_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";
		    log("TransactionId : " + tloItemTransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");
		    String tloItemStartMessage = localSimpleDateFormat.format(Long.valueOf(l));
		      
			LVCSR_DATA_LOGINFO loginfo = new LVCSR_DATA_LOGINFO();
			loginfo.setSID(tloItemSId);
			loginfo.setDevInfo(tloItemDevInfo);
			loginfo.setOsInfo(tloItemOsInfo);
			loginfo.setNwInfo(tloItemNwInfo);
			loginfo.setDevModel(tloItemDevModel);
			loginfo.setCarrierType(tloItemCarrierType);
			loginfo.setScnName(tloItemScnName);
			loginfo.setCallID(tloItemCallId);
			loginfo.setTransactionID(tloItemTransactionId);
			loginfo.setStartMessage(tloItemStartMessage);  
			dataprepare.setDataLoginfo(loginfo);
			dataprepare.setLimitTokenCnt(0);
            
            ret = lib.SelvySTT_PREPARE(cHost, uPort, 10, 60, dataprepare);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_PREPARE RETURN] : " + ret);
            } else {
            	log("[SelvySTT_PREPARE ERROR] : " + ret);	
            	return;
            }
            
            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
                LVCSR_RECOG_SEND_RESULT sendinfo = new LVCSR_RECOG_SEND_RESULT();
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
					ret = lib.SelvySTT_SEND_DATA_EX(buff, nLen, bButtonComplete, sendinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == sendinfo.getEpdInfo().getOutput()
								|| LVCSR_EPD_STAT.EPD_FOUND == sendinfo.getEpdInfo().getOutput()) {
							log("[SelvySTT_SEND_DATA_EX RECV] : " + sendinfo.getEpdInfo().getOutput());
							log("[SelvySTT_SEND_DATA_EX RETURN final] : " + ret + "|| " + sendinfo.getResultLen() + "||" + sendinfo.getDataCnt());
							if (sendinfo.getResultLen() > 0) {
								log(String.format("인식 최종 결과 문장 [%s][%d]", sendinfo.getStrResult(), sendinfo.getEngineDetectionFlag()));
							}
							
							long rsltCnt = sendinfo.getDataCnt();
							if (rsltCnt > 0) {
								for (int i = 0; i < rsltCnt; i++) {
									log(String.format("인식 최종 결과 #%d[%s, %s, %d, %d]", i + 1, fileName,
											sendinfo.getDataResult()[i].getStrToken(),
											sendinfo.getDataResult()[i].getStart(), sendinfo.getDataResult()[i].getEnd()));
								}
							}
							break;
						}
	
						if (LVCSR_EPD_STAT.RECEIV_OK == sendinfo.getEpdInfo().getOutput()) {
							log("[SelvySTT_SEND_DATA_EX RECV] : " + sendinfo.getEpdInfo().getOutput());
							continue;
						} else if (LVCSR_EPD_STAT.RECEIV_OK_SPEECH == sendinfo.getEpdInfo().getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == sendinfo.getEpdInfo().getOutput()) {
							log("[SelvySTT_SEND_DATA_EX RECV] : " + sendinfo.getEpdInfo().getOutput());						
							log("[SelvySTT_SEND_DATA_EX RETURN] : " + ret + "|| " + sendinfo.getResultLen() + "||" + sendinfo.getDataCnt());
							if (sendinfo.getResultLen() > 0) {
								log(String.format("인식 중간 결과 문장 [%s][%d]", sendinfo.getStrResult(), sendinfo.getEngineDetectionFlag()));
							}
							
							int rsltCnt = sendinfo.getDataCnt();
							if (rsltCnt > 0) {
								for (int i = 0; i < rsltCnt; i++) {
									log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i + 1, fileName,
											sendinfo.getDataResult()[i].getStrToken(),
											sendinfo.getDataResult()[i].getStart(), sendinfo.getDataResult()[i].getEnd()));
								}
							}						
							continue;
						} else {
							log("[SelvySTT_SEND_DATA_EX RECV] : " + sendinfo.getEpdInfo().getOutput());
							break;
						}
					} else {
						log("[SelvySTT_SEND_DATA_EX ERROR] : " + ret);
						break;
					}
					
				}
				dataInputStream.close();
				fileInputStream.close();

				if (-1 == nLen || 0 == nLen) {
					bButtonComplete = 1;
					while (true) {
						ret = lib.SelvySTT_SEND_DATA_EX(null, 0, bButtonComplete, sendinfo);
	            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            		if (LVCSR_EPD_STAT.EPD_FOUND != sendinfo.getEpdInfo().getOutput()) {
		            			log(String.format("SelvySTT_SEND_DATA_EX EPD Not Found [%s]", fileName));
		            			return;
		            		} else {
		            			log("[SelvySTT_SEND_DATA_EX RECV] : " + sendinfo.getEpdInfo().getOutput());       
								log("[SelvySTT_SEND_DATA_EX RETURN final] : " + ret + "|| " + sendinfo.getResultLen() + "||" + sendinfo.getDataCnt());
								if (sendinfo.getResultLen() > 0) {
									log(String.format("인식 최종 결과 문장 [%s][%d]", sendinfo.getStrResult(), sendinfo.getEngineDetectionFlag()));
								}
								
								int rsltCnt = sendinfo.getDataCnt();
								if (rsltCnt > 0) {
									for (int i = 0; i < rsltCnt; i++) {
										log(String.format("인식 최종 결과 #%d[%s, %s, %d, %d]", i + 1, fileName,
												sendinfo.getDataResult()[i].getStrToken(),
												sendinfo.getDataResult()[i].getStart(), sendinfo.getDataResult()[i].getEnd()));
									}
								}
		            		}
		            		break;
	    	            } else if (ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	    	            	log("[SelvySTT_SEND_DATA_EX RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + sendinfo.getResultLen() + " 개수:" + sendinfo.getDataCnt());
	    	            	Thread.sleep(100);         		
	            		} else {
			            	log("[SelvySTT_SEND_DATA_EX ERROR] : " + ret);
			            	break;
	            		}
					}
				}

				if (LVCSR_EPD_STAT.DURATION_TIME_OVER == sendinfo.getEpdInfo().getOutput()) {
					bButtonComplete = 1;
					ret = lib.SelvySTT_SEND_DATA_EX(buff, 0, bButtonComplete, sendinfo);
					if (LVCSR_EPD_STAT.EPD_FOUND != sendinfo.getEpdInfo().getOutput()) {
						log(String.format("SelvySTT_SEND_DATA_EX ERROR [%s]", fileName));
						return;
					} else {
						log("[SelvySTT_SEND_DATA_EX RECV] : " + sendinfo.getEpdInfo().getOutput());
						log("[SelvySTT_SEND_DATA_EX RETURN final] : " + ret + "|| " + sendinfo.getResultLen() + "||" + sendinfo.getDataCnt());
						if (sendinfo.getResultLen() > 0) {
							log(String.format("인식 최종 결과 문장 [%s][%d]", sendinfo.getStrResult(), sendinfo.getEngineDetectionFlag()));
						}
						
						int rsltCnt = sendinfo.getDataCnt();
						if (rsltCnt > 0) {
							for (int i = 0; i < rsltCnt; i++) {
								log(String.format("인식 최종 결과 #%d[%s, %s, %d, %d]", i + 1, fileName,
										sendinfo.getDataResult()[i].getStrToken(),
										sendinfo.getDataResult()[i].getStart(), sendinfo.getDataResult()[i].getEnd()));
							}
						}
					}
				}          	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }    
            
            ret = lib.SelvySTT_FINISH();
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_FINISH RETURN] : " + ret);
            } else {
            	log("[SelvySTT_FINISH ERROR] : " + ret);	
            	return;
            }
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		System.out.println("종료");
    	}
	}	
	
	/**
	  * @Method Name : doTestSystemRes
	  * @작성일 : 2022. 11. 15. 오전 10:13:56
	  * @작성자 : kkkim
	  * @변경이력 : 시스템 정보 확인 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  */
	public void doTestSystemRes(String cHost, int uPort)
	{
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();
    	try {
       		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 30);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
                        
			LVCSR_STT_VERSION versionInfo = new LVCSR_STT_VERSION();			
			ret = lib.SelvySTT_GET_VERSION(versionInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_GET_STTVER RETURN] : " + ret + " STT Ver: " + versionInfo.getStrEngineVer() + " | Client Ver: " + versionInfo.getStrClientVer());	
			} else {
				log("[SelvySTT_GET_STTVER RETURN] : " + ret);	
				return;
			}
			
			LVCSR_DATA_SVRINFO serverInfo = new LVCSR_DATA_SVRINFO();			
			ret = lib.SelvySTT_GET_SERVER(serverInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				String strHostID = String.format("%08x", serverInfo.getHostID());
				log("[SelvySTT_GET_SERVER RETURN] : " + ret + " Host ID: " + strHostID + " | Host Name: " + serverInfo.getHostNameStr());
				int nIFCardCnt = serverInfo.getIFCardCnt();
    			for(int i=0 ; i < nIFCardCnt ; i++) {
    				String pIFNameStr = serverInfo.getDataIFCard()[i].getStrIFName();
    				String pIpAddressStr = serverInfo.getDataIFCard()[i].getStrIpAddress();
    				log(String.format("Interface Card #%d[%s, %s]", i+1, pIFNameStr, pIpAddressStr));
    			}
			} else {
				log("[SelvySTT_GET_SERVER RETURN] : " + ret);	
				return;
			}

            LVCSR_DATA_RESOURCE resourceinfo = new LVCSR_DATA_RESOURCE();
            ret = lib.SelvySTT_GET_RESOURCE(resourceinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_RESOURCE RETURN] : " + ret);	
            	log(String.format("Network [%.10f] System CPU [%.3f] Process CPU [%.3f] System MEM [%.3f] Process MEM [%.3f] System MEM [%d] Used MEM [%d] Process MEM [%d] Disk [%.3f] Total MEM [%d] Used Disk [%d]", resourceinfo.getNetworkUsage(), resourceinfo.getfCPUSysUsage(), resourceinfo.getCPUProcUsage(), resourceinfo.getMEMSysUsage(), resourceinfo.getMEMProcUsage(), resourceinfo.getMEMTotPhys(), resourceinfo.getMEMSysPhys(), resourceinfo.getMEMProcPhys(), resourceinfo.getDiskUsage(), resourceinfo.getDiskTotalPhys(), resourceinfo.getDiskUsedPhys()));
        		LVCSR_GPU_LIST[] gpustat = resourceinfo.getGpuInfo();
        		for (int i = 0; i < resourceinfo.getGpuCnt(); i++)
        		{
        			log(String.format("#%d GPU Utilization[%.3f] GPU MEMUtilization [%.3f] GPU System MEM [%.3f]", i, gpustat[i].getGPUUtilization(), gpustat[i].getGPUMEMUtilization(), gpustat[i].getGPUMEMSysUsage()));
        		}
            } else {
            	log("[SelvySTT_GET_RESOURCE_ADV ERROR] : " + ret);	
            	return;
            }  
            
            LVCSR_DATA_CHANNEL channelinfo = new LVCSR_DATA_CHANNEL();
            ret = lib.SelvySTT_GET_CHANNEL(channelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_CHANNEL RETURN] : " + ret);
            	log(String.format("Total CH [%d] Used CH [%d]", channelinfo.getChannelCnt(), channelinfo.getUsedCnt()));
            	LVCSR_CHANNEL_LIST[] channelstat = channelinfo.getChannelInfo();
            	for (int i = 0; i < channelinfo.getThreadCnt(); i++)
            	{
            		log(String.format("CHANNEL_ID [%d] CHANNEL_STAT [%s]", channelstat[i].getChannelID(), channelstat[i].getChannelStat()));
            	}
            } else {
            	log("[SelvySTT_GET_CHANNEL ERROR] : " + ret);	
            	return;
            }
            
            LVCSR_DATA_DATETERM dateinfo = new LVCSR_DATA_DATETERM();
            dateinfo.setStartDate(20251031);
            dateinfo.setEndDate(20251101);
            LVCSR_DATA_STATDATE statdateinfo = new LVCSR_DATA_STATDATE();
            ret = lib.SelvySTT_GET_STAT(dateinfo, statdateinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_STAT RETURN] : " + ret);            	
            	log(String.format("StatDate Cnt [%d]", statdateinfo.getStatDateCnt()));
            	
            	LVCSR_STATDATE_LIST[] statdatelist = statdateinfo.getStatDateInfo();
            	for (int i = 0; i < statdateinfo.getStatDateCnt(); i++)
            	{       	
            		Calendar calendar = Calendar.getInstance();        			
            		calendar.setTime(new Date(statdatelist[i].getStatTime() * 1000));
            		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            		String statDate = simpleDateFormat.format(calendar.getTime());
            		
            		log(String.format("StatTime [%s][%d] TotalSuccess [%d] TotalFail[%d] MaxClientFail[%d] EtcFail[%d] TotalBytes[%d] TotalElapsedTime[%f] MaxElapsedTime[%f] MaxElapsedBytes[%d], MaxBytesSize[%d], MaxBytesElapsed[%f] ", statDate, statdatelist[i].getStatTime(), statdatelist[i].getTotalSuccess(), statdatelist[i].getTotalFail(), statdatelist[i].getMaxClientFail(), 
            				statdatelist[i].getEtcFail(), statdatelist[i].getTotalBytes(), statdatelist[i].getTotalElapsedTime(), statdatelist[i].getMaxElapsedTime(), statdatelist[i].getMaxElapsedBytes(), statdatelist[i].getMaxBytesSize(),
            				statdatelist[i].getMaxBytesElapsed()));
            	}        		
            } else {
            	log("[SelvySTT_GET_STAT ERROR] : " + ret);	
            	return;
            } 
            
			LVCSR_DATA_LICENSE licenseinfo = new LVCSR_DATA_LICENSE();
			ret = lib.SelvySTT_GET_LICENSE(licenseinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_GET_LICENSE RETURN] : " + ret);
				
				int nLicenseType = licenseinfo.getLicenseType();
    			int nChannelCnt = licenseinfo.getChannelCnt();
    			String pKeyMadeDate = licenseinfo.getKeyMadeDateStr();
    			String pKeyExpireDate = licenseinfo.getKeyExpireDateStr();
    			
    			log(String.format("라이선스[%d, %d, %s, %s]", nLicenseType, nChannelCnt, pKeyMadeDate, pKeyExpireDate));
    			
			} else {
				log("[SelvySTT_GET_LICENSE ERROR] : " + ret);
				return;
			}
            
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();
            ret = lib.SelvySTT_GET_MODEL(modelinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);            	
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
            
			LVCSR_DATA_REQTIMEOUT timeInfo = new LVCSR_DATA_REQTIMEOUT();
			ret = lib.SelvySTT_GET_REQTIME(timeInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_GET_REQTIME RETURN] : " + ret + " Sock TimeOut: " + timeInfo.getSockTimeOut() + " Read TimeOut: " + timeInfo.getReadTimeOut());	
			} else {
				log("[SelvySTT_GET_REQTIME ERROR] : " + ret);	
				return;
			}    		
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	
	/**
	  * @Method Name : doTestUserDictRes
	  * @작성일 : 2022. 11. 16.
	  * @작성자 : kkkim
	  * @변경이력 : 사용자 사전 등록 삭제
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  */
	public void doTestUserDictRes(String cHost, int uPort)
	{
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();
		try {
      		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 30);
           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
           	log("[SelvySTT_INIT RETURN] : " + ret);
           } else {
           	log("[SelvySTT_INIT ERROR] : " + ret);
           	return;
           }
           
           LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
           String pAuthentication = "BaseAuthCode";
           authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
                
           LVCSR_SET_CHARSET nCharSet = LVCSR_SET_CHARSET.CHAR_SET_UTF8;
//			ret = lib.SelvySTT_GET_CHARSET(nCharSet);
//			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//				log("[SelvySTT_GET_CHARSET RETURN] : " + ret);
//			} else {
//				log("[SelvySTT_GET_CHARSET ERROR] : " + ret);
//				return;
//			}
			
			ret = lib.SelvySTT_SET_CHARSET(nCharSet);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_CHARSET RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_CHARSET ERROR] : " + ret);
				return;
			}
						
           String wordinfoitem[][] = {
           		{"YES","예"},
           		{"NO","아니오"},
           		{"CE","센터"},
           		{"BACK"," 전단계"},
           		{"UNKNOWN"," 모름"},
           		{"GUIDE","안내"},
           		{"GURI","구리역센터"},
           		{"다시듣기","다시듣기"},
           		{"","안녕하세요"}
           };
           
           LVCSR_DATA_USERDICT userDictinfo = new LVCSR_DATA_USERDICT();
           userDictinfo.setVocabCnt(wordinfoitem.length);
           userDictinfo.setStrVocabName("VocabTest456");
   		
           LVCSR_DATA_VOCAB_INFO[] wordinfo = new LVCSR_DATA_VOCAB_INFO[(int) userDictinfo.getVocabCnt()];
   			for(int i=0;i<userDictinfo.getVocabCnt() ;i++)
	   		{
	   			LVCSR_DATA_VOCAB_INFO wordinfoItem = new LVCSR_DATA_VOCAB_INFO(); 
	   			wordinfoItem.setStrVocab(wordinfoitem[i][0]);
	   			wordinfoItem.setStrToken(wordinfoitem[i][1]);
	   			wordinfo[i] = wordinfoItem;
	   		}
   			userDictinfo.setDataVocabResult(wordinfo);
   	   		LVCSR_DATA_USERDICT_ID userDictID = new LVCSR_DATA_USERDICT_ID();   	   		
   	   		ret = lib.SelvySTT_CRE_USERDICT(userDictinfo, userDictID);
   	   		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
   	   			log("[SelvySTT_CRE_USERDICT RETURN] : " + ret + " : " + userDictID.getVocabID());
   	        } else {
   	        	log("[SelvySTT_CRE_USERDICT ERROR] : " + ret);	
   	        	return;
   	        }
   	   		
	   		while(true) {
	   			LVCSR_DATA_USERDICT_LIST userDictlist = new LVCSR_DATA_USERDICT_LIST();
	   			ret = lib.SelvySTT_GET_USERDICT_LIST(userDictlist);
	   			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	   				log("[SelvySTT_GET_USERDICT_LIST RETURN] : " + ret + " Cnt : " + userDictlist.getUserDictCnt());
	   				if (userDictlist.getUserDictCnt() > 0)
	   				{
	   					for(int i=0; i<userDictlist.getUserDictCnt(); i++) {
	   						log("SelvySTT_GET_USERDICT_LIST: " + userDictlist.getUserDictInfo()[i].getVocabID() + " /// " + userDictlist.getUserDictInfo()[i].getStrVocabName() + "=============");	   						
	   					}
	   				}
	   				break;
	   			} else if (ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	   				Thread.sleep(100);
	   			} else {
	   				log("[SelvySTT_GET_USERDICT_LIST ERROR] : " + ret);
	   				break;
	   			}	
	   		}
	   		
	   		while(true) {	
			   	userDictID = new LVCSR_DATA_USERDICT_ID();
			   	userDictID.setVocabID(0);
	   			LVCSR_DATA_USERDICTS userDicts = new LVCSR_DATA_USERDICTS();
	   			ret = lib.SelvySTT_GET_USERDICT_VIEW(userDictID, userDicts);
	   			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	   				log("[SelvySTT_GET_USERDICT_VIEW RETURN] : " + ret + " Cnt : " + userDicts.getUserDictCnt());
	   				if (userDicts.getUserDictCnt() > 0)
	   				{
	   					for(int i=0; i<userDicts.getUserDictCnt(); i++) {
	   						log("SelvySTT_GET_USERDICT_VIEW: " + userDicts.getUserDictInfo()[i].getVocabID() + " /// " + userDicts.getUserDictInfo()[i].getStrVocabName() + " /// " + userDicts.getUserDictInfo()[i].getVocabCnt() + "=============");
	   						
	   						for(int j=0; j<userDicts.getUserDictInfo()[i].getVocabInfo().length; j++) {
	   							log("Token["+j+"] : " + userDicts.getUserDictInfo()[i].getVocabInfo()[j].getStrToken());
	   							log("Vocab["+j+"] : " + userDicts.getUserDictInfo()[i].getVocabInfo()[j].getStrVocab());
	   						}
	   					}
	   				}
	   				break;
	   			} else if (ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	   				log("[SelvySTT_GET_USERDICT_VIEW] : " + ret);
	   				Thread.sleep(100);
	   			} else {
	   				log("[SelvySTT_GET_USERDICT_VIEW ERROR] : " + ret);
	   				break;
	   			}	
	   			
	   			LVCSR_DATA_USERDICTS userDictlist = new LVCSR_DATA_USERDICTS();
	   			ret = lib.SelvySTT_GET_USERDICTS(userDictlist);
	   			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	   				log("[SelvySTT_GET_USERDICT RETURN] : " + ret + " Cnt : " + userDictlist.getUserDictCnt());
	   				if (userDictlist.getUserDictCnt() > 0)
	   				{
	   					for(int i=0; i<userDictlist.getUserDictCnt(); i++) {
	   						log("=============" + userDictlist.getUserDictInfo()[i].getVocabID() + " /// " + userDictlist.getUserDictInfo()[i].getStrVocabName() + "=============");
	   						
	   						for(int j=0; j<userDictlist.getUserDictInfo()[i].getVocabInfo().length; j++) {
	   							log("Token["+j+"] : " + userDictlist.getUserDictInfo()[i].getVocabInfo()[j].getStrToken());
	   							log("Vocab["+j+"] : " + userDictlist.getUserDictInfo()[i].getVocabInfo()[j].getStrVocab());
	   						}
	   					}
	   				}
	   				break;
	   			} else if (ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	   				Thread.sleep(100);
	   			} else {
	   				log("[SelvySTT_GET_USERDICT ERROR] : " + ret);
	   				break;
	   			}	   			
	   		}

            LVCSR_DATA_USERDICTS userDictlist = new LVCSR_DATA_USERDICTS();
    		ret = lib.SelvySTT_GET_USERDICTS(userDictlist);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_GET_USERDICT RETURN] : " + ret);	
    			for(int i=0; i<userDictlist.getUserDictInfo().length; i++) {
    				log("=============" + userDictlist.getUserDictInfo()[i].getVocabID() + " /// " + userDictlist.getUserDictInfo()[i].getStrVocabName() + "=============");
    			 
    				for(int j=0; j<userDictlist.getUserDictInfo()[i].getVocabInfo().length; j++) {
    					log("Token["+j+"] : " + userDictlist.getUserDictInfo()[i].getVocabInfo()[j].getStrToken());
    					log("Vocab["+j+"] : " + userDictlist.getUserDictInfo()[i].getVocabInfo()[j].getStrVocab());
    				}
    			}
   			    			
	        } else {
	        	log("[SelvySTT_GET_USERDICT ERROR] : " + ret);	
	        	return;
	        }

    	userDictID = new LVCSR_DATA_USERDICT_ID();
    	userDictID.setVocabID(3);
   		
   		ret = lib.SelvySTT_DEL_USERDICT(userDictID);
   		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
   			log("[SelvySTT_DEL_USERDICT RETURN] : " + ret);	
        } else {
        	log("[SelvySTT_DEL_USERDICT ERROR] : " + ret);	
        	return;
        } 
        		
	   	} catch(Exception e) {
	   		System.out.println(e);
	   	} finally {
	   		try {
	   			lib.SelvySTT_EXIT();
	   		} catch(Exception e) { }    	
	   	}
	}	

	/**
	  * @Method Name : doTestUserDictGetRes
	  * @작성일 : 2022. 11. 16.
	  * @작성자 : kkkim
	  * @변경이력 : KWD 등록 삭제
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  */
	public void doTestUserDictGetRes(String cHost, int uPort)
	{
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();
    	try {
       		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 30);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
            
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
                 
            LVCSR_SET_CHARSET nCharSet = LVCSR_SET_CHARSET.CHAR_SET_EUCKR;
			ret = lib.SelvySTT_GET_CHARSET(nCharSet);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_GET_CHARSET RETURN] : " + ret);
			} else {
				log("[SelvySTT_GET_CHARSET ERROR] : " + ret);
				return;
			}
			
            nCharSet = LVCSR_SET_CHARSET.CHAR_SET_EUCKR;
			ret = lib.SelvySTT_SET_CHARSET(nCharSet);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_CHARSET RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_CHARSET ERROR] : " + ret);
				return;
			}
						
            LVCSR_DATA_USERDICTS userDictlist = new LVCSR_DATA_USERDICTS();
    		ret = lib.SelvySTT_GET_USERDICTS(userDictlist);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_GET_USERDICT RETURN] : " + ret);	
	        } else {
	        	log("[SelvySTT_GET_USERDICT ERROR] : " + ret);	
	        	return;
	        }
    		
         		
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	
	/**
	  * @Method Name : doTestKWDRes
	  * @작성일 : 2022. 11. 16.
	  * @작성자 : kkkim
	  * @변경이력 : KWD 등록 삭제
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  */
	public void doTestKwdRes(String cHost, int uPort)
	{
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();
    	try {
       		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 30);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
            
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
                 
            LVCSR_SET_CHARSET nCharSet = LVCSR_SET_CHARSET.CHAR_SET_EUCKR;
//			ret = lib.SelvySTT_GET_CHARSET(nCharSet);
//			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//				log("[SelvySTT_GET_CHARSET RETURN] : " + ret);
//			} else {
//				log("[SelvySTT_GET_CHARSET ERROR] : " + ret);
//				return;
//			}
			
			ret = lib.SelvySTT_SET_CHARSET(nCharSet);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_CHARSET RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_CHARSET ERROR] : " + ret);
				return;
			}
						
            String wordinfoitem[][] = {
            		{"YES","예"},
            		{"NO","아니오"},
            		{"CE","센터"},
            		{"BACK"," 전단계"},
            		{"UNKNOWN"," 모름"},
            		{"GUIDE","안내"},
            		{"GURI","구리역센터"},
            		{"다시듣기","다시듣기"}
            };
            
    		LVCSR_DATA_WORD wordlist = new LVCSR_DATA_WORD();
    		wordlist.setModelID(3);
    		wordlist.setKwdCnt(wordinfoitem.length);
    		wordlist.setStrKwdName("ABCName");
    		
    		LVCSR_DATA_KWD_RESULT[] wordinfo = new LVCSR_DATA_KWD_RESULT[(int) wordlist.getKwdCnt()];
    		
    		for(int i=0;i<wordlist.getKwdCnt() ;i++)
    		{
    			LVCSR_DATA_KWD_RESULT wordinfoItem = new LVCSR_DATA_KWD_RESULT(); 
    			wordinfoItem.setStrSymbol(wordinfoitem[i][0]);
    			wordinfoItem.setStrToken(wordinfoitem[i][1]);
    			wordinfo[i] = wordinfoItem;
    		}
    		wordlist.setDataKwdResult(wordinfo);   
    		LVCSR_DATA_WORD_ID wordlist_id = new LVCSR_DATA_WORD_ID();
    		
    		ret = lib.SelvySTT_CRE_WORD(wordlist, wordlist_id);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_CRE_WORD RETURN] : " + ret);	
	        } else {
	        	log("[SelvySTT_CRE_WORD ERROR] : " + ret);	
	        	return;
	        }
    		
//    		LVCSR_DATA_WORD_KWD word_kwd_info = new LVCSR_DATA_WORD_KWD();
//    		word_kwd_info.setModelID(3);
//    		word_kwd_info.setKwdID(4);
//    		
//    		ret = lib.SelvySTT_DEL_WORD(word_kwd_info);
//    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    			log("[SelvySTT_DEL_WORD RETURN] : " + ret);	
//	        } else {
//	        	log("[SelvySTT_DEL_WORD ERROR] : " + ret);	
//	        	return;
//	        } 
         		
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
		

	/**
	  * @Method Name : doTestBaseSvc
	  * @작성일 : 2022. 11. 15. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 기본 결과 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestUserDictSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
                if(modelinfo.getModelCnt() > 0) {
                	int rsltCnt = modelinfo.getModelCnt();
            		for(int i=0 ; i < rsltCnt ; i++) {
            			int nModelID = modelinfo.getModelInfo()[i].getModelID();
            			String pModelName = modelinfo.getModelInfo()[i].getStrModelName();
            			int nModelType = modelinfo.getModelInfo()[i].getModelType();        			
    					int nSamplingRate = modelinfo.getModelInfo()[i].getSamplingRate();
            			int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
            			log(String.format("MODEL 결과 #%d[%d, %s, %d, %d]", i+1, nModelID, pModelName, nModelType, nSamplingRate));
            			
            			for(int j=0 ; j < nKwdCnt ; j++) {
            				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
            				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
            				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
            			}
            			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
            			if (ContextUsed == 1) {
            				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
            			}
            		}            		
            	}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
            int[] nUserDictID = { 0, 1 };
            datainfo.setUserDictCnt(nUserDictID.length);          
            LVCSR_DATA_USERDICTID[] userDictIDinfo = new LVCSR_DATA_USERDICTID[(int) datainfo.getUserDictCnt()];
            for (int nDictCnt = 0; nDictCnt < datainfo.getUserDictCnt(); nDictCnt++) {
            	LVCSR_DATA_USERDICTID userDictinfoItem = new LVCSR_DATA_USERDICTID();
            	userDictinfoItem.setUserDictID(nUserDictID[nDictCnt]);
            	userDictIDinfo[nDictCnt] = userDictinfoItem;
    		}   
            datainfo.setUserDictID(userDictIDinfo);   
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            LVCSR_DATA_POST_TREATMENT dataPostTreatmentinfo = new LVCSR_DATA_POST_TREATMENT();
            dataPostTreatmentinfo.setSpmUsed(LVCSR_USED_SPM.SPM_USED_ON);
            dataPostTreatmentinfo.setItnUsed(LVCSR_USED_ITN.ITN_USED_ON);
            dataPostTreatmentinfo.setDidUsed(LVCSR_USED_DID.DID_USED_ON);
            dataPostTreatmentinfo.setUDictUsed(LVCSR_USED_UDICT.UDICT_USED_ON);			
            ret = lib.SelvySTT_SET_TREATMENT(dataPostTreatmentinfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_TREATMENT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_SET_TREATMENT ERROR] : " + ret);	
            	return;            	
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s][%d]", resultmidinfo.getStrResult(), resultmidinfo.getEngineDetectionFlag()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	 	
	/**
	  * @Method Name : doTestLevenshtein
	  * @작성일 : 2022. 11. 15. 오후 1:33:18
	  * @작성자 : kkkim
	  * @변경이력 : 인식률 측정 함수 소스
	  * @Method 설명 :
	  *
	  * @param ref
	  * @param hyp
	  * @param charSet
	  * @param wordKind
	  */
	public void doTestLevenshtein(String ref, String hyp, LVCSR_SET_CHARSET charSet, LVCSR_USED_WORD wordKind)
	{
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();

    	try {
    	   	if (charSet == LVCSR_SET_CHARSET.CHAR_SET_EUCKR) {	   		
    			System.out.println("Source : " + ref);	   		
    	   		// String 을 euc-kr 로 인코딩.
    			byte[] euckrRefBuffer = ref.getBytes(Charset.forName("euc-kr"));
    			byte[] euckrHypBuffer = hyp.getBytes(Charset.forName("euc-kr"));
    			System.out.println("euc-kr - length : " + euckrRefBuffer.length);
    			System.out.println("euc-kr - length : " + euckrHypBuffer.length);
    	   		String refFromEucKr = null;
    	   		String hypFromEucKr = null;
    	   		try {
    	   			refFromEucKr = new String(euckrRefBuffer, "euc-kr");
    	   			hypFromEucKr = new String(euckrHypBuffer, "euc-kr");
    	   		} catch (UnsupportedEncodingException e) {
    	   			// TODO Auto-generated catch block
    	   			e.printStackTrace();
    	   		}
    	   		System.out.println("String from euc-kr : " + refFromEucKr);
    	   		System.out.println("String from euc-kr : " + hypFromEucKr);
    	   		ref = refFromEucKr;
    	   		hyp = hypFromEucKr;
        	}
        	else
        	{    
    			System.out.println("Source : " + ref);
    	   		
    	   		// String 을 utf-8 로 인코딩.
    			byte[] utf8RefBuffer = ref.getBytes(Charset.forName("utf-8"));
    			byte[] utf8HypBuffer = hyp.getBytes(Charset.forName("utf-8"));
    			System.out.println("utf-8 - length : " + utf8RefBuffer.length);
    			System.out.println("utf-8 - length : " + utf8HypBuffer.length);
    	   		String refFromUtf8 = null;
    	   		String hypFromUtf8 = null;
    	   		try {
    	   			refFromUtf8 = new String(utf8RefBuffer, "utf-8");
    	   			hypFromUtf8 = new String(utf8HypBuffer, "utf-8");
    	   		} catch (UnsupportedEncodingException e) {
    	   			// TODO Auto-generated catch block
    	   			e.printStackTrace();
    	   		}
    	   		System.out.println("String from utf-8 : " + refFromUtf8);
    	   		System.out.println("String from utf-8 : " + hypFromUtf8);
    	   		ref = refFromUtf8;
    	   		hyp = hypFromUtf8;
        	}
    	   	
    		LVCSR_DATA_LSTM dateinfo = new LVCSR_DATA_LSTM();
          	dateinfo.setStrRef(ref);
          	dateinfo.setStrHyp(hyp);
          	dateinfo.setCharSet(charSet);
          	dateinfo.setWordUsed(wordKind);
    		
			LVCSR_LSTM_RESULT resultinfo = new LVCSR_LSTM_RESULT();			
			ret = lib.SelvySTT_GET_LSTM(dateinfo, resultinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    	    	log("[SelvySTT_GET_LSTM RETURN] : " + ret);
    	    	
    	    	log(String.format("인식률[%f] 에러률[%f] 전체[%d] 일치[%d] 오류[%d] 삽입[%d] 대치[%d] 삭제[%d]", resultinfo.getLstmSummary().getRecgRate(), resultinfo.getLstmSummary().getErrorRate(), resultinfo.getLstmSummary().getTotal(), resultinfo.getLstmSummary().getMatch(), resultinfo.getLstmSummary().getDistance(), resultinfo.getLstmSummary().getInsert(), resultinfo.getLstmSummary().getSubstitute(), resultinfo.getLstmSummary().getDelete()));
    			long nMatchCnt = resultinfo.getLstmMatch().getMatchCnt();
    			  
    			if(nMatchCnt > 0) {	  		 
    				log(String.format("개수[%d] 오류정보[%s]", nMatchCnt, resultinfo.getLstmMatch().getStrAlign()));
    				log(String.format("개수[%d] 정답[%s] 인식결과[%s]", nMatchCnt, resultinfo.getLstmMatch().getRefStr(), resultinfo.getLstmMatch().getHypStr()));
    				for(int i=0 ; i < nMatchCnt ; i++) {
    					log(String.format("%d 오류정보[%s] 정답[%s] 인식결과[%s]", i+1, resultinfo.getLstmMatch().getDataAlign()[i].getStrToken(), resultinfo.getLstmMatch().getRefResult()[i].getStrToken(), resultinfo.getLstmMatch().getHypResult()[i].getStrToken()));
    				}
    			}
    	    } else {
    	    	log("[SelvySTT_GET_LSTM ERROR] : " + ret);
    	    	return;
    	    }
    	} catch(Exception e) {    		
    		System.out.println(e);
    	} finally {    		
	
    	}
	}	

	/**
	  * @Method Name : doTestModelChangeSvc
	  * @작성일 : 2022. 11. 17.
	  * @작성자 : kkkim
	  * @변경이력 : 음성인식 연속 수행 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestModelChangeSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    	return;
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			}
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);            	
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }

            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                               
            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
            datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
            ret = lib.SelvySTT_SET_INFO(datainfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_INFO RETURN] : " + ret);
            	return;
            } else {
            	log("[SelvySTT_SET_INFO ERROR] : " + ret);	
            }
            
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                               
            resultinfo = new LVCSR_RECOG_RESULT();
            proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	

	/**
	  * @Method Name : doTestModelChange2Svc
	  * @작성일 : 2022. 11. 17.
	  * @작성자 : kkkim
	  * @변경이력 : 음성인식 연속 수행 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestModelChange2Svc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    	return;
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    }
    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
			} else { 
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;					
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
						
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);            	
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
			datainfo.setUserDictCnt(0);
            LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
            datainfo.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }  
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                               
            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
//            ret = lib.SelvySTT_CLOS();
//            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//            	log("[SelvySTT_CLOS RETURN] : " + ret);
//            } else {
//            	log("[SelvySTT_CLOS ERROR] : " + ret);	
//            	return;
//            }            
           
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	int nCnt = 0; 
            	int[] nUserDictIDCheck = null;
            	LVCSR_DATA_USERDICTID[] userDictIDCheckinfo = null;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		if (nCnt == 5) {
                        datainfo = new LVCSR_DATA_INFO();
                        datainfo.setModelID(ModelID);
                        datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
                        datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
                        datainfo.setEpdUsed(bEpd);  
                        datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
                        datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
                        datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
						datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
                        //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
                        datainfo.setKwdID(-1);
                        nUserDictIDCheck = new int[]{ 0, 1 };
                        datainfo.setUserDictCnt(nUserDictIDCheck.length);          
                        userDictIDCheckinfo = new LVCSR_DATA_USERDICTID[(int) datainfo.getUserDictCnt()];
                        for (int nDictCnt = 0; nDictCnt < datainfo.getUserDictCnt(); nDictCnt++) {
                        	LVCSR_DATA_USERDICTID userDictinfoItem = new LVCSR_DATA_USERDICTID();
                        	userDictinfoItem.setUserDictID(nUserDictIDCheck[nDictCnt]);
                        	userDictIDCheckinfo[nDictCnt] = userDictinfoItem;
                		}   
                        datainfo.setUserDictID(userDictIDCheckinfo);   
                        pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
                        pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
                        pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
                        pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
                        datainfo.setDataSpkDiar(pDataSpkDiar);
                        ret = lib.SelvySTT_SET_INFO(datainfo);
                        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
                        	log("[SelvySTT_SET_INFO RETURN] : " + ret);	
                        } else {
                        	log("[SelvySTT_SET_INFO ERROR] : " + ret);
                        	return;
                        }
            		}
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
					}
					nCnt++;
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}            		
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                               
            resultinfo = new LVCSR_RECOG_RESULT();
            proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
//    		try {
//    			lib.SelvySTT_CLOS();
//    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}			
	
	/**
	  * @Method Name : doTestTLOSvc
	  * @작성일 : 2022. 11. 15. 오후 1:14:34
	  * @작성자 : kkkim
	  * @변경이력 : 통합 통계 확인 테스트 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  */
	public void doTestTLOSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd)
	{	
		LVCSR_RESULT ret = null;
		LVCSR_ERROR_RESULT errInfo = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {    	    
    		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT RETURN] : " + ret);
            } else if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }            
		
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);	
			} else {
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;
			}
            
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			} 
			
			String tloItemSId = "01012341234";
			String tloItemDevInfo = "PHONE";
			String tloItemOsInfo = "ios_6";
			String tloItemNwInfo = "4G";
			String tloItemDevModel = "LE-E250";
			String tloItemCarrierType = "L";
			String tloItemScnName = "서비스시나리오";
			
		    long l = System.currentTimeMillis();
		    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("YYYYMMddHH24mmss");
		    String tloItemCallId = "CALL_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";		    
		    String tloItemTransactionId = "TR_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";		    
		    String tloItemStartMessage = localSimpleDateFormat.format(Long.valueOf(l));
		      
			LVCSR_DATA_LOGINFO loginfo = new LVCSR_DATA_LOGINFO();
			loginfo.setSID(tloItemSId);
			loginfo.setDevInfo(tloItemDevInfo);
			loginfo.setOsInfo(tloItemOsInfo);
			loginfo.setNwInfo(tloItemNwInfo);
			loginfo.setDevModel(tloItemDevModel);
			loginfo.setCarrierType(tloItemCarrierType);
			loginfo.setScnName(tloItemScnName);
			loginfo.setCallID(tloItemCallId);
			loginfo.setTransactionID(tloItemTransactionId);
			loginfo.setStartMessage(tloItemStartMessage);    
		      
			ret = lib.SelvySTT_SET_LOGINFO(loginfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_LOGINFO RETURN] : " + ret);	
            } else if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_LOGINFO ERROR] : " + ret);	
            	errInfo = new LVCSR_ERROR_RESULT();
            	ret = lib.SelvySTT_GET_ERROR(errInfo);
            	log(ret +" [CODE] : " + errInfo.getErrorCode() + " [MSG] : " + errInfo.getErrorMsg());
            	return;
            }
			
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
            LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
            ret = lib.SelvySTT_GET_MODEL(modelinfo);  
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_GET_MODEL RETURN] : " + ret);            	
            	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
            	boolean bSpacingUsed = false;
            	boolean bSentUsed = false;
            	
				boolean bKwdUsed = false;
				boolean bAddrUsed = false;
				boolean bPhonicsUsed = false;
				boolean bItnUsed = false;
				boolean bDidUsed = false;
				boolean bFillerUsed = false;
				
    	        boolean bOtfUsed = false;  
    	        boolean bQtmUsed = false;
            	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
        			int nModelType = modellist[i].getModelType();
        	        switch ((int)nModelType & 0x000F) 
        	        {
        	            case 0x0002:
        	    			bSpacingUsed = true;
        	    			bSentUsed = false;
        	                break;  
        	            case 0x0004:
        	            	bSpacingUsed = false;
        	    			bSentUsed = true;
        	                break;          	                
        	            default:
        	            	bSpacingUsed = false;
        	            	bSentUsed = false;
        	            	break;
        	        }
        	        
        	        switch ((int)nModelType & 0xF0000) 
        	        {
        	            case 0x10000:
        	            	bFillerUsed = true;
        	                break;      	                
        	            default:
        	            	bFillerUsed = false;
        	            	break;
        	        }
        	        
        			switch((int)nModelType & 0x00F0)
        			{
	        			case 0x0010:
	        				bKwdUsed = true;
							bAddrUsed = false;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0020:
							bKwdUsed = true;
							bAddrUsed = true;
							bPhonicsUsed = false;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0030:
							bKwdUsed = false;
							bAddrUsed = false;
							bPhonicsUsed = true;
							bItnUsed = false;
							bDidUsed = false;
	        				break;
	        			case 0x0040:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = false;
	        				break;
	        			case 0x0050:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = true;
	        				bDidUsed = true;
	        				break;	        				
	        			default:
	        				bKwdUsed = false;
	        				bAddrUsed = false;
	        				bPhonicsUsed = false;
	        				bItnUsed = false;
	        				bDidUsed = false;
	        				break;
        			}
        	        
        	        switch ((int)nModelType & 0xF000) 
        	        {
        	            case 0x1000:
        	            	bOtfUsed = true;
        	            	bQtmUsed = false;
        	                break;
        	            case 0x2000:
        	            	bOtfUsed = false;
        	            	bQtmUsed = true;
        	                break;        	                
        	            default:
        	            	bOtfUsed = false;
        	            	bQtmUsed = false;
        	            	break;
        	        }
        			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] FillerWord[%b] OTF[%b] QTM[%b] SampleRate[%d] AM[%s] LM[%s]"
        					, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bFillerUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
					int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
        			for(int j=0 ; j < nKwdCnt ; j++) {
        				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
        				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
        				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
        			}
        			int ContextUsed = modelinfo.getModelInfo()[i].getModelContextUsed();
        			if (ContextUsed == 1) {
        				log(String.format("Context 결과 LangType[%d] GpuID[%d] GpuThread[%d]", modelinfo.getModelInfo()[i].getContextInfo().getLangType().getValue(), modelinfo.getModelInfo()[i].getContextInfo().getGpuID(), modelinfo.getModelInfo()[i].getContextInfo().getGpuThread()));
        			}
        		}            	
            } else {
            	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
            	return;
            }
            			
            LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_8K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
            datainfo.setEpdUsed(bEpd);  
            datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
            datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
            datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
			datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
            //datainfo.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
            datainfo.setKwdID(-1);
            int[] nUserDictID = { 0, 1 };
            datainfo.setUserDictCnt(nUserDictID.length);          
            LVCSR_DATA_USERDICTID[] userDictIDinfo = new LVCSR_DATA_USERDICTID[(int) datainfo.getUserDictCnt()];
            for (int nDictCnt = 0; nDictCnt < datainfo.getUserDictCnt(); nDictCnt++) {
            	LVCSR_DATA_USERDICTID userDictinfoItem = new LVCSR_DATA_USERDICTID();
            	userDictinfoItem.setUserDictID(nUserDictID[nDictCnt]);
            	userDictIDinfo[nDictCnt] = userDictinfoItem;
    		}   
            datainfo.setUserDictID(userDictIDinfo);   
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            datainfo.setDataSpkDiar(pDataSpkDiar);
            ret = lib.SelvySTT_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_OPEN RETURN] : " + ret);
            } else {
            	log("[SelvySTT_OPEN ERROR] : " + ret);	
            	return;
            }  
            
            if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
            {
	            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
	            datatimeout.setStartTimeout(6);
	            datatimeout.setDurationTimeout(60);
	            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
	            	return;
	            }     
	            
	            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
	            datamargin.setEpdMargin(0.7f);
	            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
	            	return;
	            }     
            }
            
            LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
            datathreshold.setEpdThreshold(8);
            ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
            	return;
            }
            
            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
            } else {
            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
            	return;
            }            
            
            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
	                        } else {
	                        	log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
	                        } 
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {   
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
	            		}
            		} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
                               
            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
        		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());	            	
        		int rsltLen = resultinfo.getResultLen();
        		if (rsltLen > 0) {
        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
        		}
        		
        		int rsltCnt = resultinfo.getDataCnt();
        		if(rsltCnt > 0) {
        			for(int i=0 ; i < rsltCnt ; i++) {
        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
        			}
        		}
            } else {
            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
                return;
            }
            
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySTT_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}

	/**
	  * @Method Name : doTestSRSvc
	  * @작성일 : 2024. 10. 29. 오후 1:14:34
	  * @작성자 : kkkim
	  * @변경이력 : SR 인식 테스트  소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  */
	public void doTestSRSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
    		if (bSSLUsed) {
	    		ret = lib.SelvySTT_SSL(null, null);
	    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	    	    	log("[SelvySTT_SSL RETURN] : " + ret);
	    	    } else {
	    	    	log("[SelvySTT_SSL ERROR] : " + ret);
	    	    	return;
	    	    }
    		}
    	    
    		ret = lib.SelvySR_INIT(cHost, uPort, 10, 60);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
            
            LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
            String pAuthentication = "BaseAuthCode";
            authInfo.setStrAuthentication(pAuthentication);
			ret = lib.SelvySTT_SET_AUTH(authInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);	
			} else {
				log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
				return;
			}
			
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			String pTransaction = pAuthentication + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);
			ret = lib.SelvySTT_SET_TRANS(transInfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_TRANS RETURN] : " + ret);
				log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
			} else {
				log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
				return;
			}
			
			LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
			reqtimeinfo.setSockTimeOut(10);
			reqtimeinfo.setReadTimeOut(240);
			ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
			} else {
				log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
				return;
			}
			
			LVCSR_DATA_SR_MODEL modelinfo = new LVCSR_DATA_SR_MODEL();
			ret = lib.SelvySR_GET_MODEL(modelinfo);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_GET_MODEL RETURN] : " + ret);
				log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));
				LVCSR_SR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
        		for (int i = 0; i < modelinfo.getModelCnt(); i++)
        		{        	        
					log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] "
									, modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType()));
        		}            	
            } else {
            	log("[SelvySR_GET_MODEL ERROR] : " + ret);	
            	return;
            }	
		
			LVCSR_DATA_SR_INFO datainfo = new LVCSR_DATA_SR_INFO();
            datainfo.setModelID(ModelID);
            datainfo.setCodecType(LVCSR_TYPE_CODEC.CODEC_RAW_16K);
            datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
            datainfo.setRecogMode(LVCSR_MODE_RECOG.USE_EPD_WITH_SIL_APPEND);
            //LVCSR_MODE_RECOG.NO_EPD;
            //LVCSR_MODE_RECOG.USE_EPD;
            //LVCSR_MODE_RECOG.USE_EPD_WITH_SIL_APPEND;
            
            String[] strWordList = { "i'm looking for jane" };//{ "she doesn't like salad" };            
            datainfo.setWordListCnt(strWordList.length);
            LVCSR_DATA_WORDLIST[] wordListinfo = new LVCSR_DATA_WORDLIST[(int) datainfo.getWordListCnt()];
            for (int nWordListCnt = 0; nWordListCnt < datainfo.getWordListCnt(); nWordListCnt++) {
            	LVCSR_DATA_WORDLIST wordListinfoItem = new LVCSR_DATA_WORDLIST();
            	wordListinfoItem.setWordLen(strWordList[nWordListCnt].length());
            	wordListinfoItem.setStrWord(strWordList[nWordListCnt].toString());
            	wordListinfo[nWordListCnt] = wordListinfoItem;
    		}              
            datainfo.setDataWordList(wordListinfo);
            datainfo.setRecognitionUsed(LVCSR_USED_RECOGNITION.RECOGNITION_SCOPE_USED_ON);
            //LVCSR_USED_RECOGNITION.RECOGNITION_USED_OFF;
            //LVCSR_USED_RECOGNITION.RECOGNITION_SCOPE_USED_ON;
            //LVCSR_USED_RECOGNITION.RECOGNITION_PARAGRAPH_USED_ON;
            
            datainfo.setReferenceDataUsed(LVCSR_USED_REFERENCE.REFERENCE_USED_BUFF);
            //LVCSR_USED_REFERENCE.REFERENCE_USED_BUFF; 
            //LVCSR_USED_REFERENCE.REFERENCE_USED_ID;
            //LVCSR_USED_REFERENCE.REFERENCE_USED_OFF;
            LVCSR_REFERENCE_DATA referenceDatainfo = null;
   			String pReferenceIdStr = "ENG_Sentence_01_01_F";
//            String pReferenceIdStr = "ENG_Sentence_01_01_TEST_1";
            datainfo.setReferenceIdLen(pReferenceIdStr.length());
            datainfo.setStrReferenceId(pReferenceIdStr);
    		
            if (datainfo.getReferenceDataUsed() == LVCSR_USED_REFERENCE.REFERENCE_USED_BUFF)
    		{
    			referenceDatainfo = new LVCSR_REFERENCE_DATA();
    			referenceDatainfo.setModelID(0);
    			referenceDatainfo.setRecogMode(LVCSR_MODE_RECOG.USE_EPD_WITH_SIL_APPEND);
                //LVCSR_MODE_RECOG.NO_EPD;
                //LVCSR_MODE_RECOG.USE_EPD;
                //LVCSR_MODE_RECOG.USE_EPD_WITH_SIL_APPEND;
    			
    			LVCSR_DATA_SRCONFIG pDataSRConfig = new LVCSR_DATA_SRCONFIG();   			
    			pDataSRConfig.setAccThreshold(1);
    			pDataSRConfig.setRequireEpdPcm(false);
    			pDataSRConfig.setRequireEpdMfcc(false);
    			pDataSRConfig.setRequirePitch(true);
    			pDataSRConfig.setRequireEnergy(true);
    			pDataSRConfig.setCmsUpdateUsed(false);
    			pDataSRConfig.setTimeoverFrame(3000);
    			pDataSRConfig.setBegineMarginFrame(18);
    			pDataSRConfig.setEndMarginFrame(15);
    			pDataSRConfig.setEndPauseFrame(50);
    			pDataSRConfig.setSpeechLengthFrame(25);
    			pDataSRConfig.setBeamSize(500);
    			pDataSRConfig.setWendSize(500);
    			pDataSRConfig.setCandidateCount(3);
    			referenceDatainfo.setDataSRConfig(pDataSRConfig);

//    	        short[] pcmData = null;
//    	        int[] pcmDataSize = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
//    	        int bufferSize = 0;
                //short[] pBuffData = new short[60800];
    			
                List<Short> pListBuffData = new ArrayList<>();               
                short[] pBuffData = null;
    	        int[] nBuffSize = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
    	        int bufferSize = 0;
    	        String filePath = pcmPath + "ENG_Sentence_01_01_F.pcm";   
    	        
    	        ret = lib.SelvySR_GET_READPCM(filePath, pListBuffData, nBuffSize);        
    	        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    	        	bufferSize = nBuffSize[0];	        	
    		        pBuffData = Utils.toShortArray(pListBuffData);
    	            System.out.println("PCM data read successfully. Buffer size: " + nBuffSize[0]);
    	            System.out.println("data: " + pBuffData.length);
    	        } else {
    	            System.out.println("Failed to read PCM data.");
    	        }    	        
    			
    			referenceDatainfo.setReferenceDataSize(bufferSize);
    			referenceDatainfo.setReferenceDataBuff(pBuffData);    			
    		} 
            datainfo.setReferenceData(referenceDatainfo);            
            ret = lib.SelvySR_OPEN(datainfo);
            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
            	log("[SelvySR_OPEN RETURN] : " + ret);	
            } else {
            	log("[SelvySR_OPEN ERROR] : " + ret);
            	return;
            }
			
			LVCSR_DATA_SRCONFIG pDataSRConf = new LVCSR_DATA_SRCONFIG();
			ret = lib.SelvySR_GET_CONFIG(pDataSRConf);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_GET_CONFIG RETURN] : " + ret + " | " + pDataSRConf.getTimeoverFrame());
				log(String.format("AccThreshold[%d] RequireEpdPcm[%b] RequireEpdMfcc[%b] RequirePitch[%b] RequireEnergy[%b] CmsUpdateUsed[%b] TimeoverFrame[%d] BegineMarginFrame[%d] EndMarginFrame[%d] EndPauseFrame[%d] SpeechLengthFrame[%d] BeamSize[%d] WendSize[%d] CandidateCount[%d]"
						, pDataSRConf.getAccThreshold(), pDataSRConf.getRequireEpdPcm(), pDataSRConf.getRequireEpdMfcc(), pDataSRConf.getRequirePitch(), pDataSRConf.getRequireEnergy(), pDataSRConf.getCmsUpdateUsed(), pDataSRConf.getTimeoverFrame(), pDataSRConf.getBegineMarginFrame(), pDataSRConf.getEndMarginFrame(), pDataSRConf.getEndPauseFrame(), pDataSRConf.getSpeechLengthFrame(), pDataSRConf.getBeamSize(), pDataSRConf.getWendSize(), pDataSRConf.getCandidateCount()));				
			} else {
				log("[SelvySR_GET_CONFIG ERROR] : " + ret);
				return;
			}
					
			pDataSRConf = new LVCSR_DATA_SRCONFIG();
			pDataSRConf.setAccThreshold(1);
			pDataSRConf.setRequireEpdPcm(false);
			pDataSRConf.setRequireEpdMfcc(false);
			pDataSRConf.setRequirePitch(true);
			pDataSRConf.setRequireEnergy(true);
			pDataSRConf.setCmsUpdateUsed(false);
			pDataSRConf.setTimeoverFrame(3000);
			pDataSRConf.setBegineMarginFrame(18);
			pDataSRConf.setEndMarginFrame(15);
			pDataSRConf.setEndPauseFrame(50);
			pDataSRConf.setSpeechLengthFrame(25);
			pDataSRConf.setBeamSize(500);
			pDataSRConf.setWendSize(500);
			pDataSRConf.setCandidateCount(3);
			ret = lib.SelvySR_SET_CONFIG(pDataSRConf);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_CONFIG ERROR] : " + ret);
				return;
			}
			
			pDataSRConf = new LVCSR_DATA_SRCONFIG();
			ret = lib.SelvySR_GET_CONFIG(pDataSRConf);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_GET_CONFIG RETURN] : " + ret + " | " + pDataSRConf.getTimeoverFrame());
				log(String.format("AccThreshold[%d] RequireEpdPcm[%b] RequireEpdMfcc[%b] RequirePitch[%b] RequireEnergy[%b] CmsUpdateUsed[%b] TimeoverFrame[%d] BegineMarginFrame[%d] EndMarginFrame[%d] EndPauseFrame[%d] SpeechLengthFrame[%d] BeamSize[%d] WendSize[%d] CandidateCount[%d]"
						, pDataSRConf.getAccThreshold(), pDataSRConf.getRequireEpdPcm(), pDataSRConf.getRequireEpdMfcc(), pDataSRConf.getRequirePitch(), pDataSRConf.getRequireEnergy(), pDataSRConf.getCmsUpdateUsed(), pDataSRConf.getTimeoverFrame(), pDataSRConf.getBegineMarginFrame(), pDataSRConf.getEndMarginFrame(), pDataSRConf.getEndPauseFrame(), pDataSRConf.getSpeechLengthFrame(), pDataSRConf.getBeamSize(), pDataSRConf.getWendSize(), pDataSRConf.getCandidateCount()));
			} else {
				log("[SelvySR_GET_CONFIG ERROR] : " + ret);
				return;
			}			

            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;  
            try { 
            	File file = new File(pcmPath + fileName);
            	fileInputStream = new FileInputStream(file);
            	dataInputStream = new DataInputStream(fileInputStream);
            	
            	byte[] buff = new byte[3200];	//버퍼 크기는 생성되는 크기에 따라 설정 가능
	
            	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
            	int nLen = 0;
            	int bButtonComplete = 0;
            	
            	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
            		ret = lib.SelvySR_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            		if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
	            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
	            			//break;
	            		}
	            		if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput()) {
	            			log("[SelvySR_SEND_DATA RECV] : " + epdinfo.getOutput()); 
	            			//continue;
	            		}
            		} else {
		            	log("[SelvySR_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}                
            	dataInputStream.close();
            	fileInputStream.close();
            	
            	if (-1 == nLen || 0 == nLen) {
            		bButtonComplete = 1;
            		ret = lib.SelvySR_SEND_DATA(null, 0, bButtonComplete, epdinfo);
            		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {   
	            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
	            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s]", fileName));
	            			return;
	            		} else {
	            			log("[SelvySR_SEND_DATA RECV] : " + epdinfo.getOutput());
	            		}
            		} else {
		            	log("[SelvySR_SEND_DATA ERROR] : " + ret);
		            	return;
            		}
            	}  
            	
            } catch ( IOException e ) {
            	throw e;
            } finally {
            	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
            	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
            }  
            
            LVCSR_RECOG_CONTOURS_RESULT contoursResultinfo = new LVCSR_RECOG_CONTOURS_RESULT();
            LVCSR_RESULT contours_ret = lib.SelvySR_GET_CONTOURS(contoursResultinfo);
            if (contours_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
        		log("[SelvySR_GET_CONTOURS RETURN] : " + contours_ret);       		
       			log(String.format("PitchCnt[%d] EnergyCnt[%d]", contoursResultinfo.getPitchCnt(), contoursResultinfo.getEnergyCnt()));        		
        	} else {
        		log(String.format("SelvySR_GET_CONTOURS ERROR [%d]", contours_ret));
        		return;        	
        	}
		
            LVCSR_RECOG_SR_RESULT srResultinfo = new LVCSR_RECOG_SR_RESULT();
			LVCSR_RESULT sr_ret = lib.SelvySR_GET_RES(srResultinfo);
			if (sr_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_GET_RES RETURN] : " + ret);       		
				int userDataUsed = srResultinfo.getUserDataUsed();
				if (userDataUsed == 1) {
					log(String.format("User SnR[%f] User SentCnt[%d] ModelNameStr[ %s ] InputTextStr[ %s ] nStart[ %d ] nEnd[ %d ]", srResultinfo.getDataUser().getSnR(), srResultinfo.getDataUser().getDataSentCnt(), srResultinfo.getDataUser().getStrModelName(), srResultinfo.getDataUser().getStrInputText(), srResultinfo.getDataUser().getDataVAD().getStart(), srResultinfo.getDataUser().getDataVAD().getEnd()));
					if (srResultinfo.getDataUser().getDataSentCnt() > 0)
					{
						for (int i = 0; i < srResultinfo.getDataUser().getDataSentCnt(); i++)
						{
							log(String.format("음성인식 결과 : 문장 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
								, srResultinfo.getDataUser().getDataSentResult()[i].getStrToken(), srResultinfo.getDataUser().getDataSentResult()[i].getDataVAD().getStart(), srResultinfo.getDataUser().getDataSentResult()[i].getDataVAD().getEnd()));
							if (srResultinfo.getDataUser().getDataSentResult()[i].getDataWordCnt() > 0)
							{
								for (int j = 0; j < srResultinfo.getDataUser().getDataSentResult()[i].getDataWordCnt(); j++)
								{
									log(String.format("음성인식 결과 : 단어 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
										, srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[j].getStrToken(), srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[j].getDataVAD().getStart(), srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[j].getDataVAD().getEnd()));
									if (srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeCnt() > 0)
									{
										for (int x = 0; x < srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeCnt(); x++)
										{
											log(String.format("음성인식 결과 : 단어 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
												, srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getStrToken(), srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getDataVAD().getStart(), srResultinfo.getDataUser().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getDataVAD().getEnd()));
										}
									}
								}
							}
						}
					}
				}
		
				int referenceDataUsed = srResultinfo.getReferenceDataUsed();
				if(referenceDataUsed == 1) {
					log(String.format("Reference SnR[%f] Reference SentCnt[%d] ModelNameStr[ %s ] InputTextStr[ %s ] nStart[ %d ] nEnd[ %d ]", srResultinfo.getDataReference().getSnR(), srResultinfo.getDataReference().getDataSentCnt(), srResultinfo.getDataReference().getStrModelName(), srResultinfo.getDataReference().getStrInputText(), srResultinfo.getDataReference().getDataVAD().getStart(), srResultinfo.getDataReference().getDataVAD().getEnd()));
					if (srResultinfo.getDataReference().getDataSentCnt() > 0)
					{
						for (int i = 0; i < srResultinfo.getDataReference().getDataSentCnt(); i++)
						{
							log(String.format("음성인식 결과 : 문장 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
								, srResultinfo.getDataReference().getDataSentResult()[i].getStrToken(), srResultinfo.getDataReference().getDataSentResult()[i].getDataVAD().getStart(), srResultinfo.getDataReference().getDataSentResult()[i].getDataVAD().getEnd()));
							if (srResultinfo.getDataReference().getDataSentResult()[i].getDataWordCnt() > 0)
							{
								for (int j = 0; j < srResultinfo.getDataReference().getDataSentResult()[i].getDataWordCnt(); j++)
								{
									log(String.format("음성인식 결과 : 단어 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
										, srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[j].getStrToken(), srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[j].getDataVAD().getStart(), srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[j].getDataVAD().getEnd()));
									if (srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeCnt() > 0)
									{
										for (int x = 0; x < srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeCnt(); x++)
										{
											log(String.format("음성인식 결과 : 단어 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
												, srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getStrToken(), srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getDataVAD().getStart(), srResultinfo.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getDataVAD().getEnd()));
										}
									}
								}
							}
						}
					}					
				}
		
				int assessmentUsed = srResultinfo.getAssessmentUsed();
				if (assessmentUsed == 1) {				
					log(String.format("Assessment Overall[ %d ] ProsodyScore[ %d ] PronunScore[ %d ] TimingScore[ %d ] IntonationScore[ %d ] LoudnessScore[ %d ]", srResultinfo.getDataAssessment().getOverall(), srResultinfo.getDataAssessment().getProsodyScore()
							, srResultinfo.getDataAssessment().getPronunScore(), srResultinfo.getDataAssessment().getTimingScore(), srResultinfo.getDataAssessment().getIntonationScore(), srResultinfo.getDataAssessment().getLoudnessScore()));
						if (srResultinfo.getDataAssessment().getAssessmentWordCnt() > 0)
						{
							for (int i = 0; i < srResultinfo.getDataAssessment().getAssessmentWordCnt(); i++)
							{
								log(String.format("Assessment 단어 TimingScore[ %d ] IntonationScore[ %d ] LoudnessScore[ %d ] PronunScore[ %d ] SmallLoudness[ %d ] Spoken[ %d ]"
									, srResultinfo.getDataAssessment().getDataAssessmentWord()[i].getTimingScore(), srResultinfo.getDataAssessment().getDataAssessmentWord()[i].getIntonationScore(), srResultinfo.getDataAssessment().getDataAssessmentWord()[i].getLoudnessScore()
									, srResultinfo.getDataAssessment().getDataAssessmentWord()[i].getPronunScore(), srResultinfo.getDataAssessment().getDataAssessmentWord()[i].getSmallLoudness(), srResultinfo.getDataAssessment().getDataAssessmentWord()[i].getSpoken()));
							}
						}
				}
			} else {
				log("[SelvySR_GET_RES ERROR] : " + sr_ret);
				return;
			}
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySR_CLOS();
    		} catch(Exception e) { }
    		try {
    			lib.SelvySTT_EXIT();
    		} catch(Exception e) { }    	
    	}
	}
	
	/**
	  * @Method Name : doTestSRReferenceSvc
	  * @작성일 : 2024. 10. 29. 오후 1:14:34
	  * @작성자 : kkkim
	  * @변경이력 : SR 레퍼런스 등록 /삭제  테스트  소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  */	
	public void doTestSRReferenceSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
    		if (bSSLUsed) {
	    		ret = lib.SelvySTT_SSL(null, null);
	    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	    	    	log("[SelvySTT_SSL RETURN] : " + ret);
	    	    } else {
	    	    	log("[SelvySTT_SSL ERROR] : " + ret);
	    	    	return;
	    	    }
    		}
    	    
    		ret = lib.SelvySR_INIT(cHost, uPort, 10, 60);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }    
			
//			LVCSR_DATA_REFERENCE pDataReference = new LVCSR_DATA_REFERENCE();
//			//int nReferenceIdLen = "ENG_Sentence_01_01_TEST".length();
//			//pDataReference.setReferenceIdLen(nReferenceIdLen);
//			pDataReference.setStrReferenceId("ENG_Sentence_01_01_TEST_11");
//			pDataReference.setModelID(ModelID);
//			pDataReference.setRecogMode(LVCSR_MODE_RECOG.USE_EPD_WITH_SIL_APPEND);
//			LVCSR_DATA_SRCONFIG pDataSRConfig = new LVCSR_DATA_SRCONFIG();   			
//			pDataSRConfig.setAccThreshold(1);
//			pDataSRConfig.setRequireEpdPcm(false);
//			pDataSRConfig.setRequireEpdMfcc(false);
//			pDataSRConfig.setRequirePitch(true);
//			pDataSRConfig.setRequireEnergy(true);
//			pDataSRConfig.setCmsUpdateUsed(false);
//			pDataSRConfig.setTimeoverFrame(1000);
//			pDataSRConfig.setBegineMarginFrame(18);
//			pDataSRConfig.setEndMarginFrame(15);
//			pDataSRConfig.setEndPauseFrame(50);
//			pDataSRConfig.setSpeechLengthFrame(12);
//			pDataSRConfig.setBeamSize(500);
//			pDataSRConfig.setWendSize(300);
//			pDataSRConfig.setCandidateCount(3);    
//			pDataReference.setDataSRConfig(pDataSRConfig);
//            String[] strWordList = { "she doesn't like salad" };            
//            pDataReference.setWordListCnt(strWordList.length);
//            LVCSR_DATA_WORDLIST[] wordListinfo = new LVCSR_DATA_WORDLIST[(int) pDataReference.getWordListCnt()];
//            for (int nWordListCnt = 0; nWordListCnt < pDataReference.getWordListCnt(); nWordListCnt++) {
//            	LVCSR_DATA_WORDLIST wordListinfoItem = new LVCSR_DATA_WORDLIST();
//            	wordListinfoItem.setWordLen(strWordList[nWordListCnt].length());
//            	wordListinfoItem.setStrWord(strWordList[nWordListCnt].toString());
//            	wordListinfo[nWordListCnt] = wordListinfoItem;
//    		}              
//            pDataReference.setDataWordList(wordListinfo);
//                        
//            // ArrayList<Short>와 버퍼 크기 배열 초기화
//            List<Short> pListBuffData = new ArrayList<>();               
//            short[] pBuffData = null;
//	        int[] nBuffSize = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
//	        int bufferSize = 0;
//	        String filePath = pcmPath + "ENG_Sentence_01_01_F.pcm";   
//	        
//	        ret = lib.SelvySR_GET_READPCM(filePath, pListBuffData, nBuffSize);        
//	        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//	        	bufferSize = nBuffSize[0];	        	
//		        pBuffData = Utils.toShortArray(pListBuffData);
//	            System.out.println("PCM data read successfully. Buffer size: " + nBuffSize[0]);
//	            System.out.println("data: " + pBuffData.length);
//	        } else {
//	            System.out.println("Failed to read PCM data.");
//	        }
//	        
//			
//	        pDataReference.setReferenceDataSize(bufferSize);
//	        pDataReference.setReferenceDataBuff(pBuffData); 
//	        
//			LVCSR_DATA_DETAIL_REFERENCE pDataDetailReference = new LVCSR_DATA_DETAIL_REFERENCE();
//    		ret = lib.SelvySR_CRE_REFERENCE(pDataReference, pDataDetailReference);
//    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    			log("[SelvySR_CRE_REFERENCE RETURN] : " + ret + " : " + pDataDetailReference.getStrReferenceId());
//				log(String.format("StrReferenceId[%s] StrReferenceFile[%s] ReferenceIdLen[%d] ReferenceFileLen[%d]"
//						, pDataDetailReference.getStrReferenceId(), pDataDetailReference.getStrReferenceFile(), pDataDetailReference.getReferenceIdLen(), pDataDetailReference.getReferenceFileLen()));
//            } else {
//            	log("[SelvySR_CRE_REFERENCE ERROR] : " + ret);
//            	return;
//            }

    		LVCSR_SET_CHARSET abc = LVCSR_SET_CHARSET.CHAR_SET_EUCKR;
    		ret = lib.SelvySTT_GET_CHARSET(abc);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_SET_CHARSET RETURN] : " + ret + " : " + abc.getValue());
            } else {
            	log("[SelvySTT_SET_CHARSET ERROR] : " + ret);
            	return;
            }
    		
    		LVCSR_DATA_DETAIL_REFERENCE pDataDetailReference = new LVCSR_DATA_DETAIL_REFERENCE();
    		LVCSR_DATA_REFERENCE_ID pDataReferenceID = new LVCSR_DATA_REFERENCE_ID();
    		//pDataReferenceID.setStrReferenceId("test2_ref_kor4.sr");
    		pDataReferenceID.setStrReferenceId("ref_kor_1.sr");
    		pDataDetailReference = new LVCSR_DATA_DETAIL_REFERENCE();
    		ret = lib.SelvySR_GET_REFERENCE_ADV(pDataReferenceID, pDataDetailReference);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySR_GET_REFERENCE_ADV RETURN] : " + ret + " : " + pDataDetailReference.getStrReferenceId());
				log(String.format("StrReferenceId[%s] StrReferenceFile[%s] ReferenceIdLen[%d] ReferenceFileLen[%d]"
						, pDataDetailReference.getStrReferenceId(), pDataDetailReference.getStrReferenceFile(), pDataDetailReference.getReferenceIdLen(), pDataDetailReference.getReferenceFileLen()));
				log(String.format("Reference SnR[%f] Reference SentCnt[%d] ModelNameStr[ %s ] InputTextStr[ %s ] nStart[ %d ] nEnd[ %d ]", pDataDetailReference.getDataReference().getSnR(), pDataDetailReference.getDataReference().getDataSentCnt(), pDataDetailReference.getDataReference().getStrModelName(), pDataDetailReference.getDataReference().getStrInputText(), pDataDetailReference.getDataReference().getDataVAD().getStart(), pDataDetailReference.getDataReference().getDataVAD().getEnd()));
				if (pDataDetailReference.getDataReference().getDataSentCnt() > 0)
				{
					for (int i = 0; i < pDataDetailReference.getDataReference().getDataSentCnt(); i++)
					{
						log(String.format("음성인식 결과 : 문장 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
							, pDataDetailReference.getDataReference().getDataSentResult()[i].getStrToken(), pDataDetailReference.getDataReference().getDataSentResult()[i].getDataVAD().getStart(), pDataDetailReference.getDataReference().getDataSentResult()[i].getDataVAD().getEnd()));
						if (pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordCnt() > 0)
						{
							for (int j = 0; j < pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordCnt(); j++)
							{
								log(String.format("음성인식 결과 : 단어 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
									, pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[j].getStrToken(), pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[j].getDataVAD().getStart(), pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[j].getDataVAD().getEnd()));
								if (pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeCnt() > 0)
								{
									for (int x = 0; x < pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeCnt(); x++)
									{
										log(String.format("음성인식 결과 : 단어 [ %s ], 시작시간 [ %d ] 종료시간 [ %d ]"
											, pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getStrToken(), pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getDataVAD().getStart(), pDataDetailReference.getDataReference().getDataSentResult()[i].getDataWordResult()[i].getDataPhonemeResult()[x].getDataVAD().getEnd()));
									}
								}
							}
						}
					}
				}
				log(String.format("Pitch Cnt[%d] Energy Cnt[%d] EPD PCM Cnt[%d]", pDataDetailReference.getDataReference().getPitchCnt(), pDataDetailReference.getDataReference().getEnergyCnt(), pDataDetailReference.getDataReference().getEpdPcmCnt()));
				if (pDataDetailReference.getDataReference().getPitchCnt() > 0)
				{
					for (int nPitchCnt = 0; nPitchCnt < pDataDetailReference.getDataReference().getPitchCnt(); nPitchCnt++) {
						log(String.format("Pitch [%d][%f] ", nPitchCnt, pDataDetailReference.getDataReference().getDataPitch()[nPitchCnt].getSize()));		
					}
				}
				if (pDataDetailReference.getDataReference().getEnergyCnt() > 0)
				{
					for (int nEnergyCnt = 0; nEnergyCnt < pDataDetailReference.getDataReference().getEnergyCnt(); nEnergyCnt++) {
						log(String.format("Energy [%d][%f] ", nEnergyCnt, pDataDetailReference.getDataReference().getDataEnergy()[nEnergyCnt].getSize()));		
					}
				}				
				if (pDataDetailReference.getDataReference().getEpdPcmCnt() > 0)
				{
					for (int nEpdPcmCnt = 0; nEpdPcmCnt < pDataDetailReference.getDataReference().getEpdPcmCnt(); nEpdPcmCnt++) {
						log(String.format("EPD PCM [%d][%d] ", nEpdPcmCnt, pDataDetailReference.getDataReference().getDataEpdPcm()[nEpdPcmCnt].getSize()));		
					}
				}
				log(String.format("Pitch lenth()[%d] Energy lenth()[%d] EPD PCM lenth()[%d]", pDataDetailReference.getDataReference().getDataPitch().length, pDataDetailReference.getDataReference().getDataEnergy().length, pDataDetailReference.getDataReference().getDataEpdPcm().length));				
            } else {
            	log("[SelvySR_GET_REFERENCE_ADV ERROR] : " + ret);
            	return;
            }
    		
//    		pDataReferenceID = new LVCSR_DATA_REFERENCE_ID();
//    		pDataReferenceID.setStrReferenceId("ENG_Sentence_01_01_TEST_9");
//    		ret = lib.SelvySR_DEL_REFERENCE(pDataReferenceID);
//    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    			log("[SelvySR_DEL_REFERENCE RETURN] : " + ret + " : " + pDataDetailReference.getStrReferenceId());
//				log(String.format("StrReferenceId[%s] StrReferenceFile[%s] ReferenceIdLen[%d] ReferenceFileLen[%d]"
//						, pDataDetailReference.getStrReferenceId(), pDataDetailReference.getStrReferenceFile(), pDataDetailReference.getReferenceIdLen(), pDataDetailReference.getReferenceFileLen()));
//				log(String.format("레퍼런스 SnR[%f] SentCnt[%d]", pDataDetailReference.getDataReference().getSnR(), pDataDetailReference.getDataReference().getDataSentCnt()));
//            } else {
//            	log("[SelvySR_DEL_REFERENCE ERROR] : " + ret);
//            	return;
//            }   		
			
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySR_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	
	/**
	  * @Method Name : doTestSRConfigSvc
	  * @작성일 : 2024. 10. 29. 오후 1:14:34
	  * @작성자 : kkkim
	  * @변경이력 : SR 환경설정 SET / GET 테스트  소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param bSSLUsed
	  */	
	public void doTestSRConfigSvc(String cHost, int uPort, boolean bSSLUsed)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
    	try {
    		if (bSSLUsed) {
	    		ret = lib.SelvySTT_SSL(null, null);
	    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	    	    	log("[SelvySTT_SSL RETURN] : " + ret);
	    	    } else {
	    	    	log("[SelvySTT_SSL ERROR] : " + ret);
	    	    	return;
	    	    }
    		}
    	    
    		ret = lib.SelvySR_INIT(cHost, uPort, 10, 60);
    		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
    			log("[SelvySTT_INIT RETURN] : " + ret);
            } else {
            	log("[SelvySTT_INIT ERROR] : " + ret);
            	return;
            }
            				
			int nAccThreshold = 5;
			ret = lib.SelvySR_SET_AT_CONFIG(nAccThreshold);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_AT_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_AT_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nAccThresholdArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_AT_CONFIG(nAccThresholdArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nAccThreshold = nAccThresholdArray[0];
				log("[SelvySR_GET_AT_CONFIG RETURN] : " + ret + " | " + nAccThreshold);
			} else {
				log("[SelvySR_GET_AT_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean bRequireEpdPcm = true;
			ret = lib.SelvySR_SET_REPC_CONFIG(bRequireEpdPcm);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_REPC_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_REPC_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean[] bRequireEpdPcmArry = {false}; // 초기값 설정
			ret = lib.SelvySR_GET_REPC_CONFIG(bRequireEpdPcmArry);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				bRequireEpdPcm = bRequireEpdPcmArry[0];
				log("[SelvySR_GET_REPC_CONFIG RETURN] : " + ret + " | " + bRequireEpdPcm);
			} else {
				log("[SelvySR_GET_REPC_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean bRequireEpdMfcc = true;
			ret = lib.SelvySR_SET_REMF_CONFIG(bRequireEpdMfcc);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_REMF_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_REMF_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean[] bRequireEpdMfccArry = {false}; // 초기값 설정
			ret = lib.SelvySR_GET_REMF_CONFIG(bRequireEpdMfccArry);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				bRequireEpdMfcc = bRequireEpdMfccArry[0];
				log("[SelvySR_GET_REMF_CONFIG RETURN] : " + ret + " | " + bRequireEpdMfcc);
			} else {
				log("[SelvySR_GET_REMF_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean bRequirePitch = true;
			ret = lib.SelvySR_SET_RPT_CONFIG(bRequirePitch);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_RPT_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_RPT_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean[] bRequirePitchArry = {false}; // 초기값 설정
			ret = lib.SelvySR_GET_RPT_CONFIG(bRequirePitchArry);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				bRequirePitch = bRequirePitchArry[0];
				log("[SelvySR_GET_RPT_CONFIG RETURN] : " + ret + " | " + bRequirePitch);
			} else {
				log("[SelvySR_GET_RPT_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean bRequireEnergy = true;
			ret = lib.SelvySR_SET_RE_CONFIG(bRequireEnergy);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_RE_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_RE_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean[] bRequireEnergyArry = {false}; // 초기값 설정
			ret = lib.SelvySR_GET_RE_CONFIG(bRequireEnergyArry);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				bRequireEnergy = bRequireEnergyArry[0];
				log("[SelvySR_GET_RE_CONFIG RETURN] : " + ret + " | " + bRequireEnergy);
			} else {
				log("[SelvySR_GET_RE_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean bCmsUpdateUsed = true;
			ret = lib.SelvySR_SET_CUU_CONFIG(bCmsUpdateUsed);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_CUU_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_CUU_CONFIG ERROR] : " + ret);
				return;
			}
			
			boolean[] bCmsUpdateUsedArry = {false}; // 초기값 설정
			ret = lib.SelvySR_GET_CUU_CONFIG(bCmsUpdateUsedArry);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				bCmsUpdateUsed = bCmsUpdateUsedArry[0];
				log("[SelvySR_GET_CUU_CONFIG RETURN] : " + ret + " | " + bCmsUpdateUsed);
			} else {
				log("[SelvySR_GET_CUU_CONFIG ERROR] : " + ret);
				return;
			}
			
			int nTimeoverFrame = 10;
			ret = lib.SelvySR_SET_TOF_CONFIG(nTimeoverFrame);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_TOF_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_TOF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nTimeoverFrameArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_TOF_CONFIG(nTimeoverFrameArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nTimeoverFrame = nTimeoverFrameArray[0];
				log("[SelvySR_GET_TOF_CONFIG RETURN] : " + ret + " | " + nTimeoverFrame);
			} else {
				log("[SelvySR_GET_TOF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int nBegineMarginFrame = 25;
			ret = lib.SelvySR_SET_BMF_CONFIG(nBegineMarginFrame);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_BMF_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_BMF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nBegineMarginFrameArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_BMF_CONFIG(nBegineMarginFrameArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nBegineMarginFrame = nBegineMarginFrameArray[0];
				log("[SelvySR_GET_BMF_CONFIG RETURN] : " + ret + " | " + nBegineMarginFrame);
			} else {
				log("[SelvySR_GET_BMF_CONFIG ERROR] : " + ret);
				return;
			}			

			int nEndMarginFrame = 25;
			ret = lib.SelvySR_SET_EMF_CONFIG(nEndMarginFrame);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_EMF_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_EMF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nEndMarginFrameArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_EMF_CONFIG(nEndMarginFrameArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nEndMarginFrame = nBegineMarginFrameArray[0];
				log("[SelvySR_GET_EMF_CONFIG RETURN] : " + ret + " | " + nEndMarginFrame);
			} else {
				log("[SelvySR_GET_EMF_CONFIG ERROR] : " + ret);
				return;
			}					

			int nEndPauseFrame = 10;
			ret = lib.SelvySR_SET_EPF_CONFIG(nEndMarginFrame);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_EPF_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_EPF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nEndPauseFrameArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_EPF_CONFIG(nEndPauseFrameArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nEndPauseFrame = nEndPauseFrameArray[0];
				log("[SelvySR_GET_EPF_CONFIG RETURN] : " + ret + " | " + nEndPauseFrame);
			} else {
				log("[SelvySR_GET_EPF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int nSpeechLengthFrame = 25;
			ret = lib.SelvySR_SET_SLF_CONFIG(nSpeechLengthFrame);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_SLF_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_SLF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nSpeechLengthFrameArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_SLF_CONFIG(nSpeechLengthFrameArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nSpeechLengthFrame = nSpeechLengthFrameArray[0];
				log("[SelvySR_GET_EPF_CONFIG RETURN] : " + ret + " | " + nSpeechLengthFrame);
			} else {
				log("[SelvySR_GET_EPF_CONFIG ERROR] : " + ret);
				return;
			}
			
			int nBeamSize = 100;
			ret = lib.SelvySR_SET_BS_CONFIG(nBeamSize);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_BS_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_BS_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nBeamSizeArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_BS_CONFIG(nBeamSizeArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nBeamSize = nSpeechLengthFrameArray[0];
				log("[SelvySR_GET_BS_CONFIG RETURN] : " + ret + " | " + nBeamSize);
			} else {
				log("[SelvySR_GET_BS_CONFIG ERROR] : " + ret);
				return;
			}			

			int nWendSize = 100;
			ret = lib.SelvySR_SET_WS_CONFIG(nWendSize);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_WS_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_WS_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nWendSizeArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_WS_CONFIG(nWendSizeArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nWendSize = nSpeechLengthFrameArray[0];
				log("[SelvySR_GET_WS_CONFIG RETURN] : " + ret + " | " + nWendSize);
			} else {
				log("[SelvySR_GET_WS_CONFIG ERROR] : " + ret);
				return;
			}
			

			int nCandidateCount = 10;
			ret = lib.SelvySR_SET_CC_CONFIG(nCandidateCount);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_CC_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_CC_CONFIG ERROR] : " + ret);
				return;
			}
			
			int[] nCandidateCountArray = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
			ret = lib.SelvySR_GET_CC_CONFIG(nCandidateCountArray);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				nCandidateCount = nCandidateCountArray[0];
				log("[SelvySR_GET_CC_CONFIG RETURN] : " + ret + " | " + nCandidateCount);
			} else {
				log("[SelvySR_GET_CC_CONFIG ERROR] : " + ret);
				return;
			}						
			
			LVCSR_DATA_SRCONFIG pDataSRConf = new LVCSR_DATA_SRCONFIG();
			ret = lib.SelvySR_GET_CONFIG(pDataSRConf);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_GET_CONFIG RETURN] : " + ret + " | " + pDataSRConf.getTimeoverFrame());
			} else {
				log("[SelvySR_GET_CONFIG ERROR] : " + ret);
				return;
			}
					
			pDataSRConf = new LVCSR_DATA_SRCONFIG();
			pDataSRConf.setAccThreshold(1);
			pDataSRConf.setRequireEpdPcm(false);
			pDataSRConf.setRequireEpdMfcc(false);
			pDataSRConf.setRequirePitch(true);
			pDataSRConf.setRequireEnergy(true);
			pDataSRConf.setCmsUpdateUsed(false);
			pDataSRConf.setTimeoverFrame(3000);
			pDataSRConf.setBegineMarginFrame(18);
			pDataSRConf.setEndMarginFrame(15);
			pDataSRConf.setEndPauseFrame(50);
			pDataSRConf.setSpeechLengthFrame(25);
			pDataSRConf.setBeamSize(500);
			pDataSRConf.setWendSize(500);
			pDataSRConf.setCandidateCount(3);
			ret = lib.SelvySR_SET_CONFIG(pDataSRConf);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_SET_CONFIG RETURN] : " + ret);
			} else {
				log("[SelvySR_SET_CONFIG ERROR] : " + ret);
				return;
			}
			
			pDataSRConf = new LVCSR_DATA_SRCONFIG();
			ret = lib.SelvySR_GET_CONFIG(pDataSRConf);
			if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
				log("[SelvySR_GET_CONFIG RETURN] : " + ret + " | " + pDataSRConf.getTimeoverFrame());
				log(String.format("AccThreshold[%d] RequireEpdPcm[%b] RequireEpdMfcc[%b] RequirePitch[%b] RequireEnergy[%b] CmsUpdateUsed[%b] TimeoverFrame[%d] BegineMarginFrame[%d] EndMarginFrame[%d] EndPauseFrame[%d] SpeechLengthFrame[%d] BeamSize[%d] WendSize[%d] CandidateCount[%d]"
						, pDataSRConf.getAccThreshold(), pDataSRConf.getRequireEpdPcm(), pDataSRConf.getRequireEpdMfcc(), pDataSRConf.getRequirePitch(), pDataSRConf.getRequireEnergy(), pDataSRConf.getCmsUpdateUsed(), pDataSRConf.getTimeoverFrame(), pDataSRConf.getBegineMarginFrame(), pDataSRConf.getEndMarginFrame(), pDataSRConf.getEndPauseFrame(), pDataSRConf.getSpeechLengthFrame(), pDataSRConf.getBeamSize(), pDataSRConf.getWendSize(), pDataSRConf.getCandidateCount()));
			} else {
				log("[SelvySR_GET_CONFIG ERROR] : " + ret);
				return;
			}		
    	} catch(Exception e) {
    		System.out.println(e);
    	} finally {    		
    		try {
    			lib.SelvySR_EXIT();
    		} catch(Exception e) { }    	
    	}
	}	
	
	/**
	  * @Method Name : doTestFileSvc
	  * @작성일 : 2025. 04. 21.
	  * @작성자 : kkkim
	  * @변경이력 : 파일 인식 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  * @param bCodec
	  */
	public void doTestFileSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd, LVCSR_TYPE_CODEC bCodec)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
	   	try {
//    		ret = lib.SelvySTT_SSL(null, null);
//    	    if(ret == LVCSR_RESULT.LVCSR_SUCCESS) {
//    	    	log("[SelvySTT_SSL RETURN] : " + ret);
//    	    } else {
//    	    	log("[SelvySTT_SSL ERROR] : " + ret);
//    	    	return;
//    	    }
	   		
	   		LVCSR_DATA_FILE_PREPARE dataprepare = new LVCSR_DATA_FILE_PREPARE();
			dataprepare.setModelID(ModelID);
			dataprepare.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_EUCKR);
			dataprepare.setEpdUsed(bEpd);  
			dataprepare.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
	        dataprepare.setTextAnlzUsed(LVCSR_USED_TEXT_ANALYSIS.TA_USED_OFF);
	                
           List<Byte> pListBuffData = new ArrayList<>();               
           byte[] pBuffData = null;
	        int[] nBuffSize = new int[1];  // 배열을 사용하여 참조 가능하게 만듦
	        int bufferSize = 0;
	        String filePath = pcmPath + fileName;   
	        
	        ret = lib.SelvySTT_GET_READBUFF(filePath, pListBuffData, nBuffSize);        
	        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	        	bufferSize = nBuffSize[0];	        	
		        pBuffData = Utils.toByteArray(pListBuffData);
	            System.out.println("PCM data read successfully. Buffer size: " + nBuffSize[0]);
	            System.out.println("data: " + pBuffData.length);
	        } else {
	            System.out.println("Failed to read PCM data.");
	        }    	        
			
	        LVCSR_DATA_FILEBUFFER pDataFileBuffer = new LVCSR_DATA_FILEBUFFER();
	        pDataFileBuffer.setFileBuffSize(bufferSize);
	        pDataFileBuffer.setFileDataBuff(pBuffData);  
	        pDataFileBuffer.setFileNameLen(filePath.length());
	        pDataFileBuffer.setFileNameStr(fileName);
	        pDataFileBuffer.setCodecType(bCodec);
	        dataprepare.setDataFileBuffer(pDataFileBuffer);
	        
	        dataprepare.setUserDictCnt(0);
	        LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
	        dataprepare.setUserDictID(userDictIDinfo);
            LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
            pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
            pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
            pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
            dataprepare.setDataSpkDiar(pDataSpkDiar); 
	        LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
	        String pAuthentication = "BaseAuthCode";
	        authInfo.setStrAuthentication(pAuthentication);
	        dataprepare.setDataAuthentication(authInfo);
	        dataprepare.setTransFlag(LVCSR_USED_TRANS_FLAG.TRANS_FLAG_TRANS);
			LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
			long startTime = System.currentTimeMillis();
			UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
			long endTime = System.currentTimeMillis();
			
			String nameWithoutExt = Paths.get(fileName).getFileName().toString().replaceFirst("[.][^.]+$", "");
			System.out.println(nameWithoutExt);
			String pTransaction = nameWithoutExt + "_" + TransactionId.toString();
			transInfo.setStrTransactionId(pTransaction);	
			log("TransactionId : " + transInfo.getStrTransactionId());
			dataprepare.setDataTransaction(transInfo);
				
			String tloItemSId = "01012341234";
			String tloItemDevInfo = "PHONE";
			String tloItemOsInfo = "ios_6";
			String tloItemNwInfo = "4G";			
			String tloItemDevModel = "LE-E250";
			String tloItemCarrierType = "L";
			String tloItemScnName = "서비스시나리오";
				
			long l = System.currentTimeMillis();
			SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("YYYYMMddHH24mmss");
			String tloItemCallId = "CALL_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";		    
			String tloItemTransactionId = "TR_" + localSimpleDateFormat.format(Long.valueOf(l)) + "_STT0_" + "0123456789012345678901";
			log("TransactionId : " + tloItemTransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");
			String tloItemStartMessage = localSimpleDateFormat.format(Long.valueOf(l));
			      
			LVCSR_DATA_LOGINFO loginfo = new LVCSR_DATA_LOGINFO();
			loginfo.setSID(tloItemSId);
			loginfo.setDevInfo(tloItemDevInfo);
			loginfo.setOsInfo(tloItemOsInfo);
			loginfo.setNwInfo(tloItemNwInfo);
			loginfo.setDevModel(tloItemDevModel);
			loginfo.setCarrierType(tloItemCarrierType);
			loginfo.setScnName(tloItemScnName);
			loginfo.setCallID(tloItemCallId);
			loginfo.setTransactionID(tloItemTransactionId);
			loginfo.setStartMessage(tloItemStartMessage);  
			dataprepare.setDataLoginfo(loginfo);
	           
	        ret = lib.SelvySTT_AUDIO_OPEN(cHost, uPort, 10, 7200, dataprepare);
	        if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	        	log("[SelvySTT_AUDIO_OPEN RETURN] : " + ret);
	        } else {
	           	log("[SelvySTT_AUDIO_OPEN ERROR] : " + ret);	
	           	return;
	        }
	           	           
    		while (true) { 				
	            LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
	            LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
	            if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {            	
	        		log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt() + " 화자수:" + resultinfo.getSpkCnt());	            	
	        		int rsltLen = resultinfo.getResultLen();
	        		if (rsltLen > 0) {
	        			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
	        		}
	        		
	        		int rsltCnt = resultinfo.getDataCnt();
	        		if(rsltCnt > 0) {
	        			for(int i=0 ; i < rsltCnt ; i++) {
	        				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getSpkId(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
	        			}
	        		}
	        		break;
	            } else if (proc_ret == LVCSR_RESULT.LVCSR_CONTINUE) {
	            	log("[SelvySTT_GET_RES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
	            	Thread.sleep(100);
	            } else {
	            	log(String.format("SelvySTT_GET_RES [%s]", fileName));
	            	break;
	            }
    	   }
        
           ret = lib.SelvySTT_AUDIO_CLOS();
           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
           		log("[SelvySTT_AUDIO_CLOS RETURN] : " + ret);
           } else {
           		log("[SelvySTT_AUDIO_CLOS ERROR] : " + ret);	
           	return;
           }
	   	} catch(Exception e) {
	   		System.out.println(e);
	   	} finally {    		
	   		System.out.println("종료");
	   	}
	}		

	/**
	  * @Method Name : doTestConvSvc
	  * @작성일 : 2025. 04. 21. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 샘플레이트 변경 기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  * @param bCodec
	  * @param bConv
	  * @param nBuffSize
	  */
	public void doTestConvSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd, LVCSR_TYPE_CODEC bCodec, LVCSR_USED_CONVERTER bConv, int nBuffSize)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
	   	try {	   	    
	   		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	           	log("[SelvySTT_INIT RETURN] : " + ret);
	           } else {
	           	log("[SelvySTT_INIT ERROR] : " + ret);
	           	return;
	           }
			
	           LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
	           String pAuthentication = "BaseAuthCode";
	           authInfo.setStrAuthentication(pAuthentication);
				ret = lib.SelvySTT_SET_AUTH(authInfo);
				if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
				} else { 
					log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
					return;					
				}
	           
				LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
				long startTime = System.currentTimeMillis();
				UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
				long endTime = System.currentTimeMillis();
				String pTransaction = pAuthentication + "_" + TransactionId.toString();
				transInfo.setStrTransactionId(pTransaction);
				ret = lib.SelvySTT_SET_TRANS(transInfo);
				if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_SET_TRANS RETURN] : " + ret);
					log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
				} else {
					log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
					return;
				} 
							
				LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
				reqtimeinfo.setSockTimeOut(10);
				reqtimeinfo.setReadTimeOut(240);
				ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
				if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
				} else {
					log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
					return;
				}
				
	           LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
	           ret = lib.SelvySTT_GET_MODEL(modelinfo);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	           	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
	           	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
	           	boolean bSpacingUsed = false;
	           	boolean bSentUsed = false;
	           	
					boolean bKwdUsed = false;
					boolean bAddrUsed = false;
					boolean bPhonicsUsed = false;
					boolean bItnUsed = false;
					boolean bDidUsed = false;
					
	   	        boolean bOtfUsed = false;  
	   	        boolean bQtmUsed = false;
	           	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
	       		for (int i = 0; i < modelinfo.getModelCnt(); i++)
	       		{        	        
	       			int nModelType = modellist[i].getModelType();
	       	        switch ((int)nModelType & 0x000F) 
	       	        {
	       	            case 0x0002:
	       	    			bSpacingUsed = true;
	       	    			bSentUsed = false;
	       	                break;  
	       	            case 0x0004:
	       	            	bSpacingUsed = false;
	       	    			bSentUsed = true;
	       	                break;          	                
	       	            default:
	       	            	bSpacingUsed = false;
	       	            	bSentUsed = false;
	       	            	break;
	       	        }
	       	        
	       			switch((int)nModelType & 0x00F0)
	       			{
		        			case 0x0010:
		        				bKwdUsed = true;
								bAddrUsed = false;
								bPhonicsUsed = false;
								bItnUsed = false;
								bDidUsed = false;
		        				break;
		        			case 0x0020:
								bKwdUsed = true;
								bAddrUsed = true;
								bPhonicsUsed = false;
								bItnUsed = false;
								bDidUsed = false;
		        				break;
		        			case 0x0030:
								bKwdUsed = false;
								bAddrUsed = false;
								bPhonicsUsed = true;
								bItnUsed = false;
								bDidUsed = false;
		        				break;
		        			case 0x0040:
		        				bKwdUsed = false;
		        				bAddrUsed = false;
		        				bPhonicsUsed = false;
		        				bItnUsed = true;
		        				bDidUsed = false;
		        				break;
		        			case 0x0050:
		        				bKwdUsed = false;
		        				bAddrUsed = false;
		        				bPhonicsUsed = false;
		        				bItnUsed = true;
		        				bDidUsed = true;
		        				break;	        				
		        			default:
		        				bKwdUsed = false;
		        				bAddrUsed = false;
		        				bPhonicsUsed = false;
		        				bItnUsed = false;
		        				bDidUsed = false;
		        				break;
	       			}
	       	        
	       	        switch ((int)nModelType & 0x0F00) 
	       	        {
	       	            case 0x1000:
	       	            	bOtfUsed = true;
	       	            	bQtmUsed = false;
	       	                break;
	       	            case 0x2000:
	       	            	bOtfUsed = false;
	       	            	bQtmUsed = true;
	       	                break;        	                
	       	            default:
	       	            	bOtfUsed = false;
	       	            	bQtmUsed = false;
	       	            	break;
	       	        }
	       			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] OTF[%b] QTM[%b] SampleRate[%d] Scale[%f] AM[%s] LM[%s]", modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getScale(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
						int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
	       			for(int j=0 ; j < nKwdCnt ; j++) {
	       				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
	       				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
	       				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
	       			}  					
	       		}            	
	           } else {
	           	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
	           	return;
	           }
				
	           LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
	           datainfo.setModelID(ModelID);
	           datainfo.setCodecType(bCodec);
	           datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
	           datainfo.setEpdUsed(bEpd);  
	           datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
	           datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
	           datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
	           datainfo.setConvUsed(bConv);
	           datainfo.setKwdID(-1);
			   datainfo.setUserDictCnt(0);
	           LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
	           datainfo.setUserDictID(userDictIDinfo);
	           LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
	           pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
	           pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
	           pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
	           datainfo.setDataSpkDiar(pDataSpkDiar);  
	           ret = lib.SelvySTT_OPEN(datainfo);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	           	log("[SelvySTT_OPEN RETURN] : " + ret);
	           } else {
	           	log("[SelvySTT_OPEN ERROR] : " + ret);	
	           	return;
	           }
	
	           if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
	           {
		            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
		            datatimeout.setStartTimeout(6);
		            datatimeout.setDurationTimeout(60);
		            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
		            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
		            } else {
		            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
		            	return;
		            }     
		            
		            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
		            datamargin.setEpdMargin(0.7f);
		            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
		            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
		            } else {
		            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
		            	return;
		            }  
	           }
	           
	           LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
	           datathreshold.setEpdThreshold(8);
	           ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	        	   log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
	           } else {
	        	   log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
	        	   return;
	           }
	           
	            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
	            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
	            	return;
	            }
	
	           FileInputStream fileInputStream = null;
	           DataInputStream dataInputStream = null;  
	           try { 
	           	File file = new File(pcmPath + fileName);
	           	fileInputStream = new FileInputStream(file);
	           	dataInputStream = new DataInputStream(fileInputStream);
	           	
	           	byte[] buff = new byte[nBuffSize];	// 33 byte - 버퍼 크기는 생성되는 크기에 따라 설정 가능 최소단위(132)
		
	           	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
	           	int nLen = 0;
	           	int bButtonComplete = 0;
	           	
	           	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
	           		Thread.sleep(100);	           		
	           		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
						
						if (LVCSR_EPD_STAT.EPD_ERROR == epdinfo.getOutput())
						{
							log("[SelvySTT_SEND_DATA ERROR] : " + ret);
							break;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	break;
					}
	           	}                
	           	dataInputStream.close();
	           	fileInputStream.close();
	           	
	           	if (-1 == nLen || 0 == nLen) {
	           		bButtonComplete = 1;
	           		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
	           		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
		            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s] EPD Info[%d]", fileName, epdinfo.getOutput().getValue()));
		            			return;
		            		} else {
		            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
		            		}
	           		} else {
			            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
			            	return;
	           		}            		
	           	}  
	           	
	           } catch ( IOException e ) {
	           	throw e;
	           } finally {
	           	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
	           	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
	           }  
	           	
	           LVCSR_RECOG_RESULT resultinfo = new LVCSR_RECOG_RESULT();
	           LVCSR_RESULT proc_ret = lib.SelvySTT_GET_RES(resultinfo);
	           if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	       		log("[SelvySTT_GET_RES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultinfo.getResultLen() + " 개수:" + resultinfo.getDataCnt());
	       		int rsltLen = resultinfo.getResultLen();
	       		if (rsltLen > 0) {
	       			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultinfo.getStrResult(), resultinfo.getConfidScore(), resultinfo.getDataEPD().getStart(), resultinfo.getDataEPD().getEnd()));
	       		}
	       		
	       		int rsltCnt = resultinfo.getDataCnt();
	       		if(rsltCnt > 0) {
	       			for(int i=0 ; i < rsltCnt ; i++) {
	       				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultinfo.getDataResult()[i].getStrToken(), resultinfo.getDataResult()[i].getStart(), resultinfo.getDataResult()[i].getEnd()));
	       			}
	       		}
	           } else {
	           	log(String.format("SelvySTT_GET_RES [%s]", fileName));
	               return;
	           }	           
	           
	   	} catch(Exception e) {
	   		System.out.println(e);
	   	} finally {    		
	   		try {
	   			lib.SelvySTT_CLOS();
	   		} catch(Exception e) { }
	   		try {
	   			lib.SelvySTT_EXIT();
	   		} catch(Exception e) { }    	
	   	}
	}			
				
	/**
	  * @Method Name : doTestFullResSvc
	  * @작성일 : 2025. 04. 21. 오전 9:10:03
	  * @작성자 : kkkim
	  * @변경이력 : 음성인식 전체 결과  기능 함수 소스
	  * @Method 설명 :
	  *
	  * @param cHost
	  * @param uPort
	  * @param fileName
	  * @param bSSLUsed
	  * @param ModelID
	  * @param bEpd
	  * @param bCodec
	  * @param bConv
	  * @param nBuffSize
	  */
	public void doTestFullResSvc(String cHost, int uPort, String fileName, boolean bSSLUsed, int ModelID, LVCSR_USED_EPD bEpd, LVCSR_TYPE_CODEC bCodec, LVCSR_USED_CONVERTER bConv, int nBuffSize)
	{	
		LVCSR_RESULT ret = null;
		
		lib = new Lvcsr_Lib();		
		
	   	try {	   	    
	   		ret = lib.SelvySTT_INIT(cHost, uPort, 10, 60);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	           	log("[SelvySTT_INIT RETURN] : " + ret);
	           } else {
	           	log("[SelvySTT_INIT ERROR] : " + ret);
	           	return;
	           }
			
	           LVCSR_DATA_AUTHENTICATION authInfo = new LVCSR_DATA_AUTHENTICATION();
	           String pAuthentication = "BaseAuthCode";
	           authInfo.setStrAuthentication(pAuthentication);
				ret = lib.SelvySTT_SET_AUTH(authInfo);
				if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_SET_AUTH RETURN] : " + ret + " ID: " + pAuthentication);
				} else { 
					log("[SelvySTT_SET_AUTH ERROR] : " + ret);	
					return;					
				}
	           
				LVCSR_DATA_TRANSACTION transInfo = new LVCSR_DATA_TRANSACTION();
				long startTime = System.currentTimeMillis();
				UUID TransactionId = Utils.generateUUID();// UUID.randomUUID();
				long endTime = System.currentTimeMillis();
				String pTransaction = pAuthentication + "_" + TransactionId.toString();
				transInfo.setStrTransactionId(pTransaction);
				ret = lib.SelvySTT_SET_TRANS(transInfo);
				if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_SET_TRANS RETURN] : " + ret);
					log("TransactionId : " + TransactionId + " UUIDs in " + (endTime - startTime) + " milliseconds.");	
				} else {
					log("[SelvySTT_SET_TRANS ERROR] : " + ret);	
					return;
				} 
							
				LVCSR_DATA_REQTIMEOUT reqtimeinfo = new LVCSR_DATA_REQTIMEOUT();
				reqtimeinfo.setSockTimeOut(10);
				reqtimeinfo.setReadTimeOut(240);
				ret = lib.SelvySTT_SET_REQTIME(reqtimeinfo);
				if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
					log("[SelvySTT_SET_REQTIME RETURN] : " + ret);
				} else {
					log("[SelvySTT_SET_REQTIME ERROR] : " + ret);
					return;
				}
				
	           LVCSR_DATA_MODEL modelinfo = new LVCSR_DATA_MODEL();         
	           ret = lib.SelvySTT_GET_MODEL(modelinfo);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	           	log("[SelvySTT_GET_MODEL RETURN] : " + ret);
	           	log(String.format("Model Cnt [%d]", modelinfo.getModelCnt()));            	    			   	        
	           	boolean bSpacingUsed = false;
	           	boolean bSentUsed = false;
	           	
					boolean bKwdUsed = false;
					boolean bAddrUsed = false;
					boolean bPhonicsUsed = false;
					boolean bItnUsed = false;
					boolean bDidUsed = false;
					
	   	        boolean bOtfUsed = false;  
	   	        boolean bQtmUsed = false;
	           	LVCSR_MODEL_LIST[] modellist = modelinfo.getModelInfo();
	       		for (int i = 0; i < modelinfo.getModelCnt(); i++)
	       		{        	        
	       			int nModelType = modellist[i].getModelType();
	       	        switch ((int)nModelType & 0x000F) 
	       	        {
	       	            case 0x0002:
	       	    			bSpacingUsed = true;
	       	    			bSentUsed = false;
	       	                break;  
	       	            case 0x0004:
	       	            	bSpacingUsed = false;
	       	    			bSentUsed = true;
	       	                break;          	                
	       	            default:
	       	            	bSpacingUsed = false;
	       	            	bSentUsed = false;
	       	            	break;
	       	        }
	       	        
	       			switch((int)nModelType & 0x00F0)
	       			{
		        			case 0x0010:
		        				bKwdUsed = true;
								bAddrUsed = false;
								bPhonicsUsed = false;
								bItnUsed = false;
								bDidUsed = false;
		        				break;
		        			case 0x0020:
								bKwdUsed = true;
								bAddrUsed = true;
								bPhonicsUsed = false;
								bItnUsed = false;
								bDidUsed = false;
		        				break;
		        			case 0x0030:
								bKwdUsed = false;
								bAddrUsed = false;
								bPhonicsUsed = true;
								bItnUsed = false;
								bDidUsed = false;
		        				break;
		        			case 0x0040:
		        				bKwdUsed = false;
		        				bAddrUsed = false;
		        				bPhonicsUsed = false;
		        				bItnUsed = true;
		        				bDidUsed = false;
		        				break;
		        			case 0x0050:
		        				bKwdUsed = false;
		        				bAddrUsed = false;
		        				bPhonicsUsed = false;
		        				bItnUsed = true;
		        				bDidUsed = true;
		        				break;	        				
		        			default:
		        				bKwdUsed = false;
		        				bAddrUsed = false;
		        				bPhonicsUsed = false;
		        				bItnUsed = false;
		        				bDidUsed = false;
		        				break;
	       			}
	       	        
	       	        switch ((int)nModelType & 0x0F00) 
	       	        {
	       	            case 0x1000:
	       	            	bOtfUsed = true;
	       	            	bQtmUsed = false;
	       	                break;
	       	            case 0x2000:
	       	            	bOtfUsed = false;
	       	            	bQtmUsed = true;
	       	                break;        	                
	       	            default:
	       	            	bOtfUsed = false;
	       	            	bQtmUsed = false;
	       	            	break;
	       	        }
	       			log(String.format("ModelID[%d] ModelName[%s] ModelType[%d] KwdUsed[%b] AddrUsed[%b] PhonicsUsed[%b] Spacing[%b] SENT[%b] ITN[%b] DID[%b] OTF[%b] QTM[%b] SampleRate[%d] Scale[%f] AM[%s] LM[%s]", modellist[i].getModelID(), modellist[i].getStrModelName(), modellist[i].getModelType(), bKwdUsed, bAddrUsed, bPhonicsUsed, bSpacingUsed, bSentUsed, bItnUsed, bDidUsed, bOtfUsed, bQtmUsed, modellist[i].getSamplingRate(), modellist[i].getScale(), modellist[i].getStrAModelName(), modellist[i].getStrLModelName()));
						int nKwdCnt = modelinfo.getModelInfo()[i].getKwdCnt();
	       			for(int j=0 ; j < nKwdCnt ; j++) {
	       				int nKwdID = modelinfo.getModelInfo()[i].getKwdInfo()[j].getKwdID();
	       				String pKwdName = modelinfo.getModelInfo()[i].getKwdInfo()[j].getStrKwdName();
	       				log(String.format("KWD 결과 #%d:#%d[%d, %s]", i+1, j+1, nKwdID, pKwdName));
	       			}  					
	       		}            	
	           } else {
	           	log("[SelvySTT_GET_MODEL ERROR] : " + ret);	
	           	return;
	           }
				
	           LVCSR_DATA_INFO datainfo = new LVCSR_DATA_INFO();
	           datainfo.setModelID(ModelID);
	           datainfo.setCodecType(bCodec);
	           datainfo.setCharSet(LVCSR_SET_CHARSET.CHAR_SET_UTF8);
	           datainfo.setEpdUsed(bEpd);  
	           datainfo.setScoreUsed(LVCSR_USED_SCORE.SCORE_USED_ON);
	           datainfo.setAsyncUsed(LVCSR_USED_ASYNC.ASYNC_USED_ON);
	           datainfo.setAsyncMidRstUsed(LVCSR_USED_ASYNC_MID_RESULT.ASYNC_MIDRST_USED_ON);
	           datainfo.setAsyncResultUsed(LVCSR_USED_ASYNC_RESULT.ASYNC_RESULT_USED_ON);
	           datainfo.setConvUsed(bConv);
	           datainfo.setKwdID(-1);
				datainfo.setUserDictCnt(0);
	           LVCSR_DATA_USERDICTID[] userDictIDinfo = null;
	           datainfo.setUserDictID(userDictIDinfo);
	           LVCSR_DATA_SPKDIAR pDataSpkDiar = new LVCSR_DATA_SPKDIAR();
	           pDataSpkDiar.setSpkDiarUsed(LVCSR_USED_SPK_DIAR.SPK_DIAR_USED_ON);
	           pDataSpkDiar.setSpkStreamUsed(LVCSR_USED_SPK_STREAM.SPK_STREAM_USED_OFF); // 현재 미지원(배치 연동만 지원)
	           pDataSpkDiar.setSpkDiarMaxSpeakers(-1); // -1: 엔진 환경설정 적용, 0: 서버 환경설정 적용, 1<=: 세션 옵션설정 적용       
	           datainfo.setDataSpkDiar(pDataSpkDiar);
	           ret = lib.SelvySTT_OPEN(datainfo);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	           	log("[SelvySTT_OPEN RETURN] : " + ret);
	           } else {
	           	log("[SelvySTT_OPEN ERROR] : " + ret);	
	           	return;
	           }
	
	           if (bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON || bEpd == LVCSR_USED_EPD.BARGEIN_EPD_USED_ON || bEpd == LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON_WITH_MIDRES)
	           {
		            LVCSR_DATA_TIMEOUT datatimeout = new LVCSR_DATA_TIMEOUT();
		            datatimeout.setStartTimeout(6);
		            datatimeout.setDurationTimeout(60);
		            ret = lib.SelvySTT_SET_EPDTIME(datatimeout);
		            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            	log("[SelvySTT_SET_EPDTIME RETURN] : " + ret);	
		            } else {
		            	log("[SelvySTT_SET_EPDTIME ERROR] : " + ret);
		            	return;
		            }     
		            
		            LVCSR_DATA_MARGIN datamargin = new LVCSR_DATA_MARGIN();
		            datamargin.setEpdMargin(0.7f);
		            ret = lib.SelvySTT_SET_EPDMARGIN(datamargin);
		            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            	log("[SelvySTT_SET_EPDMARGIN RETURN] : " + ret);	
		            } else {
		            	log("[SelvySTT_SET_EPDMARGIN ERROR] : " + ret);
		            	return;
		            }  
	           }
	           
	           LVCSR_DATA_THRESHOLD datathreshold = new LVCSR_DATA_THRESHOLD();
	           datathreshold.setEpdThreshold(8);
	           ret = lib.SelvySTT_SET_EPDTHRES(datathreshold);
	           if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	        	   log("[SelvySTT_SET_EPDTHRES RETURN] : " + ret);	
	           } else {
	        	   log("[SelvySTT_SET_EPDTHRES ERROR] : " + ret);
	        	   return;
	           }
	           
	            LVCSR_USED_UDICT_ALGO dataUDictAlgoUsed = LVCSR_USED_UDICT_ALGO.USERDICT_SEARCH_ALGO_BM;
	            ret = lib.SelvySTT_SET_UDICT_SEARCH_ALGO(dataUDictAlgoUsed);
	            if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
	            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO RETURN] : " + ret);	
	            } else {
	            	log("[SelvySTT_SET_UDICT_SEARCH_ALGO ERROR] : " + ret);
	            	return;
	            }	           
	
	           FileInputStream fileInputStream = null;
	           DataInputStream dataInputStream = null;  
	           try { 
	           	File file = new File(pcmPath + fileName);
	           	fileInputStream = new FileInputStream(file);
	           	dataInputStream = new DataInputStream(fileInputStream);
	           	
	           	byte[] buff = new byte[nBuffSize];	// 33 byte - 버퍼 크기는 생성되는 크기에 따라 설정 가능 최소단위(132)
		
	           	LVCSR_EPD_INFO epdinfo = new LVCSR_EPD_INFO();   
	           	int nLen = 0;
	           	int bButtonComplete = 0;
	           	
	           	while ( (nLen = dataInputStream.read(buff)) != -1 ) {
	           		Thread.sleep(100);	           		
	           		ret = lib.SelvySTT_SEND_DATA(buff, nLen, bButtonComplete, epdinfo);
					if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
						if (LVCSR_EPD_STAT.DURATION_TIME_OVER == epdinfo.getOutput() || LVCSR_EPD_STAT.EPD_FOUND == epdinfo.getOutput()) {
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());
							break;
						}
						if (LVCSR_EPD_STAT.RECEIV_OK == epdinfo.getOutput() || LVCSR_EPD_STAT.RECEIV_OK_SPEECH == epdinfo.getOutput() || LVCSR_EPD_STAT.SECTION_FOUND == epdinfo.getOutput()) {						
							LVCSR_RECOG_MID_RESULT resultmidinfo = new LVCSR_RECOG_MID_RESULT();
							ret = lib.SelvySTT_GET_MIDRES(resultmidinfo);
							if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {                           
								log("[SelvySTT_GET_MIDRES RETURN] : " + ret + " 문장길이:" + resultmidinfo.getResultLen() + " 개수:" + resultmidinfo.getDataCnt()+ " 체크:" + resultmidinfo.getEngineDetectionFlag());
								if(resultmidinfo.getResultLen() > 0) {
									log(String.format("인식 중간 결과 문장 [%s]", resultmidinfo.getStrResult()));
								}
								int rsltCnt = resultmidinfo.getDataCnt();
								if(rsltCnt > 0) {
									for(int i=0 ; i < rsltCnt ; i++) {
										log(String.format("인식 중간 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultmidinfo.getDataResult()[i].getStrToken(), resultmidinfo.getDataResult()[i].getStart(), resultmidinfo.getDataResult()[i].getEnd()));
									}
								}
							} else {
								log(String.format("[SelvySTT_GET_MIDRES ERROR] : " + ret));
							}
							log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput()); 
							continue;
						}
						
						if (LVCSR_EPD_STAT.EPD_ERROR == epdinfo.getOutput())
						{
							log("[SelvySTT_SEND_DATA ERROR] : " + ret);
							break;
						}
					} else {
		            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
		            	break;
					}
	           	}                
	           	dataInputStream.close();
	           	fileInputStream.close();
	           	
	           	if (-1 == nLen || 0 == nLen) {
	           		bButtonComplete = 1;
	           		ret = lib.SelvySTT_SEND_DATA(null, 0, bButtonComplete, epdinfo);
	           		if (ret == LVCSR_RESULT.LVCSR_SUCCESS) {
		            		if (LVCSR_EPD_STAT.EPD_FOUND != epdinfo.getOutput()) {
		            			log(String.format("SelvySTT_SEND_DATA EPD Not Found [%s] EPD Info[%d]", fileName, epdinfo.getOutput().getValue()));
		            			return;
		            		} else {
		            			log("[SelvySTT_SEND_DATA RECV] : " + epdinfo.getOutput());            			
		            		}
	           		} else {
			            	log("[SelvySTT_SEND_DATA ERROR] : " + ret);
			            	return;
	           		}            		
	           	}  
	           	
	           } catch ( IOException e ) {
	           	throw e;
	           } finally {
	           	if ( dataInputStream != null ) try { dataInputStream.close(); } catch (Exception e) { }
	           	if ( fileInputStream != null ) try { fileInputStream.close(); } catch (Exception e) { }
	           }  
	           	
	           while (true) { 			           
	      		   LVCSR_DATA_RESULT_OPTIONS dataopt = new LVCSR_DATA_RESULT_OPTIONS();
	      		   dataopt.setnPostHistoryUsed(LVCSR_USED_HISTORY.HISTORY_USED_OFF);
	      		   dataopt.setSentUsed(LVCSR_USED_SENT.SENT_USED_ON);
	      		   dataopt.setSubUsed(LVCSR_USED_SUBTITLE.SUB_USED_OFF);
	      		   LVCSR_DATA_SET_ADDINFO pSetAddInfo = new LVCSR_DATA_SET_ADDINFO();
	      		   pSetAddInfo.setAddInfoUsed(LVCSR_USED_ADDINFO.ADDINFO_USED_OFF);
	      		   pSetAddInfo.setCharSec(10);
	      		   dataopt.setDataAddInfo(pSetAddInfo);    			
	      		   LVCSR_RECOG_FINAL_RESULT resultfullinfo = new LVCSR_RECOG_FINAL_RESULT();
		           LVCSR_RESULT proc_ret = lib.SelvySTT_GET_FINRES(dataopt, resultfullinfo);
		           if (proc_ret == LVCSR_RESULT.LVCSR_SUCCESS) {
			       		log("[SelvySTT_GET_FINRES RETURN] : " + ret + " 음성파일:"+ fileName +" 문장길이:" + resultfullinfo.getResultLen() + " 개수:" + resultfullinfo.getDataCnt() + " 화자 개수:" + resultfullinfo.getSpkCnt());
			       		int rsltLen = resultfullinfo.getResultLen();
			       		if (rsltLen > 0) {
			       			log(String.format("인식 결과 [%s] 스코어[%4.3f] 시작ms[%d] 끝ms[%d]", resultfullinfo.getStrResult(), resultfullinfo.getConfidScore(), resultfullinfo.getDataEPD().getStart(), resultfullinfo.getDataEPD().getEnd()));
			       		}
			       		
			       		int rsltCnt = resultfullinfo.getDataCnt();
			       		if(rsltCnt > 0) {
			       			for(int i=0 ; i < rsltCnt ; i++) {
			       				log(String.format("인식 단어 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultfullinfo.getDataResult()[i].getStrToken(), resultfullinfo.getDataResult()[i].getStart(), resultfullinfo.getDataResult()[i].getEnd()));
			       			}
			       		}
			       		
			       		log(String.format("원본 인식결과[ %s ]", resultfullinfo.getDataOrgResult().getStrResult()));
			       		log(String.format("사용자 사전[ %s ]", resultfullinfo.getDataDictResult().getStrResult()));
			       		log(String.format("ITN[ %s ]", resultfullinfo.getDataItnResult().getStrResult()));
			       		log(String.format("DID[ %s ]", resultfullinfo.getDataDidResult().getStrResult()));
			       		log(String.format("SENT[ %s ]", resultfullinfo.getDataSentResult().getStrResult()));
			       		log(String.format("인식결과[ %s ]", resultfullinfo.getStrResult()));
			       					       		
			       		int rsltOrgUsed = resultfullinfo.getOrgUsed();
			       		if (rsltOrgUsed > 0)
			       		{
			       			System.err.println("원본 결과");		       			
			       			int rsltOrgCnt = resultfullinfo.getDataOrgResult().getDataCnt();
			       			if(rsltOrgCnt > 0) {
			       				for(int i=0 ; i < rsltOrgCnt ; i++) {
			       					log(String.format("인식 원본 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultfullinfo.getDataOrgResult().getDataResult()[i].getStrToken(), resultfullinfo.getDataOrgResult().getDataResult()[i].getStart(), resultfullinfo.getDataOrgResult().getDataResult()[i].getEnd()));
			       				}
			       			}		       			
			       		}
			       		else
			       		{
			       			System.err.println("원본 결과 없음");
			       		}
			       		
			       		int rsltDictUsed = resultfullinfo.getDictUsed();
			       		if (rsltDictUsed > 0)
			       		{
			       			System.err.println("사용자 사전 결과");		       			
			       			int rsltDictCnt = resultfullinfo.getDataDictResult().getDataCnt();
			       			if(rsltDictCnt > 0) {
			       				for(int i=0 ; i < rsltDictCnt ; i++) {
			       					log(String.format("인식 사용자 사전 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultfullinfo.getDataDictResult().getDataResult()[i].getStrToken(), resultfullinfo.getDataDictResult().getDataResult()[i].getStart(), resultfullinfo.getDataDictResult().getDataResult()[i].getEnd()));
			       				}
			       			}		       			
			       		}
			       		else
			       		{
			       			System.err.println("사용자 사전 결과 없음");
			       		}
			       		
			       		int rsltITNUsed = resultfullinfo.getItnUsed();
			       		if (rsltITNUsed > 0)
			       		{
			       			System.err.println("ITN 결과");		       			
			       			int rsltITNCnt = resultfullinfo.getDataItnResult().getDataCnt();
			       			if(rsltITNCnt > 0) {
			       				for(int i=0 ; i < rsltITNCnt ; i++) {
			       					log(String.format("인식 ITN 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultfullinfo.getDataItnResult().getDataResult()[i].getStrToken(), resultfullinfo.getDataItnResult().getDataResult()[i].getStart(), resultfullinfo.getDataItnResult().getDataResult()[i].getEnd()));
			       				}
			       			}		       			
			       		}
			       		else
			       		{
			       			System.err.println("ITN 결과 없음");
			       		}
			       		
			       		int rsltDIDUsed = resultfullinfo.getDidUsed();
			       		if (rsltDIDUsed > 0)
			       		{
			       			System.err.println("DID 결과");		       			
			       			int rsltDIDCnt = resultfullinfo.getDataDidResult().getDataCnt();
			       			if(rsltDIDCnt > 0) {
			       				for(int i=0 ; i < rsltDIDCnt ; i++) {
			       					log(String.format("인식 DID 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultfullinfo.getDataDidResult().getDataResult()[i].getStrToken(), resultfullinfo.getDataDidResult().getDataResult()[i].getStart(), resultfullinfo.getDataDidResult().getDataResult()[i].getEnd()));
			       				}
			       			}		       			
			       		}
			       		else
			       		{
			       			System.err.println("DID 결과 없음");
			       		}
			       		
			       		int rsltSentUsed = resultfullinfo.getSentUsed();
			       		if (rsltSentUsed > 0)
			       		{
			       			System.err.println("문장 결과");		       			
			       			int rsltSentCnt = resultfullinfo.getDataSentResult().getDataCnt();
			       			if(rsltSentCnt > 0) {
			       				for(int i=0 ; i < rsltSentCnt ; i++) {
			       					log(String.format("인식 문장 결과 #%d[%s, %s, %d, %d]", i+1, fileName, resultfullinfo.getDataSentResult().getDataResult()[i].getStrToken(), resultfullinfo.getDataSentResult().getDataResult()[i].getStart(), resultfullinfo.getDataSentResult().getDataResult()[i].getEnd()));
			       				}
			       			}		       			
			       		}
			       		else
			       		{
			       			System.err.println("문장 결과 없음");
			       		}
			       		
			       		int rsltSubUsed = resultfullinfo.getSubUsed();
			       		if (rsltSubUsed > 0)
			       		{
			       			System.err.println("자막");		       			
			    			if (resultfullinfo.getDataSub() != null) {
				    			int smiCnt = resultfullinfo.getDataSub().getSubLen();
				    			if (smiCnt > 0) {
				    				String smiData = resultfullinfo.getDataSub().getStrSub().toString();
				    				log(String.format("자막 결과 [%s]", smiData));
				    			}
			    			}
			       		}
			       		else
			       		{
			       			System.err.println("자막 없음");	       			
			       			
			       		}
			       		
			       		int rsltAddInfoUsed = resultfullinfo.getAddInfoUsed();
			       		if (rsltAddInfoUsed > 0)
			       		{
			       			System.err.println("부가정보");
							log("음절개수:" + resultfullinfo.getDataAddInfo().getCharacterSum() + " 묵음 시간:"
									+ resultfullinfo.getDataAddInfo().getEpsTime() + " 발성 시간:" + resultfullinfo.getDataAddInfo().getNoiseTime() + " 전체 시간:"
									+ resultfullinfo.getDataAddInfo().getTotalTime() + " 발성 속도:" + resultfullinfo.getDataAddInfo().getCharacterTotPerSec());
	
							int Cnt = resultfullinfo.getDataAddInfo().getWaveCnt();
							if (Cnt > 0) {
								for (int i = 0; i < Cnt; i++) {
									log(String.format("Sample 결과 #%d[%d]", i + 1, resultfullinfo.getDataAddInfo().getDataWaveForm()[i].getWaveFormSize()));
								}
							}
							
							int TenCnt = resultfullinfo.getDataAddInfo().getCharCnt();
							if (TenCnt > 0) {
								for (int j = 0; j < TenCnt; j++) {
									log(String.format("10초당 결과 #%d[%d][%4.6f]", j + 1, resultfullinfo.getDataAddInfo().getDataCharInfo()[j].getCharacterSecSum(), resultfullinfo.getDataAddInfo().getDataCharInfo()[j].getCharacterPerSec()));
								}
							}		       			
			       		}
			       		else
			       		{
			       			System.err.println("부가정보 없음");
			       		}
			       		
			       		break;
		            } else if (proc_ret == LVCSR_RESULT.LVCSR_CONTINUE) {
		            	log("[SelvySTT_GET_FINRES RETURN] : " + proc_ret + " 음성파일:"+ fileName +" 문장길이:" + resultfullinfo.getResultLen() + " 개수:" + resultfullinfo.getDataCnt());
		            	Thread.sleep(100);
		            } else {
		            	log(String.format("SelvySTT_GET_FINRES [%s]", fileName));
		            	break;
		            }    
	           }	           
	           
	   	} catch(Exception e) {
	   		System.out.println(e);
	   	} finally {    		
	   		try {
	   			lib.SelvySTT_CLOS();
	   		} catch(Exception e) { }
	   		try {
	   			lib.SelvySTT_EXIT();
	   		} catch(Exception e) { }    	
	   	}
	}			

	
	public static void main(String args[]) { 
		
//		// 기본 음성인식 - 단문1
//    	new AsrLibSampleTest().doTestBaseSvc("etri-gpu.ngg.ai.kr", 9999, "test_08k_030.pcm", false, 0, LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON);
//		// 기본 음성인식 - 화자분리2
    	new AsrLibSampleTest().doTestBaseSDSvc("etri-gpu.ngg.ai.kr", 9999, "test_08k_030.pcm", false, 0, LVCSR_USED_EPD.CHUNK_EPD_USED_ON);
//    	// 기본 음성인식 - RT/GW 연동
//    	new AsrLibSampleTest().doTestRtGwSvc("etri-gpu.ngg.ai.kr", 9999, "test_08k_030.pcm", false, 0, LVCSR_USED_EPD.CHUNK_EPD_USED_ON);
//    	// 기본 음성인식 - 배치 연동
//    	new AsrLibSampleTest().doTestBatchSvc("127.0.0.1", 9999, "test_08k_030.pcm", false, 0, LVCSR_USED_EPD.CHUNK_EPD_USED_ON);    	
//    	// 영어 모델 연동
//    	new AsrLibSampleTest().doTestEngSvc("127.0.0.1", 9999, "Here_you_are_alaw.pcm", false, 4, LVCSR_USED_EPD.BARGEIN_EPD_USED_ON);    	
//    	// 기본 음성인식 - LogInfo
//    	new AsrLibSampleTest().doTestBaseLogInfoSvc("127.0.0.1", 9999, "seoul_8k.pcm", false, 0, LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON);
//		// 후처리 음성인식 - 단문(전화망)
//		new AsrLibSampleTest().doTestTPSvc("127.0.0.1", 9999, "seoul_8k.mlaw", false, 0, LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON, LVCSR_TYPE_CODEC.CODEC_ULAW_8K);
//    	// nBEST 음성인식3
//    	new AsrLibSampleTest().doTestNbestSvc("127.0.0.1", 9999, "seoul_8k.pcm", false, 0);  
//    	// 시스템 정보 확인 - 체크4
//    	new AsrLibSampleTest().doTestSystemRes("127.0.0.1", 9999);
//    	// 파닉스 음성인식 - STT 2.0    	
//    	new AsrLibSampleTest().doTestPhonicsSvc("127.0.0.1", 9999, "fork.pcm", false, 4, LVCSR_USED_ASYNC.ASYNC_USED_ON, LVCSR_USED_EPD.CHUNK_EPD_USED_ON_WITH_MIDRES, true);
//    	// KWD 등록/삭제  - STT 2.0
//    	new AsrLibSampleTest().doTestKwdRes("127.0.0.1", 9999);
//    	// KWD 음성인식 - STT 2.0
//    	new AsrLibSampleTest().doTestKwdSvc("127.0.0.1", 9999, "11_giricenter.pcm", false, 3, 3, LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON);
//    	// 주소 음성인식 - STT 2.0
//    	new AsrLibSampleTest().doTestAddrSvc("127.0.0.1", 9999, "10_gurisi.pcm", false, 4, LVCSR_USED_EPD.CHUNK_EPD_USED_ON);
//		// 최소화 음성인식 - 수정5
//		new AsrLibSampleTest().doTestSimpleSvc("127.0.0.1", 9999, "seoul_8k.pcm", false, 0, LVCSR_USED_EPD.EPD_USED_OFF);
//    	// 음성인식 연속 수행		
//    	new AsrLibSampleTest().doTestModelChangeSvc("127.0.0.1", 9999, "seoul_8k.pcm", false, 0, LVCSR_USED_EPD.EPD_USED_OFF);
//    	// 사용자 사전 관리		
    	//new AsrLibSampleTest().doTestUserDictRes("127.0.0.1", 9999);
//    	// 사용자 사전 옵션 적용
//    	new AsrLibSampleTest().doTestUserDictSvc("127.0.0.1", 9999, "seoul_8k.pcm", false, 0, LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON);
//    	// 인식률 측정 
//    	new AsrLibSampleTest().doTestLevenshtein("클로바 시크릿가든 오에스티 파트 쓰리 틀어줘", "클로바 임창시 시크릿 걸 좀 오에스티 파트 쓰리 틀어줘", LVCSR_SET_CHARSET.CHAR_SET_EUCKR, LVCSR_USED_WORD.WORD_USED_OFF);
//    	// SR 인식 수행6
//		new AsrLibSampleTest().doTestSRSvc("127.0.0.1", 9999, "ENG_Sentence_01_01_F.pcm", false, 0);
//		// SR 레퍼런스 등록/삭제7
//		new AsrLibSampleTest().doTestSRReferenceSvc("127.0.0.1", 9300, "ENG_Sentence_01_01_F.pcm", false, 0);
//		// SR 환경설정 관리8
//		new AsrLibSampleTest().doTestSRConfigSvc("127.0.0.1", 9999, false);
//		// 파일 음성인식9
//		new AsrLibSampleTest().doTestFileSvc("127.0.0.1", 9999, "seoul_8k.pcm", false, 0, LVCSR_USED_EPD.AUTO_STOP_EPD_USED_ON, LVCSR_TYPE_CODEC.CODEC_RAW_8K);    	
//		// 샘플레이트 변경 음성인식
//		new AsrLibSampleTest().doTestConvSvc("127.0.0.1", 9999, "seoul_16k.pcm", false, 0, LVCSR_USED_EPD.CHUNK_EPD_USED_ON, LVCSR_TYPE_CODEC.CODEC_RAW_16K, LVCSR_USED_CONVERTER.CONVERTER_USED_ON, 1600);	
//		// 음성인식 전체 결과  
//		new AsrLibSampleTest().doTestFullResSvc("127.0.0.1", 9999, "test_08k_030.pcm", false, 0, LVCSR_USED_EPD.CHUNK_EPD_USED_ON, LVCSR_TYPE_CODEC.CODEC_RAW_8K, LVCSR_USED_CONVERTER.CONVERTER_USED_OFF, 1600);
	}

    public static void log(String msg) throws SelvyException {
    	
    	System.out.println("[" + Utils.getCurrentDisplayTime() + "][Client log] " + msg);
    }
	
    public static byte[] loadFileToByteArray(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];
        
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        }
        
        return fileBytes;
    }
}
