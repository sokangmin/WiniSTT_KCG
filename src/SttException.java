public class SttException extends Exception {
    public SttException(String errMsg) {
        super(errMsg);
    }

    public SttException(Throwable e) {
        super(e);
    }
}
