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
            ExternalConsole.println("File could not be loaded");
            return -1;
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
        ExternalConsole.println("Vars loaded");
        return 0;
    }

}
