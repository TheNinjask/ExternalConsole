package pt.theninjask.externalconsole.event;

import pt.theninjask.externalconsole.console.ExternalConsole;

public class InputCommandExternalConsoleEvent extends BasicEvent {

    private final String[] args;

    public InputCommandExternalConsoleEvent(ExternalConsole console, String[] args) {
        super(console, InputCommandExternalConsoleEvent.class.getSimpleName());
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

}
