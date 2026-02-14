package pt.theninjask.externalconsole.console.command.file;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class StartCommand implements ExternalConsoleCommand {

    private static ExternalConsole console;

    @Override
    public String getCommand() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Opens path in desktop";
    }

    @Override
    public int executeCommand(String... args) {
        try {
            switch (args.length) {
                default:
                    Path tmpCurrentDir = ChangeDirectoryCommand.getCurrentDir().resolve(Paths.get(args[0]));
                    Desktop.getDesktop().open(tmpCurrentDir.toFile());
                    break;
                case 0:
                    Desktop.getDesktop().open(ChangeDirectoryCommand.getCurrentDir().toFile());
                    break;
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
            case 0 -> ChangeDirectoryCommand.getCurrentDir().toFile().list();
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
