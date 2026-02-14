package pt.theninjask.externalconsole.event;

import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;

/**
 * This class is responsible for handling events
 * Since Mbassador works singleton like for each class and not object
 */
public class EventHandler {

    private final Map<Class<? extends BasicEvent>, Collection<Pair<Object, Consumer<BasicEvent>>>> listeners = new HashMap<>();

    private static final EventHandler singleton = new EventHandler();

    public static EventHandler getInstance() {
        return singleton;
    }

    public <T extends BasicEvent> void registerListener(
            Class<T> eventClass,
            Object owner,
            Consumer<T> listener
    ) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>())
                .add(Pair.of(owner, (Consumer<BasicEvent>) listener));
    }

    public void unregisterListener(
            Class<? extends BasicEvent> eventClass,
            Object owner
    ) {
        listeners.computeIfPresent(eventClass, (k, v) -> {
            v.removeIf(p -> p.getKey() == owner);
            return v;
        });
    }

    public void genericOnEvent(BasicEvent event) {
        listeners.getOrDefault(
                        event.getClass(),
                        Collections.emptyList())
                .forEach(p -> p.getValue()
                        .accept(event));

    }

    @Handler
    public void onEvent(ExternalConsoleClosingEvent event) {
        genericOnEvent(event);
    }

    @Handler
    public void onEvent(AfterCommandExecutionExternalConsole event) {
        genericOnEvent(event);
    }

    @Handler
    public void onEvent(InputCommandExternalConsoleEvent event) {
        genericOnEvent(event);
    }

    @Handler
    public void onEvent(SetClosableEvent event) {
        genericOnEvent(event);
    }

    @Handler
    public void onEvent(SetViewableEvent event) {
        genericOnEvent(event);
    }

}
