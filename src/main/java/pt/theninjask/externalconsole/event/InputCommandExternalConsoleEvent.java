package pt.theninjask.externalconsole.event;

import lombok.Getter;
import pt.theninjask.externalconsole.console.ExternalConsole;

public class InputCommandExternalConsoleEvent extends BasicEvent {

    private final String[] args;

    @Getter
    private final boolean disableLoadingBar;

    public InputCommandExternalConsoleEvent(ExternalConsole console, String[] args) {
        this(console, args, false);
    }

    public InputCommandExternalConsoleEvent(ExternalConsole console, String[] args, boolean disableLoadingBar) {
        super(console, InputCommandExternalConsoleEvent.class.getSimpleName());
        this.args = args;
        this.disableLoadingBar = disableLoadingBar;
    }

    public String[] getArgs() {
        return args;
    }

}
