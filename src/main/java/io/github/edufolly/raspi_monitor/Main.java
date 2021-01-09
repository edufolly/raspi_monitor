package io.github.edufolly.raspi_monitor;

import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Eduardo Folly
 */
public class Main {

    enum Status {
        Online,
        Offline,
        Idle,
        Running,
        Error,
    }

    public static void main(String[] args) {
        String home = System.getProperty("user.home");

        Dotenv dotenv = Dotenv.configure()
                              .directory(home)
                              .filename("raspi_monitor.env")
                              .load();

        Config config = Config.getInstance();

        String host = dotenv.get("HOST");
        config.setHost(host);

        int port = Integer.parseInt(dotenv.get("POST", "22"));
        config.setPort(port);

        String username = dotenv.get("USERNAME");
        config.setUsername(username);

        String password = dotenv.get("PASSWORD");
        config.setPassword(password);

        int pingTimeout = Integer.parseInt(dotenv.get("PING_TIMEOUT", "3000"));
        config.setPingTimeout(pingTimeout);

        long pingSleep = Long.parseLong(dotenv.get("PING_SLEEP", "5000"));
        config.setPingSleep(pingSleep);

        long infoSleep = Long.parseLong(dotenv.get("INFO_SLEEP", "60000"));
        config.setInfoSleep(infoSleep);

        if (Taskbar.isTaskbarSupported()) {
            try {
                Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(Icon.get());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (SystemTray.isSupported()) {
            try {

                Main main = new Main();

                TrayIcon trayIcon = new TrayIcon(Icon.red());

                main.setTrayIcon(trayIcon);

                SystemTray tray = SystemTray.getSystemTray();

                main.setTray(tray);

                tray.add(trayIcon);

                main.start();
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "System Tray not suppoerted!");
        }

    }

    private TrayIcon trayIcon;
    private SystemTray tray;

    private Status status = Status.Offline;
    private Status lastStatus = Status.Offline;

    private StatusMonitor statusMonitor;
    private final MenuItem statusMenu = new MenuItem("Offline");

    private InfoMonitor infoMonitor;

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    public void setTray(SystemTray tray) {
        this.tray = tray;
    }

    public void displayErrorMessage(String title, String message) {
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.ERROR);
    }

    public void start() {
        statusMonitor = new StatusMonitor(this);
        new Thread(statusMonitor).start();

        infoMonitor = new InfoMonitor(this);
        new Thread(infoMonitor).start();

        refreshMenu();
    }

    private void refreshMenu() {
        PopupMenu popupMenu = new PopupMenu();
        statusMenu.setEnabled(false);
        popupMenu.add(statusMenu);

        popupMenu.addSeparator();

        MenuItem shutdown = new MenuItem("Shutdown");
        shutdown.addActionListener(e -> raspiShutdown());
        popupMenu.add(shutdown);

        popupMenu.addSeparator();

        MenuItem exitMenu = new MenuItem("Exit");
        exitMenu.addActionListener(e -> dispose());
        popupMenu.add(exitMenu);

        trayIcon.setPopupMenu(popupMenu);
    }

    public void online() {
        status = Status.Online;
        if (status != lastStatus) {
            statusMenu.setLabel("Online");
            trayIcon.setImage(Icon.green());
            lastStatus = status;
        }
    }

    public void offline() {
        status = Status.Offline;
        if (status != lastStatus) {
            statusMenu.setLabel("Offline");
            trayIcon.setImage(Icon.red());
            displayErrorMessage("Offline", "The host is down.");
            lastStatus = status;
        }
    }

    public boolean isRunning() {
        return status == Status.Running;
    }

    public void running() {
        status = Status.Running;
        if (status != lastStatus) {
            statusMenu.setLabel("Running");
            trayIcon.setImage(Icon.blue());
            lastStatus = status;
        }
    }

    public void idle() {
        status = Status.Idle;
        lastStatus = Status.Idle;
    }

    public void error() {
        error(null);
    }

    public void error(Throwable t) {
        if (t != null) {
            t.printStackTrace();
        }
        status = Status.Error;
        if (status != lastStatus) {
            statusMenu.setLabel("Error");
            trayIcon.setImage(Icon.purple());
            lastStatus = status;
        }
    }

    private void raspiShutdown() {
        if (status == Status.Online) {
            RaspiCommand.exec(this, "sudo shutdown now");
        } else {
            error();
        }
    }

    public void dispose() {
        statusMonitor.dispose();
        infoMonitor.dispose();
        tray.remove(trayIcon);
        System.exit(0);
    }

}
