package io.github.edufolly.raspi_monitor;

/**
 * @author Eduardo Folly
 */
public class Config {

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
    private int infoTimeout;
    private long infoSleep;

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

    public int getInfoTimeout() {
        return infoTimeout;
    }

    public void setInfoTimeout(int infoTimeout) {
        this.infoTimeout = infoTimeout;
    }

    public long getInfoSleep() {
        return infoSleep;
    }

    public void setInfoSleep(long infoSleep) {
        this.infoSleep = infoSleep;
    }

}
