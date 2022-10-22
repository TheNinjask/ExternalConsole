package pt.theninjask.externalconsole.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
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

import org.reflections.Reflections;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.Handler;
import pt.theninjask.externalconsole.console.command.LoadingExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.TimerCommand;
import pt.theninjask.externalconsole.console.component.ScreenConsole;
import pt.theninjask.externalconsole.console.util.LoadingProcess;
import pt.theninjask.externalconsole.console.util.UsedCommand;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleErrorOutputStream;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleInputStream;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleOutputStream;
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

public class ExternalConsole extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final ColorTheme TWITCH_THEME = new ColorTheme("Twitch", new Color(0xe5e5e5),
			new Color(123, 50, 250));

	public static final ColorTheme NIGHT_THEME = new ColorTheme("Night", Color.WHITE, Color.BLACK);

	public static final ColorTheme DAY_THEME = new ColorTheme("Day", Color.BLACK, Color.WHITE);

	private ExternalConsoleOutputStream out;
	private ExternalConsoleErrorOutputStream err;
	private ExternalConsoleInputStream in;

	private JPanel messagePanel;

	private JTextField input;

	private Map<String, ExternalConsoleCommand> cmds;

	private ColorTheme currentTheme;

	private UsedCommand last;

	private ScreenConsole screenConsole;

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
		this.screenConsole = new ScreenConsole();
		this.add(screenConsole, BorderLayout.CENTER);
		this.add(inputConsole(), BorderLayout.SOUTH);
		screenConsole.getParent().setBackground(null);
		this.out = new ExternalConsoleOutputStream(this);
		this.err = new ExternalConsoleErrorOutputStream(this);
		this.in = new ExternalConsoleInputStream();

		// this.setAlwaysOnTop(true);
		this.cmds = new HashMap<>();

		Reflections reflections = new Reflections("pt.theninjask.externalconsole.console.command");
		List<Class<? extends ExternalConsoleCommand>> cmds = reflections.getSubTypesOf(ExternalConsoleCommand.class)
				.stream().toList();
		List<LoadingExternalConsoleCommand> op = Arrays.asList((clazz) -> {
			try {
				return clazz.getDeclaredConstructor(ExternalConsole.class).newInstance(this);
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				return null;
			}
		}, (clazz) -> {
			try {
				return clazz.getDeclaredConstructor().newInstance();
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				return null;
			}
		});
		for (Class<? extends ExternalConsoleCommand> cmd : cmds) {
			for (LoadingExternalConsoleCommand loadingExternalConsoleCommand : op) {
				ExternalConsoleCommand load = loadingExternalConsoleCommand.getCommand(cmd);
				if (load != null) {
					_addCommand(load);
					break;
				}
			}
		}

		// this.cmds.put(tinker.getCommand(), tinker);

		this.last = UsedCommand.NULL_UC;

		this.screenConsole.getScreen().setForeground(currentTheme.getFont());
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

	public boolean _getAutoScroll() {
		return screenConsole.getAutoScroll();
	}

	public void _setAutoScroll(boolean autoScroll) {
		screenConsole.setAutoScroll(autoScroll);
	}

	public JTextPane _getScreen() {
		return screenConsole.getScreen();
	}

	public JScrollPane _getScroll() {
		return screenConsole;
	}

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

	public void _addCommand(ExternalConsoleCommand newCmd) {
		cmds.put(newCmd.getCommand(), newCmd);
	}

	public static void addCommand(ExternalConsoleCommand newCmd) {
		singleton._addCommand(newCmd);
	}

	public static void removeCommand(String cmd) {
		singleton.cmds.remove(cmd);
	}

	public static ExternalConsoleCommand getCommand(String cmd) {
		ExternalConsoleCommand ecmd = singleton.cmds.get(cmd);
		return ecmd.accessibleInCode() ? ecmd : null;
	}

	public Map<String, ExternalConsoleCommand> _getAllCommands() {
		return cmds;
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
		if (ecmd == null || !ecmd.accessibleInCode() || ecmd.isProgram())
			return false;
		int result = ecmd.executeCommand(args);
		EventManager.triggerEvent(new AfterCommandExecutionExternalConsole(ecmd, args, result));
		return true;
	}

	public static void removeDemoCmds() {
		for (ExternalConsoleCommand cmd : singleton.cmds.values().stream().filter(c -> c.isDemo()).toList()) {
			singleton.cmds.remove(cmd.getCommand());
		}
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
					ExternalConsoleCommand cmd = isProgramRunning.get() ? program : cmds.get(args[0]);
					boolean next = !KeyPressedAdapter.isKeyPressed(KeyEvent.VK_SHIFT);
					if (ref == null) {
						ref = args[args.length - 1];
					}
					String[] currArgs;
					if (isProgramRunning.get())
						currArgs = Arrays.copyOf(args, args.length);
					else if (args.length > 2)
						currArgs = Arrays.copyOfRange(args, 1, args.length - 1);
					else
						currArgs = new String[] {};
					List<String> options;
					if (isProgramRunning.get() && cmd != null) {
						String[] paramOptions = cmd.getParamOptions(args.length - 1, currArgs);
						options = paramOptions!=null ? Stream.of(paramOptions).filter(c -> c.startsWith(ref)).toList() : Collections.emptyList();
					} else if (args.length <= 1 || cmd == null
							|| cmd.getParamOptions(args.length - 2, currArgs) == null) {
						options = cmds.values().stream().filter(c -> c.getCommand().startsWith(ref))
								.sorted(ExternalConsoleCommand.comparator).map(c -> c.getCommand()).toList();
					} else {
						String[] paramOptions = cmd.getParamOptions(args.length - 2, currArgs);
						options = Stream.of(paramOptions).filter(c -> c.startsWith(ref)).toList();
					}
					tabPos = next ? tabPos + 1 >= options.size() ? 0 : tabPos + 1
							: tabPos - 1 < 0 ? options.size() - 1 : tabPos - 1;
					if (tabPos < 0 || tabPos >= options.size())
						break;
					args[args.length - 1] = options.get(tabPos);
					input.setText(argsToInput(args));
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

					in.insertData((input.getText()+"\n").getBytes());
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
			if (chary == '\"' && (i == 0 || (i > 0 && input.charAt(i - 1) != '\\'))) {
				// if(i>0 && input.charAt(i-1)!='\\')
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
				if (chary == '\"')
					arg = arg.substring(0, arg.length() - 1);
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
			if (string.indexOf('\"') != -1)
				string = string.replaceAll("\\\"", "\\\\\\\"");
			if (string.indexOf(' ') != -1)
				string = String.format("\"%s\"", string);

			input += string + ' ';
		}
		return input.stripTrailing();
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
		singleton.screenConsole.getScreen().setForeground(theme.getFont());
		singleton.setBackground(theme.getBackground());

		singleton.input.setBorder(BorderFactory.createLineBorder(theme.getFont(), 1));
		singleton.input.setForeground(theme.getFont());
		singleton.input.setCaretColor(theme.getFont());
		singleton.input.setBackground(theme.getBackground());
	}

	public static void println() {
		println("");
		/*
		 * try { StyledDocument doc = singleton.console.getStyledDocument();
		 * doc.insertString(doc.getLength(), "\n", null); if (singleton.autoScroll)
		 * singleton.console.setCaretPosition(doc.getLength()); //
		 * singleton.console.append("\n"); singleton.scroll.repaint();
		 * singleton.scroll.revalidate(); } catch (BadLocationException e) {
		 * e.printStackTrace(); }
		 */
	}

	public static void println(Object msg) {
		println(msg.toString());
	}

	public static void println(String msg) {
		singleton.screenConsole.println(msg);
	}

	

	public void _clearExtraLines() throws BadLocationException {
		screenConsole._clearExtraLines();	
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

	public OutputStream _getOutputStream() {
		return out;
	}

	public OutputStream _getErrorOutputStream() {
		return err;
	}

	public InputStream _getInputStream() {
		return in;
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

	private static final LoadingProcess loop = (i, loading) -> {
		return ++i == loading.length ? 3 : i;
	};

	private static final LoadingProcess fandb = (i, loading) -> {
		if (loading.length == 4) // in case it has only 1...
			return 3;
		if ((int) loading[2] == 0b0) {
			i++;
			if (i >= loading.length) {
				i -= 2;
				loading[2] = 0b1;
			}
		} else {
			i--;
			if (i <= 2) {
				i += 2;
				loading[2] = 0b0;
			}
		}
		return i;
	};

	private static final LoadingProcess ltime = (i, loading) -> {
		loading[3] = String.format("%ss", (System.nanoTime() - (long) loading[4]) / 1000000000);
		return i;
	};
	private static final Object[] loadingTime = { ltime, 100, 0, "5s", null };
	/*
	 * [0] = index manip function [1] = time in millis until next index [2] = for
	 * any extra data for index manip function [3] = initial position for printing
	 * [4+] = unknown but intended for other printing strings
	 */
	private static final Object[] loading1 = { loop, 100, 0, "|", "/", "-", "\\" };
	private static final Object[] loading2 = { fandb, 100, 0b0, ".", "..", "...", "....", "....." };
	private static final Object[] loading3 = { fandb, 100, 0b0, "|.         |", "| .        |", "|  .       |",
			"|   .      |", "|    .     |", "|     .    |", "|      .   |", "|       .  |", "|        . |",
			"|         .|" };
	private static final Object[] loading4 = { loop, 100, 0, ">           <", ">-         -<", "> -       - <",
			">  -     -  <", ">   -   -   <", ">    - -    <", ">     *     <" };

	private static final Object[][] loadings = { loading1, loading2, loading3, loading4 };

	public Object[][] _getLoadings() {
		return loadings;
	}

	private static AtomicBoolean isProgramRunning = new AtomicBoolean();

	private static ExternalConsoleCommand program = null;

	@Handler
	public void onCommand(InputCommandExternalConsoleEvent event) {
		if (isProgramRunning.get())
			return;
		// To not lock weirdly the ExternalConsole
		in.consumeAll();
		new Thread(() -> {
			try {
				input.setEditable(false);
				input.setEnabled(false);
				if (event.getArgs() == null || event.getArgs().length == 0 || event.isCancelled())
					return;
				String[] args = Arrays.copyOfRange(event.getArgs(), 1, event.getArgs().length);
				ExternalConsoleCommand cmd = cmds.get(event.getArgs()[0]);
				if (cmd == null)
					return;
				if (cmd.isProgram()) {
					if (isProgramRunning.get())
						return;
					isProgramRunning.set(true);
				}
				Thread proc = new Thread(() -> {
					program = cmd;
					int result = Integer.MIN_VALUE;
					try {
						result = cmd.executeCommand(args);						
					}catch (Throwable e) {
						e.printStackTrace();
						println(String.format("An error with %s %s has occurred", cmd.isProgram()?"program":"command", cmd.getCommand()));
					}
					if (cmd.isProgram())
						isProgramRunning.set(false);
					EventManager.triggerEvent(new AfterCommandExecutionExternalConsole(cmd, args, result));
					program = null;
				});
				proc.start();
				if (!cmd.isProgram())
					proc.join(5 * 1000);
				Object[] loading = loadings[ThreadLocalRandom.current().nextInt(loadings.length)];
				if (cmd instanceof TimerCommand) {
					loading = loadingTime;
					loading[4] = System.nanoTime() - 5000000000L;
				}
				int i = 3;
				while (proc.isAlive() && !isProgramRunning.get()) {
					input.setText(String.format("Processing %s", loading[i]));
					proc.join((int) loading[1]);
					i = ((LoadingProcess) loading[0]).nextLoading(i, loading);
				}
				input.setText("");
				// input.setEditable(true);
				// input.setEnabled(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				input.setEnabled(true);
				input.setEditable(true);
				input.requestFocusInWindow();
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
		// removeDemoCmds();
		setSystemStreams();
		registerEventListener(new Object() {
			@Handler
			public void onClose(ExternalConsoleClosingEvent event) {
				singleton.dispose();
				System.exit(0);
			}
		});
		setViewable(true);
	}

}
