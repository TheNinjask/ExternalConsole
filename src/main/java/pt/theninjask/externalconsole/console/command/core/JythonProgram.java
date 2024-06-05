package pt.theninjask.externalconsole.console.command.core;

import org.python.core.PyStringMap;
import org.python.util.InteractiveConsole;
import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

import static pt.theninjask.externalconsole.util.KeyPressedAdapter.isKeyPressed;

public class JythonProgram implements ExternalConsoleCommand {

    private boolean isRunning = false;

    private InteractiveConsole jython;

    private final ExternalConsole console;

    public JythonProgram(ExternalConsole console) {
        this.console = console;
    }

    @Override
    public String getCommand() {
        return "jython";
    }

    @Override
    public int executeCommand(String... args) {
        try {
            if (args.length > 0) {
                var line = Arrays.stream(args).reduce("%s %s"::formatted).get();
                try (InteractiveConsole jython = new InteractiveConsole()) {
                    jython.push(line);
                }
                return 0;
            }
            ExternalConsole.executeCommand("cls");
            BufferedReader read = new BufferedReader(new InputStreamReader(console.getInputStream()));
            try (InteractiveConsole jython = new InteractiveConsole()) {
                this.jython = jython;
                this.isRunning = true;
                ExternalConsole.println("Jython 2.7.3 (CTRL+Z to exit)");
                while (!(isKeyPressed(KeyEvent.VK_CONTROL) && isKeyPressed(KeyEvent.VK_Z))) {
                    String str;
                    if ((str = read.readLine()) != null)
                        jython.push(str);
                }
                this.isRunning = false;
                this.jython = null;
            }
            ExternalConsole.executeCommand("cls");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExternalConsole.println("Leaving Jython Interpreter ...");
        return 0;
    }

    public boolean isProgram() {
        return true;
    }

    private boolean filterLocalsForParam(String local) {
        return !local.startsWith("__") || !local.endsWith("__");
    }

    @SuppressWarnings("unchecked")
    public String[] getParamOptions(int number, String[] currArgs) {
        return isRunning ? (String[]) ((PyStringMap) jython.getLocals()).keys().stream().map(Object::toString).filter(e -> filterLocalsForParam((String) e)).toArray(String[]::new) : null;
    }

}
