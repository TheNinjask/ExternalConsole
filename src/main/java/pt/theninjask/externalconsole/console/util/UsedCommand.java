package pt.theninjask.externalconsole.console.util;

public class UsedCommand {

	private UsedCommand previous;

	private UsedCommand next;

	private String fullCommand;

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
		return fullCommand;
	}

	public UsedCommand getPrevious() {
		return previous;
	}

	public void setPrevious(UsedCommand previous) {
		this.previous = previous;
	}

	public UsedCommand getNext() {
		return next;
	}

	public void setNext(UsedCommand next) {
		this.next = next;
	}

}
