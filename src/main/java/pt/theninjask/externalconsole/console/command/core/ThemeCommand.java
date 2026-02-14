package pt.theninjask.externalconsole.console.command.core;

import org.apache.commons.cli.*;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.util.ColorTheme;

import java.awt.*;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;

import static pt.theninjask.externalconsole.console.ExternalConsole.*;

public class ThemeCommand implements ExternalConsoleCommand {

    // cheese to allow to pass args
    private CommandLine cmd;

    private final Map<Option, Runnable> optionsMap;

    // To allow to add more themes from outside
    private final Map<String, ColorTheme> themes;

    private final ExternalConsole console;

    public ThemeCommand(ExternalConsole console) {
        this.console = console;
        this.themes = new HashMap<>(
                Map.ofEntries(Map.entry(TWITCH_THEME.name(), TWITCH_THEME), Map.entry(DAY_THEME.name(), DAY_THEME),
                        Map.entry(NIGHT_THEME.name(), NIGHT_THEME)));
        {
            Option set = Option.builder("s").longOpt("set").desc("Set a theme to External Console").numberOfArgs(1).build();
            Option add = Option.builder("a").longOpt("add").desc("Add a theme to External Console").numberOfArgs(7).build();
            optionsMap = Map.ofEntries(Map.entry(set, () -> {
                ColorTheme theme = themes.get(cmd.getOptionValue(set.getOpt()));
                if (theme != null)
                    console.setTheme(theme);
                else
                    console.println(String.format("Theme %s does not exist", set.getValue()));
            }), Map.entry(add, () -> {
                try {
                    String name = cmd.getOptionValues(add.getOpt())[0];
                    if (themes.containsKey(name)) {
                        console.println(String.format("Theme with name %s already exists!", name));
                        return;
                    }
                    int rf = Integer.parseInt(cmd.getOptionValues(add.getOpt())[1]);
                    // rf = Integer.min(Integer.max(0,rf),255);
                    int gf = Integer.parseInt(cmd.getOptionValues(add.getOpt())[2]);
                    // gf = Integer.min(Integer.max(0,gf),255);
                    int bf = Integer.parseInt(cmd.getOptionValues(add.getOpt())[3]);
                    // bf = Integer.min(Integer.max(0,bf),255);
                    int rb = Integer.parseInt(cmd.getOptionValues(add.getOpt())[4]);
                    // rb = Integer.min(Integer.max(0,rb),255);
                    int gb = Integer.parseInt(cmd.getOptionValues(add.getOpt())[5]);
                    // gb = Integer.min(Integer.max(0,gb),255);
                    int bb = Integer.parseInt(cmd.getOptionValues(add.getOpt())[6]);
                    // bb = Integer.min(Integer.max(0,bb),255);

                    themes.put(name, new ColorTheme(name, new Color(rf, gf, bf), new Color(rb, gb, bb)));

                    console.println(String.format("Theme %s added", name));
                } catch (Exception e) {
                    console.println(e.getClass().getSimpleName());
                    console.println(e.getMessage());
                }
            }));
        }
    }

    @Override
    public String getCommand() {
        return "theme";
    }

    @Override
    public String getDescription() {
        return "Changes Theme of console";
    }

    @Override
    public int executeCommand(String... args) {
        Options options = new Options();
        OptionGroup theme = new OptionGroup();
        optionsMap.keySet().forEach(theme::addOption);
        options.addOptionGroup(theme);
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
            Optional<Entry<Option, Runnable>> option = optionsMap.entrySet().stream()
                    .filter(e -> cmd.hasOption(e.getKey().getOpt())).findFirst();
            if (option.isPresent())
                option.get().getValue().run();
            else {
                String current = "Unknown";
                if (console.getTheme() != null)
                    current = console.getTheme().name();
                console.println(String.format("Theme is set as: %s", current));
                new HelpFormatter().printHelp(new PrintWriter(console.getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
                        this.getCommand(), null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null,
                        true);
            }
        } catch (ParseException e) {
            console.println(e.getMessage());
            cmd = null;
            return -1;
        }
        cmd = null;
        return 0;
    }

    @Override
    public String[] getParamOptions(int number, String[] currArgs) {
        if (number == 0)
            return optionsMap.keySet()
                    .stream()
                    .map(o -> String.format("--%s", o.getLongOpt()))
                    .toList()
                    .toArray(new String[optionsMap.size()]);

        return switch (currArgs[0]) {
            case "--set", "-s" -> themes.keySet().toArray(new String[0]);
            case "--add", "-a" -> switch (number) {
                case 1 -> new String[]{"name"};
                case 2, 3, 4, 5, 6, 7 -> IntStream.range(0, 255).mapToObj(Integer::toString).toArray(String[]::new);
                default -> null;
            };
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
