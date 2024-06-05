package pt.theninjask.externalconsole.console;

import java.util.Comparator;

public interface ExternalConsoleCommand {

    String getCommand();

    default String getDescription() {
        return "N/A";
    }

    /**
     *
     * @param args args
     * @return standard >= 0: success, <0 error
     */
    int executeCommand(String... args);

    /**
     *
     * @param result int code from executeCommand
     * @return null if there is no result message, string that corresponds to value returned by executeCommand
     */
    default String resultMessage(int result) {
        return null;
    }

    /**
     * @param number   - options of parameter in number
     * @param currArgs - current arguments of command
     * @return null if nothing to provide else all available options
     */
    default String[] getParamOptions(int number, String[] currArgs) {
        return null;
    }

    Comparator<ExternalConsoleCommand> comparator = Comparator.comparing(ExternalConsoleCommand::getCommand);

    /**
     * It determines if this command can be run in code without explicitly by the
     * user
     *
     * @return For security reasons it is false by default
     */
    default boolean accessibleInCode() {
        return false;
    }

    /**
     * If true when run through the ExternalConsole it is intended to work like a
     * java program is run in the command line
     *
     * @return default false
     */
    default boolean isProgram() {
        return false;
    }

    /**
     * If true, it means the command is not intended for use and can be removed
     * with ExternalConsole.removeDemoCmds()
     *
     * @return default false
     */
    default boolean isDemo() {
        return false;
    }
}
