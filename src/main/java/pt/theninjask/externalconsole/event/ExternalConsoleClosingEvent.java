package pt.theninjask.externalconsole.event;

public class ExternalConsoleClosingEvent extends BasicEvent {

    private final boolean isClosable;

    public ExternalConsoleClosingEvent(boolean isClosable) {
        super(ExternalConsoleClosingEvent.class.getSimpleName(), false);
        this.isClosable = isClosable;
    }

    public boolean isClosable() {
        return isClosable;
    }


}
