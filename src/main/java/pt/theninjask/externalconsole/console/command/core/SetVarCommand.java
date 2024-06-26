package pt.theninjask.externalconsole.console.command.core;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

public class SetVarCommand implements ExternalConsoleCommand {

    private final ExternalConsole console;

    public SetVarCommand(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "setVar";
    }

    @Override
    public String getDescription() {
        return "Creates/Clear a console variable";
    }

    @Override
    public int executeCommand(String... args) {
        if (args.length == 0 || this.getCommand().equals(args[0])) {
            return -1;
        }
        var varName = args[0];
        var varValue = args.length == 1 ? null : args[1];
        var vars = console.getAllVars();
        if (varValue == null) {
            vars.entrySet()
                    .stream()
                    .filter(entry -> varName.equals(entry.getValue()))
                    .forEach(entry -> vars.remove(entry.getKey()));
        } else {
            vars.put(varName, varValue);
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
            case -1 -> "Provide Valid Args";
            case 0 -> "Vars set";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }
}
