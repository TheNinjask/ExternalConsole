package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.util.Arrays;
import java.util.stream.IntStream;

public class RepeaterCommand implements ExternalConsoleCommand {

    @Override
    public String getCommand() {
        return "cmdRepeater";
    }

    @Override
    public String getDescription() {
        return "Repeats a requested command";
    }

    @Override
    public int executeCommand(String... args) {
        if (args.length < 2) {
            return -1;
        }
        int amount = Integer.parseInt(args[0]);
        String cmdName = args[1];
        String[] cmdArgs = Arrays.copyOfRange(args, 2, args.length);
        for (int loop = 0; loop < amount; loop++) {
            boolean exeRes = ExternalConsole.executeCommand(cmdName, cmdArgs);
            if (!exeRes) {
                return -2;
            }
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (number < 0)
            return null;
        if (number < 1) {
            return IntStream.range(0, 11).mapToObj(Integer::toString).toArray(String[]::new);
        }
        if (number < 2) {
            return null;
        }
        String cmdName = currArgs[1];
        ExternalConsoleCommand cmd = ExternalConsole.getCommand(cmdName);
        if (cmd == null)
            return null;
        String[] cmdArgs = Arrays.copyOfRange(currArgs, 2, currArgs.length);
        return cmd.getParamOptions(number - 2, cmdArgs);
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "Please provide the repeat amount and name of the command (and optionally its args)";
            case -2 -> "Command either not found or not accessible in code";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }
}
