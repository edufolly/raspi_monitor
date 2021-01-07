package io.github.edufolly.raspi_monitor;

import com.twelvemonkeys.image.ResampleOp;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Eduardo Folly
 */
public class Icon {

    public static BufferedImage get() throws IOException {
        InputStream inputStream =
                Icon.class.getResourceAsStream("/RaspiMonitor.svg");
        return ImageIO.read(inputStream);
    }

    public static BufferedImage get(String color) {
        try {
            InputStream inputStream =
                    Icon.class.getResourceAsStream("/icon.svg");

            String content = IOUtils.toString(inputStream);

            content = content.replaceAll("babaca", color);

            BufferedImage image = ImageIO.read(IOUtils.toInputStream(content));

            BufferedImageOp imageOp = new ResampleOp(13, 16,
                                                     ResampleOp.FILTER_LANCZOS);

            return imageOp.filter(image, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }

    public static BufferedImage red() {
        return Icon.get("f44336");
    }

    public static BufferedImage green() {
        return Icon.get("4caf50");
    }

    public static BufferedImage blue() {
        return Icon.get("2196f3");
    }

    public static BufferedImage purple() {
        return Icon.get("9c27b0");
    }

}
