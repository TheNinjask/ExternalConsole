package pt.theninjask.externalconsole.console.command.file;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.util.Bytes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static pt.theninjask.externalconsole.console.command.file.ChangeDirectoryCommand.getCurrentDir;

@RequiredArgsConstructor
public class ListDirectoryCommand implements ExternalConsoleCommand {

    private static ExternalConsole console;
    @Override
    public String getCommand() {
        return "ls";
    }

    @Override
    public String getDescription() {
        return "List directory";
    }

    private void printDirectoryContents(Path dir) {
        console.println("Contains:");
        for (File sub : Objects.requireNonNull(dir.toFile().listFiles())) {
            Object[] result = Bytes.roundByteSize(sub.length());
            double size = (double) result[0];
            String sizeType = result[1].toString();
            console.println(String.format("\t%s (%s)%s", sub.getName(),
                    sub.isFile() ? "File" : sub.isDirectory() ? "Directory" : "Unknown",
                    sub.isFile() ? String.format(" %.2f %s", size, sizeType) : ""));
        }
    }

    @Override
    public int executeCommand(String... args) {
        try {
            switch (args.length) {
                case 0 -> {
                    console.println(
                            String.format("Current Directory:\n\t%s", getCurrentDir().normalize().toAbsolutePath()));
                    printDirectoryContents(getCurrentDir());
                }
                default -> {
                    File path = getCurrentDir().resolve(Paths.get(args[0])).toFile();
                    if (!path.exists())
                        console.println("No such file or directory");
                    else if (path.isFile()) {
                        Object[] result = Bytes.roundByteSize(path.length());
                        double size = (double) result[0];
                        String sizeType = result[1].toString();
                        console.println(String.format("%s (File)\n\tHidden: %s\n\tSize: %.2f %s",
                                path.getName(), path.isHidden(), size, sizeType));
                    } else if (path.isDirectory()) {
                        console
                                .println(String.format("Directory:\n\t%s", path.toPath().normalize().toAbsolutePath()));
                        printDirectoryContents(path.toPath());
                    }
                }
            }
        } catch (Exception e) {
            console.println(e);
            return -1;
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (number) {
            case 0 -> getCurrentDir().toFile().list();
            default -> null;
        };
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An exception has occurred!";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }
}
