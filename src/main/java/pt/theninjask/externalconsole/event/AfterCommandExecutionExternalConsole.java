package pt.theninjask.externalconsole.event;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class AfterCommandExecutionExternalConsole extends BasicEvent {

	private ExternalConsoleCommand cmd;
	
	private String[] args;
	
	private int result;

	public AfterCommandExecutionExternalConsole(ExternalConsoleCommand cmd, String[] args, int result) {
			super(AfterCommandExecutionExternalConsole.class.getSimpleName(), false);
			this.cmd = cmd;
			this.args = args;
			this.result = result;
		}

	public ExternalConsoleCommand getCmd() {
		return cmd;
	}

	public String[] getArgs() {
		return args;
	}

	public int getResult() {
		return result;
	}

}
