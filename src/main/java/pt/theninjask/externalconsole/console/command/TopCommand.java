package pt.theninjask.externalconsole.console.command;

import java.io.PrintWriter;
import java.util.Map;

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

public class TopCommand implements ExternalConsoleCommand {

	private Map<Option, Runnable> optionsMap;
	private ExternalConsole console;

	public TopCommand(ExternalConsole console) {
		this.console = console;
		optionsMap = Map
				.ofEntries(Map.entry(new Option("t", "true", false, "AlwaysOnTop Set to True"), () -> {
					console.setAlwaysOnTop(true);
				}), Map.entry(new Option("f", "false", false, "AlwaysOnTop Set to False"), () -> {
					console.setAlwaysOnTop(false);
				}));
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
		optionsMap.keySet().forEach(o -> top.addOption(o));
		options.addOptionGroup(top);
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			boolean succ = optionsMap.entrySet().parallelStream().filter(e -> {
				if (cmd.hasOption(e.getKey().getOpt())) {
					e.getValue().run();
					return true;
				} else {
					return false;
				}
			}).count() > 0;
			if (!succ) {
				ExternalConsole.println(String.format("AlwaysOnTop is set as: %s", console.isAlwaysOnTop()));
				new HelpFormatter().printHelp(new PrintWriter(console._getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
						"top", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null,
						true);
			}
		} catch (ParseException e) {
			ExternalConsole.println(e.getMessage());
			return 1;
		}
		return 0;
	}

	@Override
	public String[] getParamOptions(int number, String[] currArgs) {
		switch (number) {
		case 0:
			return optionsMap.keySet().stream().map(o -> {
				return String.format("--%s", o.getLongOpt());
			}).toList().toArray(new String[optionsMap.size()]);
		default:
			return null;
		}
	}

	@Override
	public boolean accessibleInCode() {
		return true;
	}

}
