package pt.theninjask.externalconsole.console.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.event.InputCommandExternalConsoleEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LoadScriptCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    public LoadScriptCommand(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "loadScript";
    }

    @Override
    public String getDescription() {
        return "Loads a json file with an array of commands";
    }

    @Override
    public int executeCommand(String... args) {
        if (args.length == 0 || Files.notExists(Path.of(args[0]))) {
            return -2;
        }
        try {
            var json = new ObjectMapper().readTree(Paths.get(args[0]).toFile());
            if (json.isObject()) {
                executeJsonObject(json);
            } else if (json.isArray()) {
                for (Iterator<JsonNode> it = json.elements(); it.hasNext(); ) {
                    var e = it.next();
                    executeJsonObject(e);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private void executeJsonObject(JsonNode json) throws InterruptedException {
        if (json.isObject()) {
            String cmd = json.get("cmd").asText();
            String[] args = StreamSupport.stream(
                            ((Iterable<JsonNode>)
                                    (() -> json.get("args").elements())).spliterator(), true)
                    .map(jsonNode -> jsonNode.isObject() || jsonNode.isArray() ? jsonNode.toString() : jsonNode.asText())
                    .toArray(String[]::new);
            args = console.parseArgsVars(args);
            String[] eventArgs = Stream.concat(
                            Stream.of(cmd),
                            Arrays.stream(args))
                    .toArray(String[]::new);
            var cmdThread = console.onCommand(new InputCommandExternalConsoleEvent(console, eventArgs));
            if (cmdThread != null)
                cmdThread.join();
        }
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return ExternalConsoleCommand.super.getParamOptions(number, currArgs);
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case 0 -> "Script executed";
            case -1 -> "An exception has occurred!";
            case -2 -> "File could not be loaded";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }
}
