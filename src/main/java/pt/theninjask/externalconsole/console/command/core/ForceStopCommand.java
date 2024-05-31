package pt.theninjask.externalconsole.console.command.core;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

@RequiredArgsConstructor
public class ForceStopCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    @Override
    public String getCommand() {
        return "forceStop";
    }

    @Override
    public String getDescription() {
        return "It terminates the running JVM";
    }

    @Override
    public int executeCommand(String... args) {
        console.dispose();
        System.exit(0);
        return 0;
    }

}
