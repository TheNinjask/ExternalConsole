package pt.theninjask.externalconsole.event;

import lombok.Getter;
import pt.theninjask.externalconsole.console.ExternalConsole;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BasicEvent implements Event {

    @Getter
    private final ExternalConsole owner;
    private final UUID id;

    private final String name;

    private final AtomicBoolean cancelled;

    private final boolean isCancellable;

    public BasicEvent(ExternalConsole console) {
        this(console, BasicEvent.class.getSimpleName(), true);
    }

    public BasicEvent(ExternalConsole console, boolean isCancellable) {
        this(console, BasicEvent.class.getSimpleName(), isCancellable);
    }

    public BasicEvent(ExternalConsole console, String name) {
        this(console, name, true);
    }

    public BasicEvent(ExternalConsole console, String name, boolean isCancellable) {
        this.owner = console;
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
