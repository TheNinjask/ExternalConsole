package pt.theninjask.externalconsole.console.command;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import javax.swing.text.StyledDocument;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.util.LoadingProcess;

public class LoadAnimCommand implements ExternalConsoleCommand {

	private ExternalConsole console;
	
	public LoadAnimCommand(ExternalConsole console) {
		this.console = console;
	}
	
	@Override
	public String getCommand() {
		return "load_anim";
	}

	@Override
	public String getDescription() {
		return "Shows an animation for a certain time (default: 5s)";
	}

	@Override
	public int executeCommand(String... args) {
		int animation = -1;
		switch (args.length) {
		default:
		case 1:
			if (args.length > 0)
				try {
					animation = Integer.valueOf(args[0]);
				} catch (NumberFormatException e) {
				}
		case 0:
			break;
		}
		try {
			Thread proc = new Thread(() -> {
				try {
					int animTime = 5;
					if (args.length > 1)
						try {
							animTime = Integer.valueOf(args[1]);
						} catch (NumberFormatException e) {
						}
					Thread.sleep(animTime * 1000);
				} catch (InterruptedException e) {
				}
			});
			proc.start();
			animation = animation < 0 || animation >= console._getLoadings().length
					? ThreadLocalRandom.current().nextInt(console._getLoadings().length)
					: animation;
			Object[] loading = console._getLoadings()[animation];
			int i = 3;
			StyledDocument doc = console._getConsole().getStyledDocument();
			int offset = doc.getLength();
			while (proc.isAlive()) {
				String msg = String.format("Processing %s", loading[i]);
				doc.insertString(offset, msg, null);
				proc.join((int) loading[1]);
				i = ((LoadingProcess) loading[0]).nextLoading(i, loading);
				doc.remove(offset, msg.length());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	public String[] getParamOptions(int number, String[] currArgs) {
		switch (number) {
		case 1:
			return IntStream.range(0, 15).mapToObj(i -> {
				return Integer.toString(i);
			}).toArray(String[]::new);
		case 0:
			return IntStream.range(0, console._getLoadings().length).mapToObj(i -> {
				return Integer.toString(i);
			}).toArray(String[]::new);
		default:
			return null;
		}
	}

	@Override
	public boolean accessibleInCode() {
		return true;
	}

}
