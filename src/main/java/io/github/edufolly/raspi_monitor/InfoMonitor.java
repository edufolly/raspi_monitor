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
            String gpuTemp = "?";
            String cpuTemp = "?";
            String cpuUsage = "?";
            String ramFree = "?";
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
                            float f = Float.parseFloat(gpuMatcher.group());
                            gpuTemp = String.format("%.1f˚C", f);
                        }
                    } catch (Throwable t) {
                        main.error(t);
                    }

                    try {
                        float temp = Float.parseFloat(ret[1]) / 1000f;
                        cpuTemp = String.format("%.1f˚C", temp);
                    } catch (Throwable t) {
                        main.error(t);
                    }

                    try {
                        Matcher idleMatcher = idlePattern.matcher(ret[2]);
                        if (idleMatcher.find()) {
                            float usage = 100f - Float
                                    .parseFloat(idleMatcher.group(1));

                            cpuUsage = String.format("%.1f%%", usage);
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

                            ramFree = String.format("%.1f%%",
                                                    freeRam / totalRam * 100f);
                        }
                    } catch (Throwable t) {
                        main.error(t);
                    }

                } catch (Throwable t) {
                    main.error(t);
                }

                System.out.println("RAM Free: " + ramFree);
                System.out.println("CPU Usage: " + cpuUsage);
                System.out.println("CPU Temp: " + cpuTemp);
                System.out.println("GPU Temp: " + gpuTemp);

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
