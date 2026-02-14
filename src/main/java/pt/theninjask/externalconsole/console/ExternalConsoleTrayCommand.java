package pt.theninjask.externalconsole.console;

import java.awt.*;
import java.util.Comparator;

public interface ExternalConsoleTrayCommand extends ExternalConsoleCommand {

    /**
     * Consumes self
     * @param item
     */
    void consumeSelfMenuItem(MenuItem item);
}
