package pt.theninjask.externalconsole.event;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BasicEvent implements Event {

    private final UUID id;

    private final String name;

    private final AtomicBoolean cancelled;

    private final boolean isCancellable;

    public BasicEvent() {
        this(BasicEvent.class.getSimpleName(), true);
    }

    public BasicEvent(boolean isCancellable) {
        this(BasicEvent.class.getSimpleName(), isCancellable);
    }

    public BasicEvent(String name) {
        this(name, true);
    }

    public BasicEvent(String name, boolean isCancellable) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.cancelled = new AtomicBoolean();
        this.isCancellable = isCancellable;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return id.toString();
    }

    public void setCancelled() {
        if (isCancellable)
            cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean isCancellable() {
        return isCancellable;
    }

}
