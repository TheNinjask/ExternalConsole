package pt.theninjask.externalconsole.console.command.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadVarsFileCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    public LoadVarsFileCommand(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "loadVarsFile";
    }

    @Override
    public String getDescription() {
        return "Loads vars from file";
    }

    @Override
    public int executeCommand(String... args) {
        if (args.length == 0 || Files.notExists(Path.of(args[0]))) {
            return -2;
        }
        try {
            var json = new ObjectMapper().readTree(Paths.get(args[0]).toFile());
            if (json.isObject()) {
                json.fields()
                        .forEachRemaining(field ->
                                console.getAllVars()
                                        .put(field.getKey(),
                                                field.getValue().isObject() || field.getValue().isArray() ?
                                                        field.getValue().toString() : field.getValue().asText()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An exception has occurred!";
            case -2 -> "File could not be loaded";
            case 0 -> "Vars loaded";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

}
