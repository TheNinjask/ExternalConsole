package pt.theninjask.externalconsole.event;

import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * This class is responsible for handling events
 * Since Mbassador works singleton like for each class and not object
 */
public class EventHandler {

    private final Map<Class<? extends BasicEvent>, Map<Integer, List<Pair<Object, Consumer<BasicEvent>>>>> listeners = new HashMap<>();

    private static final EventHandler singleton = new EventHandler();

    public static EventHandler getInstance() {
        return singleton;
    }

    public <T extends BasicEvent> void registerListener(
            Class<T> eventClass,
            Object owner,
            Consumer<T> listener
    ) {
        registerListener(eventClass, owner, listener, 0);
    }

    public <T extends BasicEvent> void registerListener(
            Class<T> eventClass,
            Object owner,
            Consumer<T> listener,
            int priority
    ) {
        Map<Integer, List<Pair<Object, Consumer<BasicEvent>>>> prioMap = listeners.computeIfAbsent(eventClass, k -> new ConcurrentHashMap<>() {
        });
        List<Pair<Object, Consumer<BasicEvent>>> list = prioMap.computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>());
        list.add(0, Pair.of(owner, (Consumer<BasicEvent>) listener));
    }

    public void unregisterListener(
            Class<? extends BasicEvent> eventClass,
            Object owner
    ) {
        Map<Integer, List<Pair<Object, Consumer<BasicEvent>>>> prioMap = listeners.getOrDefault(eventClass, Collections.emptyMap());
        prioMap.values()
                .forEach(collection -> collection.removeIf(pair -> pair.getKey() == owner));
    }

    public void genericOnEvent(BasicEvent event) {
        Map<Integer, List<Pair<Object, Consumer<BasicEvent>>>> prioMap = listeners.getOrDefault(event.getClass(), Collections.emptyMap());
        prioMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .forEach(v -> v.forEach(pair -> pair.getValue().accept(event)));
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
