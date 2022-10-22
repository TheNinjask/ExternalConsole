package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class TinkerCommand implements ExternalConsoleCommand {
	
	@Override
	public String getCommand() {
		return "tinker";
	}

	@Override
	public String getDescription() {
		return "For test purposes";
	}
	
	@Override
	public boolean isDemo() {
		return true;
	}

	@Override
	public int executeCommand(String... args) {
		try {
			Thread.sleep(Integer.valueOf(args.length > 0 ? args[0] : "10") * 1000);
			System.out.println("Done");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
