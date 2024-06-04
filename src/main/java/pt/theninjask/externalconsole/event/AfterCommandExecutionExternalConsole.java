package pt.theninjask.externalconsole.event;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class AfterCommandExecutionExternalConsole extends BasicEvent {

    private final ExternalConsoleCommand cmd;

    private final String[] args;

    private final int result;

    public AfterCommandExecutionExternalConsole(ExternalConsoleCommand cmd, String[] args, int result) {
        super(AfterCommandExecutionExternalConsole.class.getSimpleName(), false);
        this.cmd = cmd;
        this.args = args;
        this.result = result;
    }

    public ExternalConsoleCommand getCmd() {
        return this.cmd;
    }

    public String[] getArgs() {
        return this.args;
    }

    public int getResult() {
        return this.result;
    }
}
