package pt.theninjask.externalconsole.console.command;

import javax.swing.JFrame;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class HideCommand implements ExternalConsoleCommand {

	private ExternalConsole console;

	public HideCommand(ExternalConsole console) {
		this.console = console;
	}
	
	@Override
	public String getCommand() {
		return "hide";
	}

	@Override
	public String getDescription() {
		return "Hides External Console";
	}

	@Override
	public int executeCommand(String... args) {
		console.setExtendedState(JFrame.ICONIFIED);
		return 0;
	}

	@Override
	public boolean accessibleInCode() {
		return true;
	}

}
