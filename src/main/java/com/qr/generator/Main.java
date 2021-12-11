package com.qr.generator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args) throws Exception {
		QRCode.QRCodeBuilder.get().withData(
				"{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\",\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage,usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}")
				.withOverlay(readImage("./logo.jpeg")).build().toFile("./qrCode.jpeg", "PNG");
	}

	public static BufferedImage readImage(String path) {
		try {
			return ImageIO.read(new File(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
