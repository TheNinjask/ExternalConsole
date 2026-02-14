package pt.theninjask.externalconsole.event;

import pt.theninjask.externalconsole.console.ExternalConsole;

public class SetClosableEvent extends BasicEvent {

    private final boolean value;

    public SetClosableEvent(ExternalConsole console, boolean toClosable) {
        super(console, SetClosableEvent.class.getSimpleName(), true);
        this.value = toClosable;
    }

    public boolean toClosable() {
        return value;
    }

}
