package pt.theninjask.externalconsole.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import pt.theninjask.externalconsole.additionalCommands.FileCommands;
import pt.theninjask.externalconsole.event.AfterCommandExecutionExternalConsole;
import pt.theninjask.externalconsole.event.Event;
import pt.theninjask.externalconsole.event.ExternalConsoleClosingEvent;
import pt.theninjask.externalconsole.event.InputCommandExternalConsoleEvent;
import pt.theninjask.externalconsole.event.SetClosableEvent;
import pt.theninjask.externalconsole.event.SetViewableEvent;
import pt.theninjask.externalconsole.stream.RedirectorErrorOutputStream;
import pt.theninjask.externalconsole.stream.RedirectorInputStream;
import pt.theninjask.externalconsole.stream.RedirectorOutputStream;
import pt.theninjask.externalconsole.util.ColorTheme;
import pt.theninjask.externalconsole.util.KeyPressedAdapter;
import pt.theninjask.externalconsole.util.WrapEditorKit;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.Handler;

public class ExternalConsole extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final ColorTheme TWITCH_THEME = new ColorTheme("Twitch", new Color(0xe5e5e5),
			new Color(123, 50, 250));

	public static final ColorTheme NIGHT_THEME = new ColorTheme("Night", Color.WHITE, Color.BLACK);

	public static final ColorTheme DAY_THEME = new ColorTheme("Day", Color.BLACK, Color.WHITE);

	private JScrollPane scroll;
	private JTextPane console;
	private ExternalConsoleOutputStream out;
	private ExternalConsoleErrorOutputStream err;
	private ExternalConsoleInputStream in;

	private JPanel messagePanel;

	private JTextField input;

	private boolean autoScroll;

	private Map<String, ExternalConsoleCommand> cmds;

	private ColorTheme currentTheme;

	private static ExternalConsoleCommand help = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "help";
		}

		@Override
		public String getDescription() {
			return "Shows all commands and their descriptions";
		}

		@Override
		public int executeCommand(String... args) {
			println("Available Commands:");
			List<ExternalConsoleCommand> helpSorted = singleton.cmds.values().stream()
					.sorted(ExternalConsoleCommand.comparator).toList();
			for (ExternalConsoleCommand cmd : helpSorted) {
				// int spacing = 4 + cmd.getCommand().length();
				println(String.format("\t%s - %s", cmd.getCommand(),
						// cmd.getDescription().replaceAll("\n", "\n\t" + " ".repeat(spacing))));
						cmd.getDescription().replaceAll("\n", "\n\t\t")));
			}
			return 0;
		}

		@Override
		public boolean accessibleInCode() {
			return true;
		}
	};

	private static ExternalConsoleCommand autoscroll = new ExternalConsoleCommand() {

		private Map<Option, Runnable> optionsMap = Map
				.ofEntries(Map.entry(new Option("a", "auto", false, "Sets Scroll to Auto"), () -> {
					ExternalConsole.singleton.autoScroll = true;
				}), Map.entry(new Option("m", "manual", false, "Sets Scroll to Manual"), () -> {
					ExternalConsole.singleton.autoScroll = false;
				}));

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
					println(String.format("autoscroll is set as: %s", singleton.autoScroll));
					new HelpFormatter().printHelp(new PrintWriter(singleton.out, true), HelpFormatter.DEFAULT_WIDTH,
							"autoscroll", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
							null, true);
				}

			} catch (ParseException e) {
				println(e.getMessage());
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
	};

	private static ExternalConsoleCommand clear = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "cls";
		}

		@Override
		public String getDescription() {
			return "Clears ExternalConsole";
		}

		@Override
		public int executeCommand(String... args) {
			singleton.console.setText("");
			return 0;
		}

		@Override
		public boolean accessibleInCode() {
			return true;
		}
	};

	private static ExternalConsoleCommand hide = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "hide";
		}

		@Override
		public String getDescription() {
			return "Hides External Console";
		}

		@Override
		public int executeCommand(String... args) {
			singleton.setExtendedState(JFrame.ICONIFIED);
			return 0;
		}

		@Override
		public boolean accessibleInCode() {
			return true;
		}
	};

	private static ExternalConsoleCommand top = new ExternalConsoleCommand() {

		private Map<Option, Runnable> optionsMap = Map
				.ofEntries(Map.entry(new Option("t", "true", false, "AlwaysOnTop Set to True"), () -> {
					ExternalConsole.singleton.setAlwaysOnTop(true);
				}), Map.entry(new Option("f", "false", false, "AlwaysOnTop Set to False"), () -> {
					ExternalConsole.singleton.setAlwaysOnTop(false);
				}));

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
					println(String.format("AlwaysOnTop is set as: %s", singleton.isAlwaysOnTop()));
					new HelpFormatter().printHelp(new PrintWriter(singleton.out, true), HelpFormatter.DEFAULT_WIDTH,
							"top", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null,
							true);
				}
			} catch (ParseException e) {
				println(e.getMessage());
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
	};

	private static ExternalConsoleCommand theme = new ExternalConsoleCommand() {

		// cheese to allow to pass args
		private CommandLine cmd;

		private Map<Option, Runnable> optionsMap;

		// To allow to add more themes from outside
		private Map<String, ColorTheme> themes = new HashMap<>(
				Map.ofEntries(Map.entry(TWITCH_THEME.getName(), TWITCH_THEME),
						Map.entry(DAY_THEME.getName(), DAY_THEME), Map.entry(NIGHT_THEME.getName(), NIGHT_THEME)));
		{
			Option set = Option.builder("s").longOpt("set").desc("Set a theme to External Console").numberOfArgs(1)
					.build();
			Option add = Option.builder("a").longOpt("add").desc("Add a theme to External Console").numberOfArgs(7)
					.build();
			optionsMap = Map.ofEntries(Map.entry(set, () -> {
				ColorTheme theme = themes.get(cmd.getOptionValue(set.getOpt()));
				if (theme != null)
					setTheme(theme);
				else
					println(String.format("Theme %s does not exist", set.getValue()));
			}), Map.entry(add, () -> {
				try {
					String name = cmd.getOptionValues(add.getOpt())[0];
					if (themes.containsKey(name)) {
						println(String.format("Theme with name %s already exists!", name));
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

					println(String.format("Theme %s added", name));
				} catch (Exception e) {
					println(e.getClass().getSimpleName());
					println(e.getMessage());
				}
			}));
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
					if (singleton.currentTheme != null)
						current = singleton.currentTheme.getName();
					println(String.format("Theme is set as: %s", current));
					new HelpFormatter().printHelp(new PrintWriter(singleton.out, true), HelpFormatter.DEFAULT_WIDTH,
							"theme", null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD,
							null, true);
				}
			} catch (ParseException e) {
				println(e.getMessage());
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
					return new String[] { "0", "127", "255" };
				case 3:
					return new String[] { "0", "127", "255" };
				case 4:
					return new String[] { "0", "127", "255" };
				case 5:
					return new String[] { "0", "127", "255" };
				case 6:
					return new String[] { "0", "127", "255" };
				case 7:
					return new String[] { "0", "127", "255" };
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
	};

	private static ExternalConsoleCommand forceStop = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "forceStop";
		}

		@Override
		public String getDescription() {
			return "It terminates the running JVM";
		}

		@Override
		public int executeCommand(String... args) {
			singleton.dispose();
			System.exit(0);
			return 0;
		}
	};

	/*private static ExternalConsoleCommand tinker = new ExternalConsoleCommand() {

		@Override
		public String getCommand() {
			return "tinker";
		}

		@Override
		public String getDescription() {
			return "For test purposes";
		}

		@Override
		public int executeCommand(String... args) {
			try {
				Thread.sleep(Integer.valueOf(args.length > 0? args[0]: "10") * 1000);
				System.out.println("Done");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return 0;
		}
	};*/

	private static class UsedCommand {

		private UsedCommand previous;

		private UsedCommand next;

		private String fullCommand;

		private static final UsedCommand NULL_UC = new UsedCommand();

		private UsedCommand() {
			this.fullCommand = null;
			this.previous = this;
			this.next = this;
		}

		public UsedCommand(String fullCommand, UsedCommand previous, UsedCommand next) {
			this.fullCommand = fullCommand;
			this.previous = previous;
			this.next = next;
		}

		public String getFullCommand() {
			return fullCommand;
		}

		public UsedCommand getPrevious() {
			return previous;
		}

		/*
		 * public void setPrevious(UsedCommand previous) { this.previous = previous; }
		 */

		public UsedCommand getNext() {
			return next;
		}

		public void setNext(UsedCommand next) {
			this.next = next;
		}
	}

	private UsedCommand last;

	private static ExternalConsole singleton = new ExternalConsole();

	private ExternalConsole() {
		this.setTitle("External Console");

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyPressedAdapter());
		RedirectorOutputStream.changeRedirect(System.out);
		RedirectorOutputStream.changeDefault(System.out);
		System.setOut(RedirectorOutputStream.getInstance());

		RedirectorErrorOutputStream.changeRedirect(System.err);
		RedirectorErrorOutputStream.changeDefault(System.err);
		System.setErr(RedirectorErrorOutputStream.getInstance());

		RedirectorInputStream.changeRedirect(System.in);
		RedirectorInputStream.changeDefault(System.in);
		System.setIn(RedirectorInputStream.getInstance());

		this.currentTheme = NIGHT_THEME;
		this.setMinimumSize(new Dimension(300, 300));
		// ImageIcon icon = new ImageIcon(Constants.ICON_PATH);
		// this.setIconImage(icon.getImage());
		this.setLayout(new BorderLayout());
		this.add(insertConsole(), BorderLayout.CENTER);
		this.add(inputConsole(), BorderLayout.SOUTH);
		scroll.getParent().setBackground(null);
		this.out = new ExternalConsoleOutputStream();
		this.err = new ExternalConsoleErrorOutputStream();
		this.in = new ExternalConsoleInputStream();

		// this.setAlwaysOnTop(true);
		this.autoScroll = true;
		this.cmds = new HashMap<>();

		this.cmds.put(help.getCommand(), help);
		this.cmds.put(autoscroll.getCommand(), autoscroll);
		this.cmds.put(clear.getCommand(), clear);
		this.cmds.put(hide.getCommand(), hide);
		this.cmds.put(top.getCommand(), top);
		this.cmds.put(theme.getCommand(), theme);
		this.cmds.put(forceStop.getCommand(), forceStop);
		for (ExternalConsoleCommand cmd : FileCommands.getCommands()) {
			this.cmds.put(cmd.getCommand(), cmd);
		}

		//this.cmds.put(tinker.getCommand(), tinker);

		this.last = UsedCommand.NULL_UC;

		this.console.setForeground(currentTheme.getFont());
		this.setBackground(currentTheme.getBackground());

		this.input.setBorder(BorderFactory.createLineBorder(currentTheme.getFont(), 1));
		this.input.setForeground(currentTheme.getFont());
		this.input.setCaretColor(currentTheme.getFont());
		this.input.setBackground(currentTheme.getBackground());

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				EventManager.triggerEvent(new ExternalConsoleClosingEvent(isClosable()));
			}
		});

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		EventManager.registerEventListener(this);
	}

	/*
	 * public static ExternalConsole getInstance() { return singleton; }
	 */

	public static boolean isViewable() {
		return singleton.isVisible();
	}

	public static void setViewable(boolean b) {
		SetViewableEvent event = new SetViewableEvent(b);
		EventManager.triggerEvent(event);
		if (event.isCancelled())
			return;
		singleton.setVisible(b);
	}

	public static boolean isClosable() {
		return singleton.getDefaultCloseOperation() >= JFrame.DISPOSE_ON_CLOSE;
	}

	public static void setClosable(boolean b) {
		SetClosableEvent event = new SetClosableEvent(b);
		EventManager.triggerEvent(event);
		if (event.isCancelled())
			return;
		if (b)
			singleton.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		else
			singleton.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public static void addCommand(ExternalConsoleCommand newCmd) {
		singleton.cmds.put(newCmd.getCommand(), newCmd);
	}

	public static void removeCommand(String cmd) {
		singleton.cmds.remove(cmd);
	}

	public static ExternalConsoleCommand getCommand(String cmd) {
		ExternalConsoleCommand ecmd = singleton.cmds.get(cmd);
		return ecmd.accessibleInCode() ? ecmd : null;
	}

	public static List<ExternalConsoleCommand> getAllCommands() {
		return singleton.cmds.values().parallelStream().filter(p -> p.accessibleInCode()).toList();
	}

	public static List<String> getAllCommandsAsString() {
		return getAllCommandsAsString(true);
	}

	public static List<String> getAllCommandsAsString(boolean onlyAccessibleInCode) {
		return singleton.cmds.values().parallelStream().filter(p -> p.accessibleInCode() || !onlyAccessibleInCode)
				.map(p -> p.getCommand()).toList();
	}

	public static boolean executeCommand(String cmd, String... args) {
		ExternalConsoleCommand ecmd = singleton.cmds.get(cmd);
		if (ecmd == null || !ecmd.accessibleInCode())
			return false;
		int result = ecmd.executeCommand(args);
		EventManager.triggerEvent(new AfterCommandExecutionExternalConsole(ecmd, args, result));
		return true;
	}

	private JScrollPane insertConsole() {
		scroll = new JScrollPane();
		console = new JTextPane();
		console.setEditorKit(new WrapEditorKit());
		// console.setBorder(null);
		// console.setTabSize(4);
		console.setEditable(false);
		// console.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret) console.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scroll.setViewportView(console);
		scroll.setFocusable(false);
		scroll.setEnabled(false);
		scroll.setBorder(null);
		scroll.setWheelScrollingEnabled(true);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		console.setOpaque(false);
		// console.setForeground(Color.BLACK);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
			}
		});
		return scroll;
	}

	private JPanel inputConsole() {
		messagePanel = new JPanel(new BorderLayout());
		input = new JTextField();
		input.setBorder(null);

		// input.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		// input.setForeground(Color.BLACK);
		// input.setCaretColor(Color.BLACK);
		// input.setBackground(Color.WHITE);
		input.setFocusTraversalKeysEnabled(false);
		input.addKeyListener(new KeyListener() {

			private int tabPos = -1;

			private String ref = null;

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() != KeyEvent.VK_TAB && e.getKeyCode() != KeyEvent.VK_SHIFT) {
					tabPos = -1;
					ref = null;
				}
				switch (e.getKeyCode()) {
				default:
					break;
				case KeyEvent.VK_TAB:
					String[] args = inputToArgs(input.getText(), true);
					ExternalConsoleCommand cmd = cmds.get(args[0]);
					boolean next = !KeyPressedAdapter.isKeyPressed(KeyEvent.VK_SHIFT);
					if (ref == null) {
						ref = args[args.length - 1];
					}
					String[] currArgs;
					if (args.length > 2)
						currArgs = Arrays.copyOfRange(args, 1, args.length - 1);
					else
						currArgs = new String[] {};
					if (args.length <= 1 || cmd == null || cmd.getParamOptions(args.length - 2, currArgs) == null) {
						List<ExternalConsoleCommand> options = cmds.values().stream()
								.filter(c -> c.getCommand().startsWith(ref)).sorted(ExternalConsoleCommand.comparator)
								.toList();
						tabPos = next ? tabPos + 1 >= options.size() ? 0 : tabPos + 1
								: tabPos - 1 < 0 ? options.size() - 1 : tabPos - 1;
						if (tabPos < 0 || tabPos >= options.size())
							break;
						args[args.length - 1] = options.get(tabPos).getCommand();
						input.setText(argsToInput(args));
					} else {
						String[] paramOptions = cmd.getParamOptions(args.length - 2, currArgs);
						List<String> options = Stream.of(paramOptions).filter(c -> c.startsWith(ref)).toList();
						tabPos = next ? tabPos + 1 >= options.size() ? 0 : tabPos + 1
								: tabPos - 1 < 0 ? options.size() - 1 : tabPos - 1;
						if (tabPos < 0 || tabPos >= options.size())
							break;
						args[args.length - 1] = options.get(tabPos);
						input.setText(argsToInput(args));
					}
					break;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_KP_UP:
					if (last != UsedCommand.NULL_UC) {
						if (last.getPrevious() != UsedCommand.NULL_UC) {
							input.setText(last.getFullCommand());
							last = last.getPrevious();
						}
					} else {
						input.setText("");
					}
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
					if (last != UsedCommand.NULL_UC && last.getNext() != UsedCommand.NULL_UC
							&& last.getNext().getNext() != UsedCommand.NULL_UC) {
						last = last.getNext();
						input.setText(last.getNext().getFullCommand());
					}
					break;
				case KeyEvent.VK_ENTER:
					InputCommandExternalConsoleEvent event = new InputCommandExternalConsoleEvent(
							inputToArgs(input.getText()));
					while (last.getNext() != UsedCommand.NULL_UC)
						last = last.getNext();
					if (last == UsedCommand.NULL_UC)
						last = new UsedCommand(null, UsedCommand.NULL_UC, UsedCommand.NULL_UC);
					UsedCommand current = new UsedCommand(input.getText(), last, UsedCommand.NULL_UC);
					last.setNext(current);
					last = current;

					// console.append(input.getText());
					// console.append("\n");
					println(input.getText());

					scroll.repaint();
					scroll.revalidate();
					in.contents = input.getText().getBytes();
					in.pointer = 0;
					input.setText("");
					// event.finishedEvent();
					EventManager.triggerEvent(event);
					break;
				}
			}
		});
		messagePanel.add(input, BorderLayout.CENTER);
		return messagePanel;

	}

	@SuppressWarnings("unused")
	private String[] inputToArgsOld(String input) {
		return inputToArgsOld(input, false);
	}

	private String[] inputToArgsOld(String input, boolean emptyLast) {
		if (emptyLast)
			return input.split(" ", -1);
		else
			return input.split(" ");
	}

	@SuppressWarnings("unused")
	private String argsToInputOld(String[] args) {
		return String.join(" ", args);
	}

	private String[] inputToArgs(String input) {
		return inputToArgs(input, false);
	}

	/*
	 * The code is written this way with a challenge to not use an import
	 */
	private String[] inputToArgs(String input, boolean emptyLast) {
		if (emptyLast)
			input = input.stripLeading();
		else
			input = input.strip();
		boolean quote = false;
		int argSize = 0;
		String arg = "";
		Object[] link = new Object[2];
		Object[] start = link;
		if (input.isEmpty() && emptyLast) {
			link[0] = arg;
			argSize++;
		}
		for (int i = 0; i < input.length(); i++) {
			char chary = input.charAt(i);
			if (chary == '\"') {
				quote = !quote;
				if (i + 1 == input.length()) {
					link[0] = arg;
					argSize++;
				}
			} else if (chary == ' ' && !quote) {
				Object[] tmp = new Object[2];
				link[0] = arg;
				link[1] = tmp;
				link = tmp;
				argSize++;
				arg = "";
				if (i + 1 == input.length()) {
					link[0] = arg;
					argSize++;
				}
			} else if (i + 1 == input.length()) {
				link[0] = arg + chary;
				argSize++;
			} else {
				arg += chary;
			}
		}
		String[] args = new String[argSize];
		link = start;
		for (int i = 0; i < argSize; i++) {
			args[i] = (String) link[0];
			link = (Object[]) link[1];
		}
		return args;
	}

	/*
	 * The code is written this way with a challenge to not use an import
	 */
	private String argsToInput(String[] args) {
		String input = "";
		for (String string : args) {
			if (string.indexOf(' ') != -1) {
				string = String.format("\"%s\"", string);
			}
			input += string + ' ';
		}
		return input.stripTrailing();
	}

	/*
	 * public static OutputStream getExternalConsoleOutputStream() { return
	 * singleton.out; }
	 * 
	 * public static OutputStream getExternalConsoleErrorOutputStream() { return
	 * singleton.err; }
	 * 
	 * public static InputStream getExternalConsoleInputStream() { return
	 * singleton.in; }
	 */

	private class ExternalConsoleOutputStream extends OutputStream {

		@Override
		public void write(int b) throws IOException {
			try {
				StyledDocument doc = singleton.console.getStyledDocument();
				doc.insertString(doc.getLength(), Character.toString(b), null);
				if (autoScroll)
					console.setCaretPosition(doc.getLength());
				singleton.scroll.repaint();
				singleton.scroll.revalidate();
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// continue
			}
			/*
			 * console.append(Character.toString(b)); if (autoScroll) try {
			 * console.setCaretPosition(console.getLineStartOffset(console.getLineCount() -
			 * 1)); } catch (BadLocationException e) { } scroll.repaint();
			 * scroll.revalidate();
			 */
		}

	}

	private class ExternalConsoleErrorOutputStream extends OutputStream {

		private SimpleAttributeSet errorSet;

		public ExternalConsoleErrorOutputStream() {
			errorSet = new SimpleAttributeSet();
			StyleConstants.setForeground(errorSet, Color.RED);
		}

		@Override
		public void write(int b) throws IOException {
			try {
				StyledDocument doc = singleton.console.getStyledDocument();
				doc.insertString(doc.getLength(), Character.toString(b), errorSet);
				if (autoScroll)
					console.setCaretPosition(doc.getLength());
				singleton.scroll.repaint();
				singleton.scroll.revalidate();
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// continue
			}
			/*
			 * console.append(Character.toString(b)); if (autoScroll) try {
			 * console.setCaretPosition(console.getLineStartOffset(console.getLineCount() -
			 * 1)); } catch (BadLocationException e) { } scroll.repaint();
			 * scroll.revalidate();
			 */
		}

	}

	public static void setConsoleTitle(String title) {
		singleton.setTitle(title);
	}

	public static void setIcon(URL iconPath) {
		ImageIcon icon = new ImageIcon(iconPath);
		singleton.setIconImage(icon.getImage());
	}

	public static void setTheme(ColorTheme theme) {
		singleton.currentTheme = theme;
		singleton.console.setForeground(theme.getFont());
		singleton.setBackground(theme.getBackground());

		singleton.input.setBorder(BorderFactory.createLineBorder(theme.getFont(), 1));
		singleton.input.setForeground(theme.getFont());
		singleton.input.setCaretColor(theme.getFont());
		singleton.input.setBackground(theme.getBackground());
	}

	public static void println() {
		try {
			StyledDocument doc = singleton.console.getStyledDocument();
			doc.insertString(doc.getLength(), "\n", null);
			if (singleton.autoScroll)
				singleton.console.setCaretPosition(doc.getLength());
			// singleton.console.append("\n");
			singleton.scroll.repaint();
			singleton.scroll.revalidate();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public static void println(Object msg) {
		println(msg.toString());
	}

	public static void println(String msg) {
		try {
			StyledDocument doc = singleton.console.getStyledDocument();
			doc.insertString(doc.getLength(), String.format("%s\n", msg), null);
			if (singleton.autoScroll)
				singleton.console.setCaretPosition(doc.getLength());
			// singleton.console.append(String.format("%s\n", msg));
			singleton.scroll.repaint();
			singleton.scroll.revalidate();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public static ColorTheme getTheme() {
		return singleton.currentTheme;
	}

	public static void setSystemStreams() {
		RedirectorOutputStream.changeRedirect(singleton.out);
		RedirectorErrorOutputStream.changeRedirect(singleton.err);
		RedirectorInputStream.changeRedirect(singleton.in);
	}

	public static void revertSystemStreams() {
		RedirectorOutputStream.changeRedirectToDefault();
		RedirectorErrorOutputStream.changeRedirectToDefault();
		RedirectorInputStream.changeRedirectToDefault();
	}

	public static void enableEventManagerLogging(boolean val) {
		EventManager.enableLogging(val);
	}

	public static boolean isEnableEventManagerLogging() {
		return EventManager.isEnableLogging();
	}

	public static void registerEventListener(Object listener) {
		EventManager.registerEventListener(listener);
		;
	}

	public static void unregisterEventListener(Object listener) {
		EventManager.unregisterEventListener(listener);
	}

	// Cannot think of an use case and will be in conflict with OnCommand()
	/*
	 * public static void enableInput(boolean enable) { if(!enable)
	 * singleton.input.setText(""); singleton.input.setEnabled(enable); }
	 */

	/*
	 * I dont know if this even has a point
	 *
	 */
	private class ExternalConsoleInputStream extends InputStream {

		private byte[] contents;
		private int pointer = 0;

		@Override
		public int read() throws IOException {
			if (pointer >= contents.length)
				return -1;
			return this.contents[pointer++];
		}

	}

	private static final String[] loading1 = { "|", "/", "-", "\\" };
	private static final String[] loading2 = { ".", "..", "...", "....", ".....", "....", "...", ".." };
	private static final String[] loading3 = { "|.         |", "| .        |", "|  .       |", "|   .      |", "|    .     |",
			"|     .    |", "|      .   |", "|       .  |", "|        . |", "|         .|", "|        . |",
			"|       .  |", "|      .   |", "|     .    |", "|    .     |", "|   .      |", "|  .       |",
			"| .        |" };
	//private static String[] loading4 = {"^",">","v","<"};
	private static final String[][] loadings = {loading1, loading2, loading3 };

	@Handler
	public void onCommand(InputCommandExternalConsoleEvent event) {
		// To not lock weirdly the ExternalConsole
		new Thread(() -> {
			try {
				input.setEditable(false);
				Thread proc = new Thread(() -> {
					if (event.getArgs() == null || event.getArgs().length == 0 || event.isCancelled())
						return;
					// event.addAfterEvent(() -> {
					String[] args = Arrays.copyOfRange(event.getArgs(), 1, event.getArgs().length);
					ExternalConsoleCommand cmd = cmds.get(event.getArgs()[0]);
					if (cmd != null) {
						int result = cmd.executeCommand(args);
						EventManager.triggerEvent(new AfterCommandExecutionExternalConsole(cmd, args, result));
					}
				});
				proc.start();
				proc.join(5 * 1000);
				int i = 0;
				String[] loading = loadings[ThreadLocalRandom.current().nextInt(loadings.length)];
				while (proc.isAlive()) {
					input.setText(String.format("Processing %s", loading[i]));
					proc.join(100);
					i = ++i == loading.length ? 0 : i;
				}
				input.setText("");
				input.setEditable(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
				input.setEnabled(true);
			}
			// });
		}).start();
	}

	private static class EventManager {

		private static final SimpleFormatter LOGGER_FORMATTER = new SimpleFormatter() {
			private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

			@Override
			public synchronized String format(LogRecord lr) {
				return String.format("[%s] %s\n", sdf.format(new Date(lr.getMillis())), lr.getMessage());
			}
		};

		private static Logger logger = setUpLogger();

		private static EventManager singleton = new EventManager();

		private MBassador<Object> dispatcher;

		private EventManager() {
			IBusConfiguration config = new BusConfiguration()
					.addPublicationErrorHandler(new IPublicationErrorHandler() {
						@Override
						public void handleError(PublicationError error) {
						}
					}).addFeature(Feature.SyncPubSub.Default())
					.addFeature(Feature.AsynchronousHandlerInvocation.Default())
					.addFeature(Feature.AsynchronousMessageDispatch.Default());
			this.dispatcher = new MBassador<Object>(config);
		}

		private static final Logger setUpLogger() {
			Logger logger = Logger.getLogger(EventManager.class.getName());
			logger.setUseParentHandlers(false);
			StreamHandler handler = new StreamHandler(System.out, LOGGER_FORMATTER) {
				@Override
				public synchronized void publish(LogRecord record) {
					super.publish(record);
					super.flush();
				}
			};
			logger.addHandler(handler);
			logger.setLevel(Level.OFF);
			return logger;
		}

		public static void enableLogging(boolean val) {
			if (val) {
				logger.setLevel(Level.ALL);
			} else {
				logger.setLevel(Level.OFF);
			}
		}

		public static boolean isEnableLogging() {
			if (logger.getLevel().equals(Level.ALL))
				return true;
			else
				return false;
		}

		public static void registerEventListener(Object listener) {
			singleton.dispatcher.subscribe(listener);
		}

		public static void unregisterEventListener(Object listener) {
			singleton.dispatcher.unsubscribe(listener);
		}

		public static void triggerEvent(Event event) {
			logger.info(String.format("Event %s triggered", event.getName()));
			singleton.dispatcher.post(event).now();
		}
	}

	public static void main(String[] args) {
		setSystemStreams();
		registerEventListener(new Object() {
			@Handler
			public void onClose(ExternalConsoleClosingEvent event) {
				System.exit(0);
			}
		});
		setViewable(true);
	}

}
