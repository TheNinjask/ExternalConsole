package pt.theninjask.externalconsole.event;

public class InputCommandExternalConsoleEvent extends BasicEvent {

    private final String[] args;

    public InputCommandExternalConsoleEvent(String[] args) {
        super(InputCommandExternalConsoleEvent.class.getSimpleName());
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

}
