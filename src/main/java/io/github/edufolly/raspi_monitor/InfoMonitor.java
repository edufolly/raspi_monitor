package io.github.edufolly.raspi_monitor;

/**
 * @author Eduardo Folly
 */
public class InfoMonitor implements Runnable {

    private final Main main;
    private boolean go = true;

    public InfoMonitor(Main main) {
        this.main = main;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (go) {
            try {
                Config config = Config.getInstance();
                String ret = RaspiCommand.exec(
                        "/opt/vc/bin/vcgencmd measure_temp",
                        "cat /sys/class/thermal/thermal_zone0/temp",
                        "top -b | head -n 5");
                System.out.println(ret);
                // TODO - Create a config parameter.
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        go = false;
    }

}
