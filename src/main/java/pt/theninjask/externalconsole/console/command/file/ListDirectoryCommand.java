package pt.theninjask.externalconsole.console.command.file;

import static pt.theninjask.externalconsole.console.command.file.ChangeDirectoryCommand.getCurrentDir;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.util.Bytes;

public class ListDirectoryCommand implements ExternalConsoleCommand {
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
			Object[] result = Bytes.roundByteSize(sub.length());
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
						String.format("Current Directory:\n\t%s", getCurrentDir().normalize().toAbsolutePath()));
				printDirectoryContents(getCurrentDir());
				break;
			default:
				File path = getCurrentDir().resolve(Paths.get(args[0])).toFile();
				if (!path.exists())
					ExternalConsole.println("No such file or directory");
				else if (path.isFile()) {
					Object[] result = Bytes.roundByteSize(path.length());
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
			return getCurrentDir().toFile().list();
		default:
			return null;
		}
	}

}
