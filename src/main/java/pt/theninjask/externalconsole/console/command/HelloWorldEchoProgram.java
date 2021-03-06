package pt.theninjask.externalconsole.console.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class HelloWorldEchoProgram implements ExternalConsoleCommand {

	@Override
	public String getCommand() {
		return "hello_world";
	}

	@Override
	public int executeCommand(String... args) {
		ExternalConsole.setSystemStreams();
		BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
		try {
			String tmp;
			while (true)
				while ((tmp = read.readLine()) != null) {
					System.out.println("Hello world to %s!".formatted(tmp));
					if (tmp.equals("hw"))
						return 0;
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		ExternalConsole.revertSystemStreams();
		return 0;
	}

	@Override
	public boolean accessibleInCode() {
		return true;
	}

	@Override
	public boolean isProgram() {
		return true;
	}

	@Override
	public String[] getParamOptions(int number, String[] currArgs) {
		if(number == 0)
			return new String[] {"demo_0", "demo_1"}; 
		return new String[] {"demo_2", "demo_3"};
	}

}
