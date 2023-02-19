package pt.theninjask.externalconsole.console.command;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.util.List;

@RequiredArgsConstructor
public class HelpCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

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
        ExternalConsole.println("Available Commands:");
        //TODO change
        List<ExternalConsoleCommand> helpSorted = console._getAllCommands().values().stream()
                .sorted(ExternalConsoleCommand.comparator).toList();
        for (ExternalConsoleCommand cmd : helpSorted) {
            // int spacing = 4 + cmd.getCommand().length();
            ExternalConsole.println(String.format("\t%s - %s", cmd.getCommand(),
                    // cmd.getDescription().replaceAll("\n", "\n\t" + " ".repeat(spacing))));
                    cmd.getDescription().replaceAll("\n", "\n\t\t")));
        }
        return 0;
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

}
