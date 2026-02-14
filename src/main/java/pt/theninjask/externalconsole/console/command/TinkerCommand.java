package pt.theninjask.externalconsole.console.command;

import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.console.ExternalConsoleTrayCommand;
import pt.theninjask.externalconsole.util.KeyPressedAdapter;
import pt.theninjask.externalconsole.util.SmoothMoveRobot;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class TinkerCommand implements ExternalConsoleTrayCommand {

    private MenuItem self;

    private AtomicBoolean on = new AtomicBoolean(false);

    @Override
    public String getCommand() {
        return "tinker";
    }

    @Override
    public String getDescription() {
        return "For test purposes";
    }

    @Override
    public boolean isDemo() {
        return true;
    }

    @Override
    public int executeCommand(String... args) {
        try {
            if (on.get()) {
                on.set(false);
                Optional.ofNullable(self).ifPresent(item -> item.setLabel(this.getCommand() + " (off)"));
            } else {
                on.set(true);
                Optional.ofNullable(self).ifPresent(item -> item.setLabel(this.getCommand() + " (on)"));
                new Thread(() -> {
                    try {
                        Robot robot = new SmoothMoveRobot();
                        while (on.get()) {
                            robot.keyPress(KeyEvent.VK_F15);
                            Thread.sleep(5000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Optional.ofNullable(self).ifPresent(item -> item.setLabel(this.getCommand() + " (off)"));
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }

    @Override
    public boolean canBeOnTray() {
        return true;
    }

    @Override
    public void consumeSelfMenuItem(MenuItem item) {
        this.self = item;
        this.self.setLabel(this.getCommand() + (on.get() ? " (on)" : " (off)"));
    }
}
