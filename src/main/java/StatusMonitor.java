import java.net.InetAddress;

public class StatusMonitor implements Runnable {

    private boolean go = true;

    @Override
    public void run() {
        while (go) {
            try {
                Config config = Config.getInstance();
                InetAddress inet = InetAddress.getByName(config.getHost());

                if (inet.isReachable(config.getPingTimeout())) {
                    Config.getInstance().online();

                } else {
                    Config.getInstance().offline();
                }

                Thread.sleep(config.getPingSleep());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        go = false;
    }

}
