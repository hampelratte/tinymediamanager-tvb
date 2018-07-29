package tmm.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {

    public static Image loadThumbnail(File thumbnail, int height) throws IOException {
        if (thumbnail.exists()) {
            BufferedImage image = ImageIO.read(thumbnail);
            int h = image.getHeight();
            int w = image.getWidth();
            double aspectRatio = (double) h / (double) w;
            int scaledWidth = (int) (height / aspectRatio);
            Image smallThumb = image.getScaledInstance(scaledWidth, height, java.awt.Image.SCALE_SMOOTH);
            return smallThumb;
        } else {
            throw new RuntimeException("Image " + thumbnail.getAbsolutePath() + " does not exist");
        }
    }
}
