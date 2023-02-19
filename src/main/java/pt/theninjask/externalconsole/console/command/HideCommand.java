package pt.theninjask.externalconsole.console.command;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import javax.swing.*;

@RequiredArgsConstructor
public class HideCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    @Override
    public String getCommand() {
        return "hide";
    }

    @Override
    public String getDescription() {
        return "Hides External Console";
    }

    @Override
    public int executeCommand(String... args) {
        console.setExtendedState(JFrame.ICONIFIED);
        return 0;
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

}
