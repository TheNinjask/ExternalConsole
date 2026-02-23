package pt.theninjask.externalconsole.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TrayManager {

    private List<MenuItem> options = new ArrayList<>();

    private boolean isActive = false;

    private TrayIcon trayIcon = new TrayIcon(new ImageIcon(Utils.ICON_PATH).getImage());

    private PopupMenu popup = new PopupMenu();

    public TrayManager() {
        trayIcon.setImageAutoSize(true);
    }

    public boolean isSupported() {
        return SystemTray.isSupported();
    }

    public boolean startUp() {
        if (!isActive && !options.isEmpty() && SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            options.forEach(opt -> {
                popup.add(opt);
            });
            trayIcon.setPopupMenu(popup);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                return false;
            }
            isActive = true;
            return isActive;
        }
        return false;
    }

    public boolean refresh() {
        return stop() && startUp();
    }

    public boolean stop() {
        if (isActive && SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
            isActive = false;
        }
        return true;
    }

    public TrayManager setToolTip(String tooltip) {
        trayIcon.setToolTip(tooltip);
        return this;
    }

    public TrayManager setImage(Image image) {
        trayIcon.setImage(image);
        return this;
    }

    public TrayManager setImage(URL image) {
        trayIcon.setImage(new ImageIcon(image).getImage());
        return this;
    }

    public TrayManager addMenuItem(MenuItem item) {
        options.add(item);
        return this;
    }

    public TrayManager removeMenuItem(MenuItem item) {
        options.remove(item);
        return this;
    }

    public TrayManager clearMenuItems() {
        options.clear();
        popup.removeAll();
        return this;
    }

}
