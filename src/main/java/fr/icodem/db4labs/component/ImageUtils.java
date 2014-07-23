package fr.icodem.db4labs.component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static byte[] convertToJPEG(byte[] source) throws IOException {
        // load image from source
        ByteArrayInputStream bais = new ByteArrayInputStream(source);
        BufferedImage originalImage = ImageIO.read(bais);

        // write to JPEG format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);

        return baos.toByteArray();
    }

    public static byte[] reduceImage(byte[] source, int maxWidth, int maxHeight) throws IOException {
        // load image from source
        ByteArrayInputStream bais = new ByteArrayInputStream(source);
        BufferedImage originalImage = ImageIO.read(bais);

        // keep proportions
        float originalWidth = originalImage.getWidth();
        float originalHeight = originalImage.getHeight();
        float originalRatio = originalWidth / originalHeight;
        float maxRatio = maxWidth / maxHeight;

        float newWidth = originalWidth;
        float newHeight = originalHeight;
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            if (originalRatio < maxRatio) {
                newWidth = maxHeight / originalHeight * originalWidth;
                newHeight = maxHeight;
            } else {
                newWidth = maxWidth;
                newHeight = maxWidth / originalWidth * originalHeight;
            }
        }

        // resize image
        BufferedImage resizedImage = new BufferedImage((int)newWidth, (int)newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();

        g.drawImage(originalImage, 0, 0, (int)newWidth, (int)newHeight, null);
        g.dispose();

        // write image to output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //compressImage(resizedImage, baos);
        ImageIO.write(resizedImage, "jpg", baos);

        return baos.toByteArray();
    }

    /*
    public static void compressImage(BufferedImage image, ByteArrayOutputStream baos) throws IOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = iter.next();

        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        ImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.7F);

        writer.write(null, new IIOImage(image, null, null), param);

        ios.flush();
        writer.dispose();
        ios.close();
    }
    */

}
