package io.github.edufolly.raspi_monitor;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;

/**
 * @author Eduardo Folly
 */
public class RaspiCommand {

    public static String exec(Main main, String... commands) {
        StringBuilder cmd = new StringBuilder();

        cmd.append("rm -f info.txt && ");

        for (String command : commands) {
            cmd.append(command.trim());
            cmd.append(" >> info.txt && ");
            cmd.append("echo \"[END]\" >> info.txt && ");
        }

        cmd.append("cat info.txt");

        return exec(main, cmd.toString());
    }

    @SuppressWarnings("BusyWait")
    public static String exec(Main main, String command) {
        Session session = null;
        ChannelExec channel = null;
        String responseString = null;

        if (main != null) {
            main.running();
        }

        try {
            Config config = Config.getInstance();

            session = new JSch().getSession(config.getUsername(),
                                            config.getHost(),
                                            config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();

            while (channel.isConnected()) {
                Thread.sleep(500);
            }

            responseString = responseStream.toString().trim();

            if (main != null) {
                main.idle();
            }
        } catch (Throwable t) {
            if (main != null) {
                main.error(t);
            } else {
                t.printStackTrace();
            }
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        return responseString;
    }

}
