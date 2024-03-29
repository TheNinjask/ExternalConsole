package pt.theninjask.externalconsole.stream;

import java.io.OutputStream;
import java.io.PrintStream;

public class RedirectorOutputStream extends PrintStream {

    private static final OutputStream VOID = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };
    private static final RedirectorOutputStream singleton = new RedirectorOutputStream(VOID);

    private static OutputStream defaultStream = VOID;

    private RedirectorOutputStream(OutputStream out) {
        super(out);
    }

    public static RedirectorOutputStream getInstance() {
        return singleton;
    }

    public static void changeDefault(OutputStream newDefaultStream) {
        defaultStream = newDefaultStream;
    }

    public static void changeRedirectToDefault() {
        singleton.out = defaultStream;
    }

    public static void changeRedirect(OutputStream newOut) {
        singleton.out = newOut;
    }

    public static void changeRedirectToVoid() {
        singleton.out = VOID;
    }

}
