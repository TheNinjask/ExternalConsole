package pt.theninjask.externalconsole.console.command;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import pt.theninjask.externalconsole.console.ExternalConsoleCommand;
import pt.theninjask.externalconsole.util.KeyPressedAdapter;
import pt.theninjask.externalconsole.util.SmoothMoveRobot;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class TinkerFunCommand implements ExternalConsoleCommand {

    private AtomicBoolean on = new AtomicBoolean(false);

    private static int stopKey = NativeKeyEvent.VC_ESCAPE;
    public TinkerFunCommand() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

                @Override
                public void nativeKeyTyped(NativeKeyEvent e) {
                    // DO NOTHING
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent e) {
                    // DO NOTHING
                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    if (e.getKeyCode() == stopKey) {
                        on.set(false);
                    }
                }
            });
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getCommand() {
        return "tinker-fun";
    }

    @Override
    public String getDescription() {
        return "Dummy Command for testing";
    }

    @Override
    public int executeCommand(String... args) {
        try {
            if (on.get()) {
                on.set(false);
            } else {
                on.set(true);
                new Thread(() -> {
                    try {
                        Robot robot = new SmoothMoveRobot();
                        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                        int width = (int) dim.getWidth();
                        int height = (int) dim.getHeight();
                        int x = ThreadLocalRandom.current().nextInt(0, width);
                        int y = ThreadLocalRandom.current().nextInt(0, height);
                        int xSpeed = 100;
                        int ySpeed = 100;
                        while (on.get()) {
                            robot.mouseMove(x,y);
                            x += xSpeed;
                            y += ySpeed;
                            x = Math.min(x, width);
                            x = Math.max(x, 0);
                            y = Math.min(y, height);
                            y = Math.max(y, 0);
                            if(x == width || x==0)
                                xSpeed *= -1;
                            if(y == height || y==0)
                                ySpeed *= -1;
                            //Thread.sleep(151);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

}
