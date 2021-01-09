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
                String[] ret = RaspiCommand
                        .exec(main, config.getInfoTimeout(),
                              "/opt/vc/bin/vcgencmd measure_temp",
                              "cat /sys/class/thermal/thermal_zone0/temp",
                              "top -b | head -n 5");

                System.out.println(ret.length);
                for (String s : ret) {
                    System.out.println(s);
                }
                /*
temp=55.8'C
[END]
55844
[END]
top - 20:40:39 up  3:13,  0 users,  load average: 0.00, 0.04, 0.08
Tasks: 112 total,   1 running, 111 sleeping,   0 stopped,   0 zombie
%Cpu(s):  2.8 us,  1.4 sy,  0.0 ni, 95.8 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
MiB Mem :    924.8 total,    636.1 free,     41.2 used,    247.6 buff/cache
MiB Swap:    100.0 total,    100.0 free,      0.0 used.    814.4 avail Mem
[END]
                 */

                Thread.sleep(config.getInfoSleep());
            } catch (Throwable t) {
                main.error(t);
            }
        }
    }

    public void dispose() {
        go = false;
    }

}
