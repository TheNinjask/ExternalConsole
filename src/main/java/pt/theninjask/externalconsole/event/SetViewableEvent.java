package pt.theninjask.externalconsole.event;

public class SetViewableEvent extends BasicEvent {

    private final boolean value;

    public SetViewableEvent(boolean toViewable) {
        super(SetViewableEvent.class.getSimpleName(), true);
        this.value = toViewable;
    }

    public boolean toViewable() {
        return value;
    }

}
