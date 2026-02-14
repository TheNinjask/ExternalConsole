package pt.theninjask.externalconsole.event;

import pt.theninjask.externalconsole.console.ExternalConsole;

public class ExternalConsoleClosingEvent extends BasicEvent {

    private final boolean isClosable;

    public ExternalConsoleClosingEvent(ExternalConsole console, boolean isClosable) {
        super(console, ExternalConsoleClosingEvent.class.getSimpleName(), false);
        this.isClosable = isClosable;
    }

    public boolean isClosable() {
        return isClosable;
    }


}
