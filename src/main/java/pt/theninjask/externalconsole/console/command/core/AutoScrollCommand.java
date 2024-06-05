package pt.theninjask.externalconsole.console.command.core;

import org.apache.commons.cli.*;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.io.PrintWriter;
import java.util.Map;

public class AutoScrollCommand implements ExternalConsoleCommand {

    private final Map<Option, Runnable> optionsMap;
    private final ExternalConsole console;

    public AutoScrollCommand(ExternalConsole console) {
        this.console = console;
        optionsMap = Map
                .ofEntries(
                        Map.entry(
                                new Option("a", "auto", false, "Sets Scroll to Auto"),
                                () -> console._setAutoScroll(true)),
                        Map.entry(
                                new Option("m", "manual", false, "Sets Scroll to Manual"),
                                () -> console._setAutoScroll(false)));
    }

    @Override
    public String getCommand() {
        return "autoscroll";
    }

    @Override
    public String getDescription() {
        return "Enables/Disables autoscrolling of ExternalConsole";
    }

    @Override
    public int executeCommand(String... args) {
        Options options = new Options();
        OptionGroup scroll = new OptionGroup();
        optionsMap.keySet().forEach(scroll::addOption);
        options.addOptionGroup(scroll);
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
                ExternalConsole.println(String.format("autoscroll is set as: %s", console._getAutoScroll()));
                new HelpFormatter().printHelp(new PrintWriter(console.getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
                        "autoscroll", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
                        null, true);
            }

        } catch (ParseException e) {
            ExternalConsole.println(e.getMessage());
            return -1;
        }
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        return switch (number) {
            case 0 -> optionsMap.keySet()
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
        return switch (result){
            case -1 -> null;
            default -> ExternalConsoleCommand.super.resultMessage(result);
        };
    }
}
