package pt.theninjask.externalconsole.console.command.core;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class ViewVarCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    public ViewVarCommand(ExternalConsole console) {
        this.console = console;
    }

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
}
