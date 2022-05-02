package pt.theninjask.externalconsole.additionalCommands;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
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

public class FileCommands {

	private static Path currentDir = Paths.get(".");

	private enum Bytes {
		TERABYTE("TB", null), GIGABYTE("GB", TERABYTE), MEGABYTE("MB", GIGABYTE), KILOBYTE("KB", MEGABYTE),
		BYTE("B", KILOBYTE);

		private String sigla;
		private Bytes bigger;

		private Bytes(String sigla, Bytes bigger) {
			this.sigla = sigla;
			this.bigger = bigger;
		}

		public String toString() {
			return sigla;
		}

		public Bytes getBigger() {
			return bigger;
		}
	}

	private static Object[] roundByteSize(double size) {
		return roundByteSize(size, Bytes.BYTE);
	}

	private static Object[] roundByteSize(double size, Bytes sizeType) {
		while (size > 1023 && sizeType.getBigger() != null) {
			size = size / 1024;
			sizeType = sizeType.getBigger();
		}
		return new Object[] { size, sizeType };
	}

	private static ExternalConsoleCommand ls = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "ls";
		}

		@Override
		public String getDescription() {
			return "List directory";
		}

		private void printDirectoryContents(Path dir) {
			ExternalConsole.println("Contains:");
			for (File sub : dir.toFile().listFiles()) {
				Object[] result = roundByteSize(sub.length());
				double size = (double) result[0];
				String sizeType = ((Bytes) result[1]).toString();
				ExternalConsole.println(String.format("\t%s (%s)%s", sub.getName(),
						sub.isFile() ? "File" : sub.isDirectory() ? "Directory" : "Unknown",
						sub.isFile() ? String.format(" %.2f %s", size, sizeType) : ""));
			}
		}

		@Override
		public int executeCommand(String... args) {
			try {
				switch (args.length) {
				case 0:
					ExternalConsole.println(
							String.format("Current Directory:\n\t%s", currentDir.normalize().toAbsolutePath()));
					printDirectoryContents(currentDir);
					break;
				default:
					File path = currentDir.resolve(Paths.get(args[0])).toFile();
					if (!path.exists())
						ExternalConsole.println("No such file or directory");
					else if (path.isFile()) {
						Object[] result = roundByteSize(path.length());
						double size = (double) result[0];
						String sizeType = ((Bytes) result[1]).toString();
						ExternalConsole.println(String.format("%s (File)\n\tHidden: %s\n\tSize: %.2f %s",
								path.getName(), path.isHidden(), size, sizeType));
					} else if (path.isDirectory()) {
						ExternalConsole
								.println(String.format("Directory:\n\t%s", path.toPath().normalize().toAbsolutePath()));
						printDirectoryContents(path.toPath());
					}
					break;
				}
			} catch (Exception e) {
				ExternalConsole.println(e);
				return 1;
			}
			return 0;
		}

		@Override
		public String[] getParamOptions(int number, String[] currArgs) {
			switch (number) {
			case 0:
				return currentDir.toFile().list();
			default:
				return null;
			}
		}
	};

	private static ExternalConsoleCommand tree = new ExternalConsoleCommand() {

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
			for (int i = 0; i < ((File[]) linkedList[ARRAY]).length; i++) {
				File sub = ((File[]) linkedList[ARRAY])[i];
				boolean hasNext = i + 1 < ((File[]) linkedList[ARRAY]).length;
				ExternalConsole.println(String.format("%s%s%s", linkedList[PRE_STRING],
						hasNext ? ENTRY_NEXT : ENTRY_NO_NEXT, sub.getName()));
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
						linkedList = (Object[]) linkedList[ROOT_ARRAY];
						if (linkedList == null)
							return;
						i = (int) linkedList[ARRAY_INDEX];
						hasNext = i + 1 < ((File[]) linkedList[ARRAY]).length;
					} while (!hasNext);
				}
			}
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
					printDirectoryTree(currentDir, cmd.hasOption('f'), cmd.getOptionValues('f'));
					break;
				default:
					File path = currentDir.resolve(Paths.get(cmd.getArgs()[0])).toFile();
					if (!path.exists() || !path.isDirectory())
						ExternalConsole.println("Invalid path");
					else
						printDirectoryTree(path.toPath(), cmd.hasOption('f'), cmd.getOptionValues('f'));
					break;
				}
			} catch (Exception e) {
				ExternalConsole.println(e);
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
	};

	private static ExternalConsoleCommand cd = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "cd";
		}

		@Override
		public String getDescription() {
			return "Change directory";
		}

		@Override
		public int executeCommand(String... args) {
			try {
				switch (args.length) {
				default:
					Path newCurrentDir = currentDir.resolve(Paths.get(args[0]));
					File test = newCurrentDir.toFile();
					if (test == null || !test.isDirectory()) {
						ExternalConsole.println(
								String.format("The path %s is not valid.", newCurrentDir.normalize().toAbsolutePath()));
						break;
					}
					currentDir = newCurrentDir.toRealPath();
				case 0:
					ExternalConsole.println(
							String.format("Current Directory:\n\t%s", currentDir.normalize().toAbsolutePath()));
					break;
				}
			} catch (Exception e) {
				ExternalConsole.println(e);
				return 1;
			}
			return 0;
		}

		@Override
		public String[] getParamOptions(int number, String[] currArgs) {
			switch (number) {
			case 0:
				return currentDir.toFile().list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return new File(dir, name).isDirectory();
					}
				});
			default:
				return null;
			}
		}
	};

	private static ExternalConsoleCommand mkdir = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "mkdir";
		}

		@Override
		public String getDescription() {
			return "Creates a directory";
		}

		@Override
		public int executeCommand(String... args) {
			try {
				switch (args.length) {
				default:
					Path newCurrentDir = currentDir.resolve(Paths.get(args[0]));
					File test = newCurrentDir.toFile();
					if (test.exists()) {
						ExternalConsole.println(
								String.format("The path %s is not valid.", newCurrentDir.normalize().toAbsolutePath()));
						break;
					}
					boolean result = test.mkdirs();
					ExternalConsole.println(String.format("The path %s was %ssuccessful",
							newCurrentDir.normalize().toAbsolutePath(), result ? "" : "un"));
					break;
				case 0:
					ExternalConsole.println("The syntax of the command is incorrect");
					break;
				}
			} catch (Exception e) {
				ExternalConsole.println(e);
				return 1;
			}
			return 0;
		}

	};

	public static ExternalConsoleCommand[] getCommands() {
		return new ExternalConsoleCommand[] { ls, cd, tree, mkdir };
	}

	public static Path getFileCommandsCurrentPath() {
		return currentDir;
	}

}
