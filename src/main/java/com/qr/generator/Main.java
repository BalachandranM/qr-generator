package com.qr.generator;

import com.qr.generator.QRCode.Shape;
import com.qr.generator.exceptions.QrGenerationException;
import com.qr.generator.utils.ImageUtils;

public class Main {

	public static void main(String[] args) {
		try {
			QRCode.Builder.get().withQrShape(Shape.OVAL).withFinderPatternShape(Shape.RECTANGLE)
					.withFinderPatternSize(1).withSize(300, 300)
//		.withData(
//				"{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\",\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\",\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage,usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}")
					.withData("bala").withQrScaleDownFactor(0.90f).withOverlay(ImageUtils.readImage("./logo.jpeg"))
					.build().verify().toFile("./qrCode.jpeg", "PNG");
		} catch (QrGenerationException e) {
			e.printStackTrace();
		}
	}
}
