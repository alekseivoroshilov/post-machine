package exceptions;

public class EmptyTapeException extends IllegalArgumentException {
    private final static String msg = "< Input tape is empty or wasn't read correctly. >";
    public EmptyTapeException() {
        super(msg);
    }
}
