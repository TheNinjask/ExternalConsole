package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import static pt.theninjask.externalconsole.console.command.TinkerCommand.isTinkeringDisabled;

public class NanoCommand implements ExternalConsoleCommand {

	@Override
	public String getCommand() {
		return "nano";
	}

	@Override
	public String getDescription() {
		return "Text Editor";
	}

	@Override
	public int executeCommand(String... args) {
		if(isTinkeringDisabled())
			return 0;
		return 0;
	}
	
	@Override
	public boolean isProgram() {
		return true;
	}
}
