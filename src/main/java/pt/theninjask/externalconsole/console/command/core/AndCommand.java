package pt.theninjask.externalconsole.console.command.core;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.event.InputCommandExternalConsoleEvent;

import java.util.Arrays;
import java.util.stream.IntStream;

public class AndCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    public AndCommand(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "&";
    }

    @Override
    public String getDescription() {
        return "Allow for the concatenation of 1+ commands (e.g. & cmd1 0 cmd2 2 arg1 arg2 cmd3 0 cmd4 1 arg1)";
    }

    @Override
    public int executeCommand(String... args) {
        var it = Arrays.stream(args).iterator();
        while (it.hasNext()) {
            var cmd = it.next();
            var argSize = Integer.parseInt(it.next());
            var eventArgs = new String[argSize + 1];
            eventArgs[0] = cmd;
            IntStream.range(1, argSize + 1).forEachOrdered(i -> eventArgs[i] = it.next());
            var cmdThread = console.onCommand(new InputCommandExternalConsoleEvent(
                    eventArgs
            ));
            try {
                cmdThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    private static final int CMD = 0;
    private static final int ARGS_AMOUNT = 1;
    private static final int CMD_ARGS = 2;

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (true)
            return null;
        int nextType = CMD;
        var it = Arrays.stream(currArgs).iterator();
        String lastCmd = null;
        int lastCmdNumberIndex = -1;
        int lastCmdNumber = -1;
        int lastCmdCurrentNumber = lastCmdNumber;
        while (it.hasNext()) {
            switch (nextType) {
                case CMD -> {
                    lastCmd = it.next();
                    nextType = ARGS_AMOUNT;
                }
                case ARGS_AMOUNT -> {
                    lastCmdNumber = Integer.parseInt(it.next());
                    lastCmdNumberIndex = number;
                    lastCmdCurrentNumber = 0;
                    nextType = lastCmdNumber == lastCmdCurrentNumber ? CMD : CMD_ARGS;
                }
                case CMD_ARGS -> {
                    lastCmdCurrentNumber++;
                    if (lastCmdNumber == lastCmdCurrentNumber)
                        nextType = CMD;
                }
            }
            number--;
        }
        switch (nextType) {
            default:
            case CMD:
                return null;
            case ARGS_AMOUNT:
                return IntStream.range(0, 10).mapToObj(Integer::toString).toArray(String[]::new);
            case CMD_ARGS:
                var cmd = console._getAllCommands().getOrDefault(lastCmd, null);
                return cmd == null ? null :
                        cmd.getParamOptions(
                                lastCmdCurrentNumber,
                                Arrays.copyOfRange(currArgs, lastCmdNumberIndex + 1, currArgs.length));
        }
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }
}
