package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class ClearCommand implements ExternalConsoleCommand {

	private ExternalConsole console;

	public ClearCommand(ExternalConsole console) {
		this.console = console;
	}
	
	@Override
	public String getCommand() {
		return "cls";
	}

	@Override
	public String getDescription() {
		return "Clears ExternalConsole";
	}

	@Override
	public int executeCommand(String... args) {
		console._getScreen().setText("");
		return 0;
	}

	@Override
	public boolean accessibleInCode() {
		return true;
	}

}
