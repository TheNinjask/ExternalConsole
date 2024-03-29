package pt.theninjask.externalconsole.stream;

import java.io.OutputStream;
import java.io.PrintStream;

public class RedirectorErrorOutputStream extends PrintStream {

    private static final OutputStream VOID = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };
    private static final RedirectorErrorOutputStream singleton = new RedirectorErrorOutputStream(VOID);

    private static OutputStream defaultStream = VOID;

    private RedirectorErrorOutputStream(OutputStream out) {
        super(out);
    }

    public static RedirectorErrorOutputStream getInstance() {
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
