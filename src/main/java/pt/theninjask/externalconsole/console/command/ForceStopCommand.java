package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class ForceStopCommand implements ExternalConsoleCommand {

	private ExternalConsole console;

	public ForceStopCommand(ExternalConsole console) {
		this.console = console;
	}
	
	@Override
	public String getCommand() {
		return "forceStop";
	}

	@Override
	public String getDescription() {
		return "It terminates the running JVM";
	}

	@Override
	public int executeCommand(String... args) {
		console.dispose();
		System.exit(0);
		return 0;
	}

}
