package io.github.edufolly.raspi_monitor;

import java.net.InetAddress;

/**
 * @author Eduardo Folly
 */
public class StatusMonitor implements Runnable {

    private final Main main;
    private boolean go = true;

    public StatusMonitor(Main main) {
        this.main = main;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (go) {
            try {
                Config config = Config.getInstance();
                InetAddress inet = InetAddress.getByName(config.getHost());
                if (!main.isRunning()) {
                    System.out.print("Ping ");
                    if (inet.isReachable(config.getPingTimeout())) {
                        System.out.println("Online!");
                        main.online();
                    } else {
                        System.out.println("Offline!");
                        main.offline();
                    }
                } else {
                    System.out.println("Running Lock!");
                }
                Thread.sleep(config.getPingSleep());
            } catch (Throwable t) {
                main.error(t);
            }
        }
    }

    public void dispose() {
        go = false;
    }

}
