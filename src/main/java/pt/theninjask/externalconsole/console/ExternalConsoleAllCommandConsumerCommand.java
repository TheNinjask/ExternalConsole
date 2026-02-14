package pt.theninjask.externalconsole.console;

import java.util.Map;

public interface ExternalConsoleAllCommandConsumerCommand extends ExternalConsoleCommand {

    /**
     * Consumes all commands in the console
     *
     * @param allCommands
     */
    void consumeCommandMapReference(Map<String, ExternalConsoleCommand> allCommands);
}
