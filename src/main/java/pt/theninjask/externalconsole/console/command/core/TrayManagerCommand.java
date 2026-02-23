package pt.theninjask.externalconsole.console.command.core;

import pt.theninjask.externalconsole.console.ExternalConsole;
import pt.theninjask.externalconsole.console.ExternalConsoleAllCommandConsumerCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleTrayCommand;
import pt.theninjask.externalconsole.util.TrayManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class TrayManagerCommand implements ExternalConsoleAllCommandConsumerCommand {

    private Map<String, ExternalConsoleCommand> allCommands;
    private final ExternalConsole console;
    private final TrayManager trayManager;

    public TrayManagerCommand(ExternalConsole console) {
        this.console = console;
        this.trayManager = new TrayManager();
    }

    @Override
    public String getCommand() {
        return "tray";
    }

    @Override
    public String getDescription() {
        return "Changes External Console between console and tray mode";
    }

    @Override
    public int executeCommand(String... args) {
        return console.isViewable() ? init() : stop();
    }

    private int stop() {
        trayManager.clearMenuItems();
        trayManager.stop();
        console.setViewable(true);
        ExternalConsole.setSystemStreams();
        return 0;
    }

    private int init() {
        console.setViewable(false);
        ExternalConsole.revertSystemStreams();
        List<MenuItem> menuItems = convertCommandsToMenuItems(
                getAllCommands().values()
        );
        menuItems.forEach(trayManager::addMenuItem);
        trayManager.startUp();
        return 0;
    }

    private List<MenuItem> convertCommandsToMenuItems(
            Collection<ExternalConsoleCommand> commandList
    ) {
        return commandList.stream()
                .filter(ExternalConsoleCommand::canBeOnTray)
                .map(cmd -> {
                    MenuItem item = new MenuItem(cmd.getCommand());
                    item.addActionListener(e -> {
                        String result = cmd.resultMessage(cmd.executeCommand());
                        if (result != null && !result.isEmpty())
                            showMessageDialog(result, cmd.getCommand());
                    });
                    if (cmd instanceof ExternalConsoleTrayCommand trayCmd) {
                        trayCmd.consumeSelfMenuItem(item);
                    }
                    return item;
                })
                .sorted(Comparator.comparing(MenuItem::getLabel))
                .toList();
    }

    private static void showMessageDialog(String msg, String... title) {
        JTextArea message = new JTextArea(msg);
        message.setOpaque(false);
        message.setEditable(false);
        message.setForeground(ExternalConsole.NIGHT_THEME.font());
        Object paneBG = UIManager.get("OptionPane.background");
        Object panelBG = UIManager.get("Panel.background");
        UIManager.put("OptionPane.background", ExternalConsole.NIGHT_THEME.background());
        UIManager.put("Panel.background", ExternalConsole.NIGHT_THEME.background());
        JOptionPane.showMessageDialog(null, message, title.length > 0 ? title[0] : "", JOptionPane.PLAIN_MESSAGE, null);
        UIManager.put("OptionPane.background", paneBG);
        UIManager.put("Panel.background", panelBG);
    }

    @Override
    public boolean accessibleInCode() {
        return true;
    }

    @Override
    public boolean canBeOnTray() {
        return true;
    }

    @Override
    public void consumeCommandMapReference(Map<String, ExternalConsoleCommand> allCommands) {
        this.allCommands = allCommands;
    }

    private Map<String, ExternalConsoleCommand> getAllCommands() {
        return Optional.ofNullable(allCommands)
                .orElseGet(Collections::emptyMap);
    }
}
