package pt.theninjask.externalconsole.event;

import pt.theninjask.externalconsole.console.ExternalConsole;

public class SetViewableEvent extends BasicEvent {

    private final boolean value;

    public SetViewableEvent(ExternalConsole console, boolean toViewable) {
        super(console, SetViewableEvent.class.getSimpleName(), true);
        this.value = toViewable;
    }

    public boolean toViewable() {
        return value;
    }

}
