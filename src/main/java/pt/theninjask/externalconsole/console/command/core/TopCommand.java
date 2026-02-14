package pt.theninjask.externalconsole.console.command.core;

import org.apache.commons.cli.*;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.io.PrintWriter;
import java.util.Map;

public class TopCommand implements ExternalConsoleCommand {

    private final Map<Option, Runnable> optionsMap;
    private final ExternalConsole console;

    public TopCommand(ExternalConsole console) {
        this.console = console;
        optionsMap = Map
                .ofEntries(
                        Map.entry(
                                new Option("t", "true", false, "AlwaysOnTop Set to True"),
                                () -> console.setAlwaysOnTop(true)),
                        Map.entry(
                                new Option("f", "false", false, "AlwaysOnTop Set to False"),
                                () -> console.setAlwaysOnTop(false)));
    }

    @Override
    public String getCommand() {
        return "top";
    }

    @Override
    public String getDescription() {
        return "Flag for ExternalConsole be always on top";
    }

    @Override
    public int executeCommand(String... args) {
        Options options = new Options();
        OptionGroup top = new OptionGroup();
        optionsMap.keySet().forEach(top::addOption);
        options.addOptionGroup(top);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            boolean succ = optionsMap.entrySet().parallelStream().anyMatch(e -> {
                if (cmd.hasOption(e.getKey().getOpt())) {
                    e.getValue().run();
                    return true;
                } else {
                    return false;
                }
            });
            if (!succ) {
                console.println(String.format("AlwaysOnTop is set as: %s", console.isAlwaysOnTop()));
                new HelpFormatter().printHelp(new PrintWriter(console.getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
                        "top", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null,
                        true);
            }
        } catch (ParseException e) {
            console.println(e.getMessage());
            return -1;
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (number) {
            case 0 -> optionsMap
                    .keySet()
                    .stream()
                    .map(o -> String.format("--%s", o.getLongOpt()))
                    .toList()
                    .toArray(new String[optionsMap.size()]);
            default -> null;
        };
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public String resultMessage(int result) {
        return switch (result) {
            case -1 -> "An exception has occurred!";
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }

}
