package com.qr.generator.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.qr.generator.exceptions.QrGenerationException;

public class ImageUtils {

	private ImageUtils() {
		super();
	}

	public static BufferedImage imageToBufferedImage(Image image) {
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return bufferedImage;
	}

	public static byte[] readImage(String path) throws QrGenerationException {
		byte[] bytes = new byte[0];
		try {
			Path source = Paths.get(path);
			BufferedImage bi = ImageIO.read(source.toFile());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", baos);
			bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new QrGenerationException(e);
		}
		return bytes;
	}
}
