import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.awt.*;
import java.io.ByteArrayOutputStream;

public class Config {

    enum Status {
        Online,
        Offline
    }

    private static final Config INSTANCE;

    static {
        INSTANCE = new Config();
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    private String host;
    private int port;
    private String username;
    private String password;
    private int pingTimeout;
    private long pingSleep;

    private TrayIcon trayIcon;
    private SystemTray tray;

    private Status status = Status.Offline;
    private Status lastStatus = Status.Offline;

    private final MenuItem statusMenu = new MenuItem("Offline");
    private final StatusMonitor statusMonitor = new StatusMonitor();

    private Config() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public long getPingSleep() {
        return pingSleep;
    }

    public void setPingSleep(long pingSleep) {
        this.pingSleep = pingSleep;
    }

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
        new Thread(statusMonitor).start();
        refreshMenu();
    }

    private void refreshMenu() {
        PopupMenu popupMenu = new PopupMenu();
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

    private void raspiShutdown() {
        raspiCommand("sudo shutdown now");
    }

    private void raspiCommand(String command) {
        Session session = null;
        ChannelExec channel = null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();

            while (channel.isConnected()) {
                System.out.println("Running...");
                Thread.sleep(500);
            }

            String responseString = responseStream.toString();
            System.out.println(responseString);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        System.out.println("Exit!");
    }

    public void dispose() {
        statusMonitor.dispose();
        tray.remove(trayIcon);
        System.exit(0);
    }

}
