package pt.theninjask.externalconsole.console.command.file;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import static pt.theninjask.externalconsole.console.command.file.ChangeDirectoryCommand.getCurrentDir;

public class TreeCommand implements ExternalConsoleCommand {

	private static final int QUEUE_LIMIT = 10;
	
	private static final String ENTRY_NO_NEXT = "└───";

	private static final String ENTRY_NEXT = "├───";

	private static final String VERTICAL = "│";

	// private static final String HORIZONTAL = "─";

	private static final int ARRAY = 0;

	private static final int ARRAY_INDEX = 1;

	private static final int ROOT_ARRAY = 2;

	private static final int PRE_STRING = 3;

	private Map<Option, Runnable> optionsMap = Map.ofEntries(
			Map.entry(Option.builder("f").longOpt("files").desc("Also displays files & filter by extension(s)")
					.numberOfArgs(Option.UNLIMITED_VALUES).optionalArg(true).build(), () -> {
					}));

	@Override
	public String getCommand() {
		return "tree";
	}

	@Override
	public String getDescription() {
		return "Graphically displays the directory structure of a path";
	}

	/*
	 * private void printDirectoryTree(Path dir) { printDirectoryTree(dir, false); }
	 */

	private void printDirectoryTree(Path dir, boolean includeFiles, String... fileExtensions) {
		ExternalConsole.println(dir.normalize().toAbsolutePath());
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File dir) {
				return dir.isDirectory() || (includeFiles
						&& (fileExtensions == null 
						|| fileExtensions.length==0 
						|| Arrays.stream(fileExtensions).anyMatch(ext -> {
							return dir.getName().endsWith(ext);
						})));
			}
		};
		File[] files = dir.toFile().listFiles(filter);
		if (files.length == 0) {
			ExternalConsole.println("No subfolders exist");
		}
		Object[] linkedList = new Object[4];

		linkedList[ARRAY] = files;
		linkedList[ARRAY_INDEX] = 0;
		linkedList[ROOT_ARRAY] = null;
		linkedList[PRE_STRING] = "";
		int currQueue = 0;
		int queuePatience = 0;
		StringBuilder build = new StringBuilder();
		for (int i = 0; i < ((File[]) linkedList[ARRAY]).length; i++) {
			File sub = ((File[]) linkedList[ARRAY])[i];
			boolean hasNext = i + 1 < ((File[]) linkedList[ARRAY]).length;
			build.append(String.format("%s%s%s", linkedList[PRE_STRING],
					hasNext ? ENTRY_NEXT : ENTRY_NO_NEXT, sub.getName()));
			currQueue++;
			if(currQueue>queuePatience) {
				ExternalConsole.println(build.toString());
				build = new StringBuilder();
				queuePatience += 1 + currQueue/2;
				if(queuePatience>QUEUE_LIMIT)
					queuePatience = QUEUE_LIMIT;
				currQueue = 0;
			}else {
				build.append('\n');
			}
			if (sub.isDirectory())
				files = sub.listFiles(filter);
			if (sub.isDirectory() && files.length != 0) {
				Object[] deeperLinkedList = new Object[4];
				deeperLinkedList[ARRAY] = files;
				deeperLinkedList[ARRAY_INDEX] = 0;
				deeperLinkedList[ROOT_ARRAY] = linkedList;
				deeperLinkedList[PRE_STRING] = String.format("%s%s", linkedList[PRE_STRING],
						hasNext ? VERTICAL + "      " : "        ");
				linkedList[ARRAY_INDEX] = i;

				linkedList = deeperLinkedList;
				i = -1;
			} else if (!hasNext) {
				do {
					if (linkedList[ROOT_ARRAY] == null)
						break;
					linkedList = (Object[]) linkedList[ROOT_ARRAY];
					i = (int) linkedList[ARRAY_INDEX];
					hasNext = i + 1 < ((File[]) linkedList[ARRAY]).length;
				} while (!hasNext);
			}
		}
		if(currQueue>0)
			ExternalConsole.println(build.deleteCharAt(build.length()-1).toString());
	}

	@Override
	public int executeCommand(String... args) {
		try {
			Options options = new Options();
			OptionGroup tree = new OptionGroup();
			optionsMap.keySet().forEach(o -> tree.addOption(o));
			options.addOptionGroup(tree);

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			switch (cmd.getArgs().length) {
			case 0:
				printDirectoryTree(getCurrentDir(), cmd.hasOption('f'), cmd.getOptionValues('f'));
				break;
			default:
				File path = getCurrentDir().resolve(Paths.get(cmd.getArgs()[0])).toFile();
				if (!path.exists() || !path.isDirectory())
					ExternalConsole.println("Invalid path");
				else
					printDirectoryTree(path.toPath(), cmd.hasOption('f'), cmd.getOptionValues('f'));
				break;
			}
		} catch (Exception e) {
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

}
