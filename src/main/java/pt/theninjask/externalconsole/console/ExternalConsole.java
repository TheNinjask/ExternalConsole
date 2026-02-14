package pt.theninjask.externalconsole.console;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.listener.Handler;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.reflections.Reflections;
import pt.theninjask.externalconsole.console.command.LoadingExternalConsoleCommand;
import pt.theninjask.externalconsole.console.command.TimerCommand;
import pt.theninjask.externalconsole.console.component.ScreenConsole;
import pt.theninjask.externalconsole.console.util.LoadingProcess;
import pt.theninjask.externalconsole.console.util.UsedCommand;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleErrorOutputStream;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleInputStream;
import pt.theninjask.externalconsole.console.util.stream.ExternalConsoleOutputStream;
import pt.theninjask.externalconsole.event.Event;
import pt.theninjask.externalconsole.event.*;
import pt.theninjask.externalconsole.stream.RedirectorErrorOutputStream;
import pt.theninjask.externalconsole.stream.RedirectorInputStream;
import pt.theninjask.externalconsole.stream.RedirectorOutputStream;
import pt.theninjask.externalconsole.util.ColorTheme;
import pt.theninjask.externalconsole.util.KeyPressedAdapter;
import pt.theninjask.externalconsole.util.TrayManager;
import pt.theninjask.externalconsole.util.Utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ExternalConsole extends JFrame {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public static final ColorTheme TWITCH_THEME = new ColorTheme("Twitch", new Color(0xe5e5e5),
            new Color(123, 50, 250));

    public static final ColorTheme NIGHT_THEME = new ColorTheme("Night", Color.WHITE, Color.BLACK);

    public static final ColorTheme DAY_THEME = new ColorTheme("Day", Color.BLACK, Color.WHITE);

    private ExternalConsoleOutputStream outputStream;

    private ExternalConsoleErrorOutputStream errorOutputStream;

    private ExternalConsoleInputStream inputStream;

    private JPanel messagePanel;

    private JTextField input;

    private final Map<String, ExternalConsoleCommand> cmds;

    private final Map<String, String> vars;

    private ColorTheme currentTheme;

    private UsedCommand last;

    private ScreenConsole screenConsole;

    private static final ExternalConsole singleton = new ExternalConsole();

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
        this.setLayout(new BorderLayout());
        this.screenConsole = new ScreenConsole(this);
        this.add(screenConsole, BorderLayout.CENTER);
        this.add(inputConsole(), BorderLayout.SOUTH);
        screenConsole.getParent().setBackground(null);
        this.outputStream = new ExternalConsoleOutputStream(this);
        this.errorOutputStream = new ExternalConsoleErrorOutputStream(this);
        this.inputStream = new ExternalConsoleInputStream();

        this.cmds = new HashMap<>();
        this.vars = new HashMap<>();

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

        this.last = UsedCommand.NULL_UC;

        this.screenConsole.getScreen().setForeground(currentTheme.font());
        this.setBackground(currentTheme.background());

        this.input.setBorder(BorderFactory.createLineBorder(currentTheme.font(), 1));
        this.input.setForeground(currentTheme.font());
        this.input.setCaretColor(currentTheme.font());
        this.input.setBackground(currentTheme.background());

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

    public Map<String, String> getAllVars() {
        return vars;
    }

    public static List<ExternalConsoleCommand> getAllCommands() {
        return singleton.cmds.values().parallelStream().filter(ExternalConsoleCommand::accessibleInCode).toList();
    }

    public static List<String> getAllCommandsAsString() {
        return getAllCommandsAsString(true);
    }

    public static List<String> getAllCommandsAsString(boolean onlyAccessibleInCode) {
        return singleton.cmds.values().parallelStream().filter(p -> p.accessibleInCode() || !onlyAccessibleInCode)
                .map(ExternalConsoleCommand::getCommand).toList();
    }

    public static boolean executeCommand(String cmd, String... args) {
        ExternalConsoleCommand ecmd = singleton.cmds.get(cmd);
        if (ecmd == null || !ecmd.accessibleInCode() || ecmd.isProgram())
            return false;
        args = singleton.parseArgsVars(args);
        int result = ecmd.executeCommand(args);
        EventManager.triggerEvent(new AfterCommandExecutionExternalConsole(ecmd, args, result));
        return true;
    }

    public static void removeDemoCmds() {
        for (ExternalConsoleCommand cmd : singleton.cmds.values().stream().filter(ExternalConsoleCommand::isDemo).toList()) {
            singleton.cmds.remove(cmd.getCommand());
        }
    }

    private JPanel inputConsole() {
        messagePanel = new JPanel(new BorderLayout());
        input = new JTextField();
        input.setBorder(null);

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
                    default -> {
                    }
                    case KeyEvent.VK_TAB -> {
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
                            currArgs = new String[]{};
                        List<String> options;
                        if (isProgramRunning.get() && cmd != null) {
                            String[] paramOptions = cmd.getParamOptions(args.length - 1, currArgs);
                            options = paramOptions != null ? Stream.of(paramOptions).filter(c -> c.startsWith(ref)).toList() : Collections.emptyList();
                        } else if (args.length <= 1 || cmd == null
                                || cmd.getParamOptions(args.length - 2, currArgs) == null) {
                            options = cmds.values().stream().filter(c -> c.getCommand().startsWith(ref))
                                    .sorted(ExternalConsoleCommand.comparator).map(ExternalConsoleCommand::getCommand).toList();
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
                    }
                    case KeyEvent.VK_UP, KeyEvent.VK_KP_UP -> {
                        if (last != UsedCommand.NULL_UC) {
                            if (last.getPrevious() != UsedCommand.NULL_UC) {
                                input.setText(last.getFullCommand());
                                last = last.getPrevious();
                            }
                        } else {
                            input.setText("");
                        }
                    }
                    case KeyEvent.VK_DOWN, KeyEvent.VK_KP_DOWN -> {
                        if (last != UsedCommand.NULL_UC && last.getNext() != UsedCommand.NULL_UC
                                && last.getNext().getNext() != UsedCommand.NULL_UC) {
                            last = last.getNext();
                            input.setText(last.getNext().getFullCommand());
                        }
                    }
                    case KeyEvent.VK_ENTER -> {
                        InputCommandExternalConsoleEvent event = new InputCommandExternalConsoleEvent(
                                inputToArgs(input.getText()));
                        while (last.getNext() != UsedCommand.NULL_UC)
                            last = last.getNext();
                        if (last == UsedCommand.NULL_UC)
                            last = new UsedCommand(null, UsedCommand.NULL_UC, UsedCommand.NULL_UC);
                        UsedCommand current = new UsedCommand(input.getText(), last, UsedCommand.NULL_UC);
                        last.setNext(current);
                        last = current;

                        inputStream.insertData((input.getText() + "\n").getBytes());
                        input.setText("");
                        EventManager.triggerEvent(event);
                    }
                }
            }
        });
        Consumer<Component> setDropTarget = (component) ->
                component.setDropTarget(new DropTarget() {
                    /**
                     *
                     */
                    @Serial
                    private static final long serialVersionUID = 1L;

                    @SuppressWarnings("unchecked")
                    public void drop(DropTargetDropEvent evt) {
                        try {
                            evt.acceptDrop(DnDConstants.ACTION_COPY);
                            List<File> droppedFiles = (List<File>) evt
                                    .getTransferable().getTransferData(
                                            DataFlavor.javaFileListFlavor);
                            StringJoiner join = new StringJoiner(
                                    String.format("%s%s%s", QUOTE_CHAR, ARG_DIV_CHAR, QUOTE_CHAR),
                                    String.valueOf(QUOTE_CHAR),
                                    String.valueOf(QUOTE_CHAR));
                            for (File file : droppedFiles) {
                                join.add(file.getAbsolutePath());
                            }
                            StringBuilder build = new StringBuilder();
                            build.append(input.getText());
                            if (build.length() > 0 && build.charAt(build.length() - 1) != ARG_DIV_CHAR)
                                build.append(ARG_DIV_CHAR);
                            build.append(join);
                            input.setText(build.toString());
                            input.requestFocusInWindow();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        setDropTarget.accept(input);
        setDropTarget.accept(screenConsole.getScreen());
        setDropTarget.accept(screenConsole);

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

    public String[] inputToArgs(String input) {
        return inputToArgs(input, false);
    }

    public static final char ESCAPE_CHAR = '\\';
    public static final char QUOTE_CHAR = '\"';
    public static final char ARG_DIV_CHAR = ' ';

    public String[] inputToArgs(String input, boolean emptyLast) {
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
            if (chary == QUOTE_CHAR && (i == 0 || input.charAt(i - 1) != ESCAPE_CHAR)) {
                // if(i>0 && input.charAt(i-1)!='\\')
                quote = !quote;
                if (i + 1 == input.length()) {
                    link[0] = arg;
                    argSize++;
                }
            } else if (chary == ARG_DIV_CHAR && !quote) {
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
                if (chary == QUOTE_CHAR)
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
        return parseArgsVars(args);
    }

    private String[] parseArgsVars(String[] args) {
        return Arrays.stream(args)
                .map(argy -> vars.getOrDefault(argy, argy))
                .toArray(String[]::new);
    }

    public String argsToInput(String[] args) {
        StringBuilder input = new StringBuilder();
        for (String string : args) {
            if (string.indexOf(QUOTE_CHAR) != -1)
                string = string.replaceAll(
                        String.format("%s%s", ESCAPE_CHAR, QUOTE_CHAR),
                        String.format("%s%s%s%s", ESCAPE_CHAR, ESCAPE_CHAR, ESCAPE_CHAR, QUOTE_CHAR));
            if (string.indexOf(ARG_DIV_CHAR) != -1)
                string = String.format("%s%s%s", QUOTE_CHAR, string, QUOTE_CHAR);

            input.append(string).append(ARG_DIV_CHAR);
        }
        return input.toString().stripTrailing();
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
        singleton.screenConsole.getScreen().setForeground(theme.font());
        singleton.setBackground(theme.background());

        singleton.input.setBorder(BorderFactory.createLineBorder(theme.font(), 1));
        singleton.input.setForeground(theme.font());
        singleton.input.setCaretColor(theme.font());
        singleton.input.setBackground(theme.background());
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
        RedirectorOutputStream.changeRedirect(singleton.outputStream);
        RedirectorErrorOutputStream.changeRedirect(singleton.errorOutputStream);
        RedirectorInputStream.changeRedirect(singleton.inputStream);
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
    }

    public static void unregisterEventListener(Object listener) {
        EventManager.unregisterEventListener(listener);
    }

    public static void triggerEvent(Event event) {
        EventManager.triggerEvent(event);
    }


    // Cannot think of a use case and will be in conflict with OnCommand()
    /*
     * public static void enableInput(boolean enable) { if(!enable)
     * singleton.input.setText(""); singleton.input.setEnabled(enable); }
     */

    private static final LoadingProcess loop = (i, loading) -> ++i == loading.length ? 3 : i;

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
    private static final Object[] loadingTime = {ltime, 100, 0, "5s", null};
    /*
     * [0] = index manip function [1] = time in millis until next index [2] = for
     * any extra data for index manip function [3] = initial position for printing
     * [4+] = unknown but intended for other printing strings
     */
    private static final Object[] loading1 = {loop, 100, 0, "|", "/", "-", "\\"};
    private static final Object[] loading2 = {fandb, 100, 0b0, ".", "..", "...", "....", "....."};
    private static final Object[] loading3 = {fandb, 100, 0b0, "|.         |", "| .        |", "|  .       |",
            "|   .      |", "|    .     |", "|     .    |", "|      .   |", "|       .  |", "|        . |",
            "|         .|"};
    private static final Object[] loading4 = {loop, 100, 0, ">           <", ">-         -<", "> -       - <",
            ">  -     -  <", ">   -   -   <", ">    - -    <", ">     *     <"};

    private static final Object[][] loadings = {loading1, loading2, loading3, loading4};

    public Object[][] _getLoadings() {
        return loadings;
    }

    private static final AtomicBoolean isProgramRunning = new AtomicBoolean();

    private static ExternalConsoleCommand program = null;

    @Handler
    public Thread onCommand(InputCommandExternalConsoleEvent event) {
        if (isProgramRunning.get())
            return null;
        println(argsToInput(event.getArgs()));
        inputStream.consumeAll();
        // To not lock weirdly the ExternalConsole
        var cmdThread = new Thread(() -> {
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
                        var resultMsg = cmd.resultMessage(result);
                        if (resultMsg != null) {
                            println(resultMsg);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        println(String.format("An error with %s %s has occurred", cmd.isProgram() ? "program" : "command", cmd.getCommand()));
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                input.setEnabled(true);
                input.setEditable(true);
                input.requestFocusInWindow();
            }
        });
        cmdThread.start();
        return cmdThread;
    }

    public ExternalConsoleOutputStream getOutputStream() {
        return this.outputStream;
    }

    public ExternalConsoleErrorOutputStream getErrorOutputStream() {
        return this.errorOutputStream;
    }

    public ExternalConsoleInputStream getInputStream() {
        return this.inputStream;
    }

    public void setOutputStream(ExternalConsoleOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setErrorOutputStream(ExternalConsoleErrorOutputStream errorOutputStream) {
        this.errorOutputStream = errorOutputStream;
    }

    public void setInputStream(ExternalConsoleInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public static void loadObjectAsCommand(Object object) {
        loadObjectAsCommand(object, false, false);
    }


    public static void loadObjectAsCommand(Object object, boolean includeOthers, boolean accessibleInCode) {
        Class<?> clazz = object.getClass();
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> includeOthers || (!m.getName().startsWith("$") && !m.getName().startsWith("lambda")))
                .forEach(m ->
                        ExternalConsole.addCommand(
                                new ExternalConsoleCommand() {
                                    @Override
                                    public String getCommand() {
                                        return "%s.%s".formatted(clazz.getSimpleName(), m.getName());
                                    }

                                    @Override
                                    public String getDescription() {
                                        return "%s.%s%s".formatted(
                                                clazz.getSimpleName(),
                                                m.getName(),
                                                Arrays.stream(m.getParameterTypes())
                                                        .map(Class::getSimpleName)
                                                        .collect(Collectors.joining(",", "(", ")"))
                                        );
                                    }

                                    @Override
                                    public boolean accessibleInCode() {
                                        return accessibleInCode;
                                    }

                                    @Override
                                    public int executeCommand(String... args) {
                                        try {
                                            Object[] mArgs = new Object[m.getParameterCount()];
                                            IntStream.range(0, m.getParameterCount())
                                                    .forEach(pi -> {
                                                        try {
                                                            mArgs[pi] = Utils.MAPPER.readValue(args[pi], m.getParameterTypes()[pi]);
                                                        } catch (JsonProcessingException e) {
                                                            e.printStackTrace();
                                                        }
                                                    });
                                            m.trySetAccessible();
                                            var result = m.invoke(object, mArgs);
                                            if (!m.getReturnType().isInstance(void.class)) {
                                                ExternalConsole.println(Utils.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                                            }
                                            return 0;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            return -1;
                                        }
                                    }

                                    @Override
                                    public String[] getParamOptions(int number, String[] currArgs) {
                                        if (number >= m.getParameterTypes().length) {
                                            return null;
                                        }
                                        return new String[]{
                                                "%s (%s)".formatted(
                                                        m.getParameters()[number].getName(),
                                                        m.getParameterTypes()[number].getSimpleName()
                                                )
                                        };
                                    }

                                    @Override
                                    public String resultMessage(int result) {
                                        return switch (result) {
                                            case -1 -> "An exception has occurred!";
                                            default -> ExternalConsoleCommand.super.resultMessage(result);
                                        };
                                    }
                                }
                        )
                );
    }

    private static class EventManager {

        private static final SimpleFormatter LOGGER_FORMATTER = new SimpleFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format("[%s] %s\n", sdf.format(new Date(lr.getMillis())), lr.getMessage());
            }
        };

        private static final Logger logger = setUpLogger();

        private static final EventManager singleton = new EventManager();

        private final MBassador<Object> dispatcher;

        private EventManager() {
            IBusConfiguration config = new BusConfiguration()
                    .addPublicationErrorHandler(error -> {
                    }).addFeature(Feature.SyncPubSub.Default())
                    .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                    .addFeature(Feature.AsynchronousMessageDispatch.Default());
            this.dispatcher = new MBassador<>(config);
        }

        private static Logger setUpLogger() {
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
            return logger.getLevel().equals(Level.ALL);
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

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        System.setProperty("Dlog4j2.formatMsgNoLookups", "true");
        setSystemStreams();
        registerEventListener(new Object() {
            @Handler
            public void onClose(ExternalConsoleClosingEvent event) {
                singleton.dispose();
                System.exit(0);
            }
        });
        setViewable(true);
        ExternalConsole.executeCommand("tray");
    }

}
