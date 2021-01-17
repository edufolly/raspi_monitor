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

        int infoTimeout = Integer.parseInt(dotenv.get("INFO_TIMEOUT", "8000"));
        config.setInfoTimeout(infoTimeout);

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
            } catch (AWTException | InterruptedException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "System Tray not supported!");
        }

    }

    private TrayIcon trayIcon;
    private SystemTray tray;

    private Status status = Status.Offline;
    private Status lastStatus = Status.Offline;

    private StatusMonitor statusMonitor;
    private final MenuItem statusMenu = new MenuItem("Offline");

    private final MenuItem ramFreeMenu = new MenuItem();
    private final MenuItem cpuUsageMenu = new MenuItem();
    private final MenuItem cpuTempMenu = new MenuItem();
    private final MenuItem gpuTempMenu = new MenuItem();

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

    public void start() throws InterruptedException {
        statusMonitor = new StatusMonitor(this);
        new Thread(statusMonitor).start();

        Thread.sleep(2000);

        infoMonitor = new InfoMonitor(this);
        new Thread(infoMonitor).start();

        refreshMenu();
    }

    private void refreshMenu() {
        PopupMenu popupMenu = new PopupMenu();
        statusMenu.setEnabled(false);
        popupMenu.add(statusMenu);

        ramFreeMenu.setEnabled(false);
        popupMenu.add(ramFreeMenu);

        cpuUsageMenu.setEnabled(false);
        popupMenu.add(cpuUsageMenu);

        cpuTempMenu.setEnabled(false);
        popupMenu.add(cpuTempMenu);

        gpuTempMenu.setEnabled(false);
        popupMenu.add(gpuTempMenu);

        updateInfo(null, null, null, null);

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

    public void updateInfo(Float ramFree, Float cpuUsage, Float cpuTemp,
                           Float gpuTemp) {

        ramFreeMenu.setLabel(
                ramFree == null
                ? "RAM Free: ?"
                : String.format("RAM Free: %.1f%%", ramFree));

        cpuUsageMenu.setLabel(
                cpuUsage == null
                ? "CPU Use: ?"
                : String.format("CPU Use: %.1f%%", cpuUsage));

        cpuTempMenu.setLabel(
                cpuTemp == null
                ? "CPU Temp: ?"
                : String.format("CPU Temp: %.1f˚C", cpuTemp));

        gpuTempMenu.setLabel(
                gpuTemp == null
                ? "GPU Temp: ?"
                : String.format("GPU Temp: %.1f˚C", gpuTemp));
    }

    public void online() {
        status = Status.Online;
        if (status != lastStatus) {
            statusMenu.setLabel("Online");
            trayIcon.setImage(Icon.green());
            lastStatus = status;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isOffline() {
        return status == Status.Offline;
    }

    public void offline() {
        status = Status.Offline;
        if (status != lastStatus) {
            statusMenu.setLabel("Offline");
            trayIcon.setImage(Icon.red());
            updateInfo(null, null, null, null);
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
        if (!isOffline()) {
            RaspiCommand.exec(this, 10000, "sudo shutdown now");
        }
    }

    public void dispose() {
        statusMonitor.dispose();
        infoMonitor.dispose();
        tray.remove(trayIcon);
        System.exit(0);
    }

}
