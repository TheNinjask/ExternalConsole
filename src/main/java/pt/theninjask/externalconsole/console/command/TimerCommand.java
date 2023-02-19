package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.util.Arrays;

public class TimerCommand implements ExternalConsoleCommand {

    @Override
    public String getCommand() {
        return "timer";
    }

    @Override
    public String getDescription() {
        return "This times how long a command takes";
    }

    @Override
    public int executeCommand(String... args) {
        if (args.length == 0) {
            ExternalConsole.println("Please provide the name of the command (and optionally its args)");
            return -1;
        }
        String cmdName = args[0];
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        long start = System.nanoTime();
        boolean exeRes = ExternalConsole.executeCommand(cmdName, cmdArgs);
        if (!exeRes) {
            ExternalConsole.println("Command either not found or not accessible in code");
            return -1;
        }
        long stop = System.nanoTime();
        long time = stop - start;
        ExternalConsole.println(String.format("Time taken: %s nanoseconds", time));
        time = time / 1000000000;
        if (time > 0)
            ExternalConsole.println(String.format("Time taken: %s seconds", time));
        time = time / 60;
        if (time > 0)
            ExternalConsole.println(String.format("Time taken: %s minutes", time));
        return Long.valueOf(stop - start).intValue();
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (number < 1)
            return null;
        String cmdName = currArgs[0];
        ExternalConsoleCommand cmd = ExternalConsole.getCommand(cmdName);
        if (cmd == null)
            return null;
        String[] cmdArgs = Arrays.copyOfRange(currArgs, 1, currArgs.length);
        return cmd.getParamOptions(number - 1, cmdArgs);
    }

}
