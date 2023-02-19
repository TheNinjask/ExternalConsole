package pt.theninjask.externalconsole.event;

public interface Event {

    String getName();

    String getID();

    void setCancelled();

    boolean isCancelled();

}
