package pt.theninjask.externalconsole.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

public final class Utils {

    public static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static final PrintStream VOID_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    });
    public static final URL ICON_PATH = Utils.class
            .getResource("/pt/theninjask/externalconsole/resource/github.png");
}
