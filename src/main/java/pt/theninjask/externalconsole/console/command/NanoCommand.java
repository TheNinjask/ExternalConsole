package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class NanoCommand implements ExternalConsoleCommand {

    @Override
    public String getCommand() {
        return "nano";
    }

    @Override
    public String getDescription() {
        return "Text Editor";
    }

    @Override
    public int executeCommand(String... args) {
        for (String string : args) {
            System.out.println(string);
        }
        return 0;
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
