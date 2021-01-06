import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        Config config = Config.getInstance();

        String host = dotenv.get("HOST");
        config.setHost(host);

        int port = Integer.parseInt(dotenv.get("POST", "22"));
        config.setPort(port);

        String username = dotenv.get("USERNAME");
        config.setUsername(username);

        String password = dotenv.get("PASSWORD");
        config.setPassword(password);

        int pingTimeout = Integer.parseInt(dotenv.get("PING_TIMEOUT", "5000"));
        config.setPingTimeout(pingTimeout);

        long pingSleep = Long.parseLong(dotenv.get("PING_SLEEP", "5000"));
        config.setPingSleep(pingSleep);

        if (SystemTray.isSupported()) {
            try {
                TrayIcon trayIcon = new TrayIcon(Icon.red());

                config.setTrayIcon(trayIcon);

                SystemTray tray = SystemTray.getSystemTray();

                config.setTray(tray);

                tray.add(trayIcon);

                config.start();
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "System Tray not suppoerted!");
        }

    }

}
