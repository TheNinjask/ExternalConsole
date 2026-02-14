package pt.theninjask.externalconsole.console.command.util;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleAllCommandConsumerCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.core.AndCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class MultipleExternalConsoleManagerCommand implements ExternalConsoleAllCommandConsumerCommand {

    private Map<String, ExternalConsoleCommand> allCommands;

    private final ExternalConsole console;

    @Override
    public String getCommand() {
        return "multiple-ec-manager";
    }

    @Override
    public String getDescription() {
        return "This program allows you to manage multiple External Consoles at the same time.";
    }

    @Override
    public int executeCommand(String... args) {
        List<ExternalConsole> otherConsoles = List.of(
                console.generateAnotherConsole(),
                console.generateAnotherConsole()
        );
        otherConsoles.stream().forEach(c -> c.setViewable(true));
        while (otherConsoles.stream().anyMatch(ExternalConsole::isDisplayable)) {
            // loop until all consoles are closed
        }
        return 0;
    }

    @Override
    public String resultMessage(int result) {
        return ExternalConsoleAllCommandConsumerCommand.super.resultMessage(result);
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return AndCommand.getParamOptions(getAllCommands(), number, currArgs);
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public boolean isDemo() {
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
