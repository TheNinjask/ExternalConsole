package pt.theninjask.externalconsole.util;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyPressedAdapter {

    private static KeyPressedAdapter instance = new KeyPressedAdapter();

    public static KeyPressedAdapter getSingleton() {
        return instance;
    }

    private KeyPressedAdapter() {
        System.setProperty("Dlog4j2.formatMsgNoLookups", "true");
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
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
                    pressedNativeKeys.remove(e.getKeyCode());
                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent e) {
                    pressedNativeKeys.add(e.getKeyCode());
                }
            });
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Set<Integer> pressedNativeKeys = new CopyOnWriteArraySet<>();

    public static boolean isKeyPressedNative(int keycode) {
        return pressedNativeKeys.contains(keycode);
    }

    public static boolean isCTRLAndCPressedNative() {
        return KeyPressedAdapter.isKeyPressedNative(
                NativeKeyEvent.VC_CONTROL
        ) && KeyPressedAdapter.isKeyPressedNative(
                NativeKeyEvent.VC_C
        );
    }

    public static boolean isCTRLAndZPressedNative(){
        return KeyPressedAdapter.isKeyPressedNative(
                NativeKeyEvent.VC_CONTROL
        ) && KeyPressedAdapter.isKeyPressedNative(
                NativeKeyEvent.VC_Z
        );
    }

}
