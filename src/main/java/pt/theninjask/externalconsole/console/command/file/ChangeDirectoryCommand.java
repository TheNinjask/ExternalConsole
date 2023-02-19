package pt.theninjask.externalconsole.console.command.file;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChangeDirectoryCommand implements ExternalConsoleCommand {

    private static Path currentDir = Paths.get(".");

    public static Path getCurrentDir() {
        return currentDir;
    }

    @Override
    public String getCommand() {
        return "cd";
    }

    @Override
    public String getDescription() {
        return "Change directory";
    }

    @Override
    public int executeCommand(String... args) {
        try {
            switch (args.length) {
                default:
                    Path newCurrentDir = currentDir.resolve(Paths.get(args[0]));
                    File test = newCurrentDir.toFile();
                    if (!test.isDirectory()) {
                        ExternalConsole.println(
                                String.format("The path %s is not valid.", newCurrentDir.normalize().toAbsolutePath()));
                        break;
                    }
                    currentDir = newCurrentDir.toRealPath();
                case 0:
                    ExternalConsole
                            .println(String.format("Current Directory:\n\t%s", currentDir.normalize().toAbsolutePath()));
                    break;
            }
        } catch (Exception e) {
            ExternalConsole.println(e);
            return 1;
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (number) {
            case 0 -> currentDir.toFile().list((dir, name) -> new File(dir, name).isDirectory());
            default -> null;
        };
    }

}
