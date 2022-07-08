package pt.theninjask.externalconsole.console;

import java.util.Comparator;

public interface ExternalConsoleCommand {

	public String getCommand();

	public default String getDescription() {
		return "N/A";
	}

	public int executeCommand(String... args);

	public default String resultMessage(int result) {
		return "N/A";
	}

	/**
	 * 
	 * @param number   - options of parameter in number
	 * @param currArgs - current arguments of command
	 * @return null if nothing to provide else all available options
	 */
	public default String[] getParamOptions(int number, String[] currArgs) {
		return null;
	}

	public static Comparator<ExternalConsoleCommand> comparator = new Comparator<ExternalConsoleCommand>() {
		@Override
		public int compare(ExternalConsoleCommand o1, ExternalConsoleCommand o2) {
			return o1.getCommand().compareTo(o2.getCommand());
		}
	};

	/**
	 * It determines if this command can be run in code without explicitly by the
	 * user
	 * 
	 * @return For security reasons it is false by default
	 */
	public default boolean accessibleInCode() {
		return false;
	}

	/**
	 * If true when run through the ExternalConsole it is intended to work like a
	 * java program is run in the command line
	 * 
	 * @return
	 */
	public default boolean isProgram() {
		return false;
	}
}
