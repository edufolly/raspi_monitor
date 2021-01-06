import com.twelvemonkeys.image.ResampleOp;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.InputStream;

public class Icon {

    public static BufferedImage get(String color) {
        try {
            InputStream inputStream =
                    Icon.class.getResourceAsStream("icon.svg");

            String content = IOUtils.toString(inputStream);

            content = content.replaceAll("babaca", color);

            BufferedImage image = ImageIO.read(IOUtils.toInputStream(content));

            BufferedImageOp imageOp = new ResampleOp(14, 16,
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

}
