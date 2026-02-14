package pt.theninjask.externalconsole.console.command.core;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleAllCommandConsumerCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HelpCommand implements ExternalConsoleAllCommandConsumerCommand {

    private Map<String, ExternalConsoleCommand> allCommands;
    private final ExternalConsole console;

    public HelpCommand(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows all commands and their descriptions";
    }

    @Override
    public int executeCommand(String... args) {
        console.println("Available Commands:");
        //TODO change
        List<ExternalConsoleCommand> helpSorted = getAllCommands().values().stream()
                .sorted(ExternalConsoleCommand.comparator).toList();
        for (ExternalConsoleCommand cmd : helpSorted) {
            console.println(String.format("\t%s - %s", cmd.getCommand(),
                    cmd.getDescription().replaceAll("\n", "\n\t\t")));
        }
        return 0;
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public void consumeCommandMapReference(Map<String, ExternalConsoleCommand> allCommands) {
        this.allCommands = allCommands;
    }

    private Map<String, ExternalConsoleCommand> getAllCommands() {
        return Optional.ofNullable(allCommands)
                .orElseGet(Collections::emptyMap);
    }
}
