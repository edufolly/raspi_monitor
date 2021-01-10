package io.github.edufolly.raspi_monitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eduardo Folly
 */
public class InfoMonitor implements Runnable {

    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern gpuPattern = Pattern.compile("([\\d\\.]+)");

    private static final Pattern idlePattern = Pattern
            .compile("%.*\\s(\\d+\\.\\d) id");

    private static final Pattern ramPattern = Pattern
            .compile("Mem.*\\s(\\d+\\.\\d) total\\D+(\\d+\\.\\d) free");

    private final Main main;
    private boolean go = true;

    public InfoMonitor(Main main) {
        this.main = main;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (go) {
            Config config = Config.getInstance();

            Float gpuTemp = null;
            Float cpuTemp = null;
            Float cpuUsage = null;
            Float ramFree = null;

            try {
                try {
                    String[] ret = RaspiCommand
                            .exec(main, config.getInfoTimeout(),
                                  "/opt/vc/bin/vcgencmd measure_temp",
                                  "cat /sys/class/thermal/thermal_zone0/temp",
                                  "top -b | head -n 5");

                    try {
                        Matcher gpuMatcher = gpuPattern.matcher(ret[0]);
                        if (gpuMatcher.find()) {
                            gpuTemp = Float.parseFloat(gpuMatcher.group());
                        }
                    } catch (Throwable t) {
                        main.error(t);
                    }

                    try {
                        cpuTemp = Float.parseFloat(ret[1]) / 1000f;
                    } catch (Throwable t) {
                        main.error(t);
                    }

                    try {
                        Matcher idleMatcher = idlePattern.matcher(ret[2]);
                        if (idleMatcher.find()) {
                            cpuUsage = 100f - Float
                                    .parseFloat(idleMatcher.group(1));
                        }
                    } catch (Throwable t) {
                        main.error(t);
                    }

                    try {
                        Matcher ramMatcher = ramPattern.matcher(ret[2]);
                        if (ramMatcher.find()) {
                            float totalRam =
                                    Float.parseFloat(ramMatcher.group(1));

                            float freeRam =
                                    Float.parseFloat(ramMatcher.group(2));

                            ramFree = freeRam / totalRam * 100f;
                        }
                    } catch (Throwable t) {
                        main.error(t);
                    }

                } catch (Throwable t) {
                    main.error(t);
                }

                main.updateInfo(ramFree, cpuUsage, cpuTemp, gpuTemp);

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
