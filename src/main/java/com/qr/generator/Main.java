package com.qr.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.qr.generator.QRCode.Shape;

public class Main {

	public static void main(String[] args) throws Exception {
		QRCode.Builder.get().withQrShape(Shape.RECTANGLE)
		.withFinderPatternShape(Shape.RECTANGLE)
		.withData(
				"{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\",\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage,usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}")
				.withData("bala")
				.withQrScaleDownFactor(0.90f)
//				.withOverlay(readImage("./logo.jpeg"))
				.build().verify().toFile("./qrCode.jpeg", "PNG");
	}

	public static byte[] readImage(String path) {
		byte[] bytes = new byte[0];
		try {
			Path source = Paths.get(path);
			BufferedImage bi = ImageIO.read(source.toFile());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", baos);
			bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return bytes;
	}
}
