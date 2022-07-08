package pt.theninjask.externalconsole.console.command;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.util.ColorTheme;

import static pt.theninjask.externalconsole.console.ExternalConsole.DAY_THEME;
import static pt.theninjask.externalconsole.console.ExternalConsole.NIGHT_THEME;
import static pt.theninjask.externalconsole.console.ExternalConsole.TWITCH_THEME;

public class ThemeCommand implements ExternalConsoleCommand {

	// cheese to allow to pass args
	private CommandLine cmd;

	private Map<Option, Runnable> optionsMap;

	// To allow to add more themes from outside
	private Map<String, ColorTheme> themes;

	private ExternalConsole console;

	public ThemeCommand(ExternalConsole console) {
		this.console = console;
		this.themes = new HashMap<>(
				Map.ofEntries(Map.entry(TWITCH_THEME.getName(), TWITCH_THEME), Map.entry(DAY_THEME.getName(), DAY_THEME),
						Map.entry(NIGHT_THEME.getName(), NIGHT_THEME)));
		{
			Option set = Option.builder("s").longOpt("set").desc("Set a theme to External Console").numberOfArgs(1).build();
			Option add = Option.builder("a").longOpt("add").desc("Add a theme to External Console").numberOfArgs(7).build();
			optionsMap = Map.ofEntries(Map.entry(set, () -> {
				ColorTheme theme = themes.get(cmd.getOptionValue(set.getOpt()));
				if (theme != null)
					ExternalConsole.setTheme(theme);
				else
					ExternalConsole.println(String.format("Theme %s does not exist", set.getValue()));
			}), Map.entry(add, () -> {
				try {
					String name = cmd.getOptionValues(add.getOpt())[0];
					if (themes.containsKey(name)) {
						ExternalConsole.println(String.format("Theme with name %s already exists!", name));
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

					ExternalConsole.println(String.format("Theme %s added", name));
				} catch (Exception e) {
					ExternalConsole.println(e.getClass().getSimpleName());
					ExternalConsole.println(e.getMessage());
				}
			}));}
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
		optionsMap.keySet().forEach(o -> theme.addOption(o));
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
				if (ExternalConsole.getTheme() != null)
					current = ExternalConsole.getTheme().getName();
				ExternalConsole.println(String.format("Theme is set as: %s", current));
				new HelpFormatter().printHelp(new PrintWriter(console._getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
						"theme", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null,
						true);
			}
		} catch (ParseException e) {
			ExternalConsole.println(e.getMessage());
			cmd = null;
			return 1;
		}
		cmd = null;
		return 0;
	}

	@Override
	public String[] getParamOptions(int number, String[] currArgs) {
		if (number == 0)
			return optionsMap.keySet().stream().map(o -> {
				return String.format("--%s", o.getLongOpt());
			}).toList().toArray(new String[optionsMap.size()]);

		switch (currArgs[0]) {
		case "--set":
		case "-s":
			return themes.keySet().toArray(new String[themes.size()]);
		case "--add":
		case "-a":
			switch (number) {
			case 1:
				return new String[] { "name" };
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				return IntStream.range(0, 255).mapToObj(i -> {
					return Integer.toString(i);
				}).toArray(String[]::new);
			default:
				return null;
			}
		default:
			return null;
		}
	}

	@Override
	public boolean accessibleInCode() {
		return true;
	}

}
