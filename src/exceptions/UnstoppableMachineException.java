package exceptions;

public class UnstoppableMachineException extends IllegalArgumentException {
    private final static String msg = " Stop command must be added ";
    public UnstoppableMachineException() {
        super(msg);
    }
}
