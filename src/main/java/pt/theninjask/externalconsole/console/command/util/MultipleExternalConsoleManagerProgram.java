package pt.theninjask.externalconsole.console.command.util;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleAllCommandConsumerCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.core.AndCommand;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class MultipleExternalConsoleManagerProgram implements ExternalConsoleAllCommandConsumerCommand {

    private Map<String, ExternalConsoleCommand> allCommands;

    private final ExternalConsole mainConsole;

    public MultipleExternalConsoleManagerProgram() {
        mainConsole = ExternalConsole.getSingleton();
    }

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
    public boolean isProgram() {
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
