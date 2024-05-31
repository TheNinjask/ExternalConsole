package pt.theninjask.externalconsole.console.command.core;

import lombok.RequiredArgsConstructor;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

@RequiredArgsConstructor
public class ViewVarCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    @Override
    public String getCommand() {
        return "viewVar";
    }

    @Override
    public String getDescription() {
        return "Views a(ll) console variable(s)";
    }

    @Override
    public int executeCommand(String... args) {
        var vars = console.getAllVars();
        if (args.length == 0) {
            vars.forEach((key, value) ->
                    ExternalConsole.println(
                            "Variable %1$s = %2$s".formatted(key, value)
                    ));
        } else {
            ExternalConsole.println(
                    "Variable %1$s = %2$s".formatted(args[0], vars.get(args[0]))
            );
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return number == 0 ? console.getAllVars().keySet().toArray(String[]::new) : null;
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "Provide args";
            case 0 -> "Success";
            default -> "Error unkown %s".formatted(result);
        };
    }
}
