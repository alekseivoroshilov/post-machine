package exceptions;

public class WrongCommandTextFormat extends IllegalArgumentException {
    private final static String msg = " Couldn't understand the format of the command ";
    public WrongCommandTextFormat() {
        super(msg);
    }
}
