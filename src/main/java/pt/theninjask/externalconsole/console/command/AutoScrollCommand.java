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

public class AutoScrollCommand implements ExternalConsoleCommand{
	
	private Map<Option, Runnable> optionsMap;
	private ExternalConsole console;

	public AutoScrollCommand(ExternalConsole console) {
		this.console = console;
		  optionsMap = Map
					.ofEntries(Map.entry(new Option("a", "auto", false, "Sets Scroll to Auto"), () -> {
						console._setAutoScroll(true);
					}), Map.entry(new Option("m", "manual", false, "Sets Scroll to Manual"), () -> {
						console._setAutoScroll(false);
					}));
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
		// scroll.setRequired(true);
		optionsMap.keySet().forEach(o -> scroll.addOption(o));
		options.addOptionGroup(scroll);
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
				ExternalConsole.println(String.format("autoscroll is set as: %s",console._getAutoScroll()));
				new HelpFormatter().printHelp(new PrintWriter(console._getOutputStream(), true), HelpFormatter.DEFAULT_WIDTH,
						"autoscroll", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
						null, true);
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
