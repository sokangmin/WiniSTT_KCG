public interface SttAdapter {
    void init(CallInfo info, String transactionId) throws SttException;
    RecogData send_data(byte[] bArr);
    RecogData send_final_data(byte[] bArr);
    void close();

    class RecogData{
        String text;
        Epd epd;
        public RecogData(String text, Epd epd) {
            this.text = text;
            this.epd = epd;
        }

        public void setEpd(Epd epd) {
            this.epd = epd;
        }
    }
    enum Epd{
        PARTIAL(0),
        EPD_FOUND(1);

        private final int value;

        Epd(int value) { this.value = value; }
        public int getValue() { return value; }
    }
}
