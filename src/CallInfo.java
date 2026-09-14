public class CallInfo {
    private final String sttHost;
    private final int sttPort;
    private final int codec;
    private final String srcMqttUrl;
    private final String srcMqttId;
    private final String srcMqttPwd;
    private final String sttMqttUrl;
    private final String sttMqttId;
    private final String sttMqttPwd;
    private final String calleeNo;
    private final String deviceHost;
    private final String session;
    private final String callerNo;
    private final CallerOrCallee callerOrCallee;
    private final int sid;
    private final String lang;
    private final String orgId;

    public CallInfo(String[] args, String session, String callerNo, CallerOrCallee callerOrCallee, int sid, String lang) {
        this(args[0], args[1], Integer.parseInt(args[2]), args[3], args[4], args[5],
                args[6], args[7], args[8], args[9], args[10], session, callerNo, callerOrCallee, sid, lang, args[11]);
    }

    public CallInfo(String sttHost, String sttPort, int codec, String srcMqttUrl, String srcMqttId, String srcMqttPwd,
                    String sttMqttUrl, String sttMqttId, String sttMqttPwd, String calleeNo, String deviceHost, String session,
                    String callerNo, CallerOrCallee callerOrCallee, int sid, String lang, String orgId) {
        String[] arrHost = sttHost.split("@");
        String[] arrPort = sttPort.split("@");

        if(lang.equals("ko")) {
                this.sttHost = arrHost[0];
                this.sttPort = Integer.parseInt(arrPort[0]);
        } else if(lang.equals("en")) {
            this.sttHost = arrHost[1];
            this.sttPort = Integer.parseInt(arrPort[1]);
        } else {
            this.sttHost = arrHost[2];
            this.sttPort = Integer.parseInt(arrPort[2]);
        }

        this.codec = codec;
        this.srcMqttUrl = srcMqttUrl;
        this.srcMqttId = srcMqttId;
        this.srcMqttPwd = srcMqttPwd;
        this.sttMqttUrl = sttMqttUrl;
        this.sttMqttId = sttMqttId;
        this.sttMqttPwd = sttMqttPwd;
        this.calleeNo = calleeNo;
        this.deviceHost = deviceHost;
        this.session = session;
        this.callerNo = callerNo;
        this.callerOrCallee = callerOrCallee;
        this.sid = sid;
        this.lang = lang;
        this.orgId = orgId;
    }

    public String getSttHost() { return sttHost; }
    public int getSttPort() { return sttPort; }
    public int getCodec() { return codec; }
    public String getSrcMqttUrl() { return srcMqttUrl; }
    public String getSrcMqttId() { return srcMqttId; }
    public String getSrcMqttPwd() { return srcMqttPwd; }
    public String getSttMqttUrl() { return sttMqttUrl; }
    public String getSttMqttId() { return sttMqttId; }
    public String getSttMqttPwd() { return sttMqttPwd; }
    public String getCalleeNo() { return calleeNo; }
    public String getDeviceHost() { return deviceHost; }
    public String getSession() { return session; }
    public String getCallerNo() { return callerNo; }
    public CallerOrCallee getCallerOrCallee() { return callerOrCallee; }
    public int getSid() { return sid; }
    public String getLang() { return lang; }
    public String getOrgId() {return orgId; }

    enum CallerOrCallee {
        Caller(0), Callee(1);
        private final int value;
        CallerOrCallee(int value) { this.value = value; }
        public int getValue() { return this.value; }
        public static CallerOrCallee getCallerOrCallerById(int value) {
            CallerOrCallee event = null;
            switch (value) {
                case 2:
                    event = Caller;
                    break;
                case 1:
                    event = Callee;
                    break;
            }
            return event;
        }
    }
}
