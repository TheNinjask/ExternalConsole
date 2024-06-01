package pt.theninjask.externalconsole.console.command.file;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StartCommand implements ExternalConsoleCommand {


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
            ExternalConsole.println(e);
            return 1;
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (number) {
            case 0 ->
                    ChangeDirectoryCommand.getCurrentDir().toFile().list();
            default -> null;
        };
    }
}
