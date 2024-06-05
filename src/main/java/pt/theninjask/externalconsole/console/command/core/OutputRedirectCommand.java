package pt.theninjask.externalconsole.console.command.core;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleErrorOutputStream;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleOutputStream;
import pt.theninjask.externalconsole.event.InputCommandExternalConsoleEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static pt.theninjask.externalconsole.console.command.file.ChangeDirectoryCommand.getCurrentDir;

public class OutputRedirectCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    public OutputRedirectCommand(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return ">";
    }

    @Override
    public String getDescription() {
        return "Redirects output to a file";
    }

    @Override
    public int executeCommand(String... args) {
        var ogOut = console.getOutputStream();
        var ogErr = console.getErrorOutputStream();
        if (args.length < 2) {
            return -3;
        }
        try (var dump = new FileOutputStream(getCurrentDir().resolve(args[0]).toFile())) {
            console.setOutputStream(new ExternalConsoleOutputStream(console) {
                @Override
                public void write(int b) {
                    try {
                        dump.write(Character.toString((char)b).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            console.setErrorOutputStream(new ExternalConsoleErrorOutputStream(console) {
                @Override
                public void write(int b) {
                    try {
                        dump.write(Character.toString((char)b).getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            String[] eventArgs = Arrays.copyOfRange(args, 1, args.length);
            var cmdThread = console.onCommand(new InputCommandExternalConsoleEvent(
                    eventArgs
            ));
            try {
                cmdThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -2;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } finally {
            console.setOutputStream(ogOut);
            console.setErrorOutputStream(ogErr);
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (number < 0)
            return null;
        if (number < 1) {
            return Arrays.stream(
                            Objects.requireNonNullElse(
                                    getCurrentDir().toFile()
                                            .listFiles(File::isFile),
                                    new File[0]))
                    .map(File::getName)
                    .toArray(String[]::new);
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
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An IOException has occurred!";
            case -2 -> "An InterruptedException has occurred!";
            case -3 -> "Please provide the repeat amount and name of the command (and optionally its args)";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }
}
