package pt.theninjask.externalconsole.console.command.file;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static pt.theninjask.externalconsole.console.command.file.ChangeDirectoryCommand.getCurrentDir;

public class MakeDirectoryCommand implements ExternalConsoleCommand {

    @Override
    public String getCommand() {
        return "mkdir";
    }

    @Override
    public String getDescription() {
        return "Creates a directory";
    }

    @Override
    public int executeCommand(String... args) {
        try {
            switch (args.length) {
                default -> {
                    Path newCurrentDir = getCurrentDir().resolve(Paths.get(args[0]));
                    File test = newCurrentDir.toFile();
                    if (test.exists()) {
                        ExternalConsole.println(
                                String.format("The path %s is not valid.", newCurrentDir.normalize().toAbsolutePath()));
                        break;
                    }
                    boolean result = test.mkdirs();
                    ExternalConsole.println(String.format("The path %s was %ssuccessful",
                            newCurrentDir.normalize().toAbsolutePath(), result ? "" : "un"));
                }
                case 0 -> ExternalConsole.println("The syntax of the command is incorrect");
            }
        } catch (Exception e) {
            ExternalConsole.println(e);
            return -1;
        }
        return 0;
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An exception has occurred!";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

}
