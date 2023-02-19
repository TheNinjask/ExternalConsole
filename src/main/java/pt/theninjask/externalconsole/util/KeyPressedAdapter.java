package pt.theninjask.externalconsole.util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class KeyPressedAdapter implements KeyEventDispatcher {

    private static final Set<Integer> pressedKeys = new CopyOnWriteArraySet<>();

    public static boolean isKeyPressed(int keycode) {
        return pressedKeys.contains(keycode);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        synchronized (this) {
            switch (e.getID()) {
                case KeyEvent.KEY_PRESSED -> pressedKeys.add(e.getKeyCode());
                case KeyEvent.KEY_RELEASED -> pressedKeys.remove(e.getKeyCode());
            }
            return false;
        }
    }

}
