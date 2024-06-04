package pt.theninjask.externalconsole.console.util;

public class UsedCommand {
    private final String fullCommand;
    private UsedCommand previous;

    private UsedCommand next;


    public static final UsedCommand NULL_UC = new UsedCommand();

    private UsedCommand() {
        this.fullCommand = null;
        this.previous = this;
        this.next = this;
    }

    public UsedCommand(String fullCommand, UsedCommand previous, UsedCommand next) {
        this.fullCommand = fullCommand;
        this.previous = previous;
        this.next = next;
    }

    public String getFullCommand() {
        return this.fullCommand;
    }

    public UsedCommand getPrevious() {
        return this.previous;
    }

    public UsedCommand getNext() {
        return this.next;
    }

    public void setPrevious(UsedCommand previous) {
        this.previous = previous;
    }

    public void setNext(UsedCommand next) {
        this.next = next;
    }
}
