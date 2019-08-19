package co.jp.snjp.x264demo.encode.exception;

public class EncoderCreateException extends Exception {
    public EncoderCreateException() {
    }

    public EncoderCreateException(String detailMessage) {
        super(detailMessage);
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
