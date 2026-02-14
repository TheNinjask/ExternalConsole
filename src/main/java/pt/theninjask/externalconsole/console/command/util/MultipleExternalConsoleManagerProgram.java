package pt.theninjask.externalconsole.console.command.util;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.core.AndCommand;

public class MultipleExternalConsoleManagerProgram implements ExternalConsoleCommand {

    private final ExternalConsole mainConsole;

    public MultipleExternalConsoleManagerProgram(ExternalConsole console) {
        mainConsole = console;
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
        return ExternalConsoleCommand.super.resultMessage(result);
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return AndCommand.getParamOptions(mainConsole, number, currArgs);
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
}
