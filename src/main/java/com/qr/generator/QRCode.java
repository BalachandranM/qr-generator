package com.qr.generator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.qr.generator.exception.QrGenerationException;
import com.qr.generator.util.ValidationUtils;

public class QRCode {

	private Charset charSet = StandardCharsets.UTF_8;
	private int width;
	private int height;
	private int quietZone;
	private Color qrColor;
	private Color qrBackgroundColor;
	private Color finderPatternOuterColor;
	private Color finderPatternMedianColor;
	private Color finderPatternInnerColor;

	private Float overlayRatio = 0.25f;
	private Float overlayTransparency = 1f;
	private boolean overlay = false;
	private String data;
	private BufferedImage overlayImage;
	private ErrorCorrectionLevel errorCorrectionLevel;

	private QRCode(QRCodeBuilder builder) {
		charSet = builder.charSet;
		data = builder.data;
		width = builder.width;
		height = builder.height;
		quietZone = builder.quietZone;
		qrColor = builder.qrColor;
		qrBackgroundColor = builder.qrBackgroundColor;
		finderPatternOuterColor = builder.finderPatternOuterColor;
		finderPatternMedianColor = builder.finderPatternMedianColor;
		finderPatternInnerColor = builder.finderPatternInnerColor;
		overlay = builder.overlay;
		overlayRatio = builder.overlayRatio;
		overlayImage = builder.overlayImage;
		overlayTransparency = builder.overlayTransparency;
		errorCorrectionLevel = builder.errorCorrectionLevel;
	}

	private BufferedImage encode() throws QrGenerationException {
		BufferedImage encodedImage;
		try {
			encodedImage = renderQRImage(Encoder.encode(data, ErrorCorrectionLevel.H, getEncodeHints()));
			encodedImage = overlay ? setOverlayImage(encodedImage) : encodedImage;
		} catch (Exception e) {
			throw new QrGenerationException("QRCode could not be generated", e);
		}
		return encodedImage;
	}

	private BufferedImage renderQRImage(com.google.zxing.qrcode.encoder.QRCode code) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(qrColor);
		graphics.setBackground(qrBackgroundColor);
		graphics.clearRect(0, 0, width, height);
		ByteMatrix input = code.getMatrix();
		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int qrWidth = inputWidth + (quietZone * 2);
		int qrHeight = inputHeight + (quietZone * 2);
		int outputWidth = Math.max(width, qrWidth);
		int outputHeight = Math.max(height, qrHeight);
		int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
		int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
		int topPadding = (outputHeight - (inputHeight * multiple)) / 2;
		int finderPatternSize = 7;
		float qrScaleDownFactor = 0.95f;
		int qrDotSize = (int) (multiple * qrScaleDownFactor);
		for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple)
			for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple)
				if ((input.get(inputX, inputY) == 1) && (!(inputX <= finderPatternSize && inputY <= finderPatternSize
						|| inputX >= inputWidth - finderPatternSize && inputY <= finderPatternSize
						|| inputX <= finderPatternSize && inputY >= inputHeight - finderPatternSize)))
					graphics.fillOval(outputX, outputY, qrDotSize, qrDotSize);

		int shapeDimension = multiple * finderPatternSize;
		drawFinderPatternStyle(graphics, leftPadding, topPadding, shapeDimension);
		drawFinderPatternStyle(graphics, leftPadding + (inputWidth - finderPatternSize) * multiple, topPadding,
				shapeDimension);
		drawFinderPatternStyle(graphics, leftPadding, topPadding + (inputHeight - finderPatternSize) * multiple,
				shapeDimension);

		return image;
	}

	private void drawFinderPatternStyle(Graphics2D graphics, int x, int y, int shapeDimension) {
		final int outerBorderDiameter = shapeDimension * 5 / 7;
		final int outerBorderOffset = shapeDimension / 7;
		final int medianBorderDiameter = shapeDimension * 3 / 7;
		final int medianBorderOffset = shapeDimension * 2 / 7;
		graphics.setColor(finderPatternOuterColor);
		graphics.fillRect(x, y, shapeDimension, shapeDimension);
		graphics.setColor(finderPatternMedianColor);
		graphics.fillRect(x + outerBorderOffset, y + outerBorderOffset, outerBorderDiameter, outerBorderDiameter);
		graphics.setColor(finderPatternInnerColor);
		graphics.fillRect(x + medianBorderOffset, y + medianBorderOffset, medianBorderDiameter, medianBorderDiameter);
	}

	private Map<EncodeHintType, Object> getEncodeHints() {
		Map<EncodeHintType, Object> encodeHints = new EnumMap<>(EncodeHintType.class);
		encodeHints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
		encodeHints.put(EncodeHintType.CHARACTER_SET, this.charSet.name());
		return encodeHints;
	}

	public BufferedImage setOverlayImage(BufferedImage qrcode) {
		Integer scaledWidth = Math.round(qrcode.getWidth() * overlayRatio);
		Integer scaledHeight = Math.round(qrcode.getHeight() * overlayRatio);
		BufferedImage scaledOverlay = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = scaledOverlay.createGraphics();
		g.drawImage(overlayImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH), 0, 0,
				new Color(0, 0, 0), null);
		g.dispose();
		Integer deltaHeight = qrcode.getHeight() - scaledOverlay.getHeight();
		Integer deltaWidth = qrcode.getWidth() - scaledOverlay.getWidth();
		BufferedImage combined = new BufferedImage(qrcode.getWidth(), qrcode.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) combined.getGraphics();
		g2.drawImage(qrcode, 0, 0, null);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayTransparency));
		g2.drawImage(scaledOverlay, deltaWidth / 2, deltaHeight / 2, null);
		return combined;
	}

	public BufferedImage toImage() throws QrGenerationException {
		return encode();
	}

	public File toFile(String fileName, String fileFormat) throws QrGenerationException {
		ValidationUtils.isTrue(StringUtils.isNotBlank(fileName), QrGenerationException.class, "file name is required");
		ValidationUtils.isTrue(StringUtils.isNotBlank(fileFormat), QrGenerationException.class,
				"file format is required");
		try {
			File imageFile = new File(fileName);
			ImageIO.write(toImage(), fileFormat, imageFile);
			return imageFile;
		} catch (IOException e) {
			throw new QrGenerationException("Could not create file", e);
		}
	}

	public static class QRCodeBuilder {

		/** QR properties with default values **/
		private int width = 500;
		private int height = 500;
		private Charset charSet = StandardCharsets.UTF_8;
		private Float overlayRatio = 0.25f;
		private Float overlayTransparency = 1f;
		private int quietZone = 2;
		private Color qrColor = Color.BLACK.darker().brighter();
		private Color qrBackgroundColor = Color.WHITE.darker().brighter();
		private Color finderPatternOuterColor = Color.BLACK.darker().brighter();
		private Color finderPatternMedianColor = Color.WHITE.darker().brighter();
		private Color finderPatternInnerColor = Color.BLACK.darker().brighter();
		/**/

		private boolean overlay = false;
		private String data;
		private BufferedImage overlayImage;
		private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.H;

		private QRCodeBuilder() {
		}

		public static QRCodeBuilder get() {
			return new QRCodeBuilder();
		}

		public QRCode build() {
			return new QRCode(this);
		}

		public QRCodeBuilder withSize(Integer width, Integer height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public QRCodeBuilder withData(String data) {
			this.data = data;
			return this;
		}

		public QRCodeBuilder withCharSet(Charset charSet) {
			this.charSet = charSet;
			return this;
		}

		public QRCodeBuilder withQuietZone(int quietZone) {
			this.quietZone = quietZone;
			return this;
		}

		public QRCodeBuilder withQrColor(Color qrColor) {
			this.qrColor = qrColor;
			return this;
		}

		public QRCodeBuilder withQrBackgroundColor(Color qrBackgroundColor) {
			this.qrBackgroundColor = qrBackgroundColor;
			return this;
		}

		public QRCodeBuilder withFinderPatternOuterColor(Color finderPatternOuterColor) {
			this.finderPatternOuterColor = finderPatternOuterColor;
			return this;
		}

		public QRCodeBuilder withFinderPatternMedianColor(Color finderPatternMedianColor) {
			this.finderPatternMedianColor = finderPatternMedianColor;
			return this;
		}

		public QRCodeBuilder withFinderPatternInnerColor(Color finderPatternInnerColor) {
			this.finderPatternInnerColor = finderPatternInnerColor;
			return this;
		}

		public QRCodeBuilder withOverlay(BufferedImage overlayImage) {
			this.overlay = true;
			this.overlayImage = overlayImage;
			return this;
		}

		public QRCodeBuilder withOverlayRatio(Float ratio) {
			this.overlayRatio = ratio;
			return this;
		}

		public QRCodeBuilder withOverlayTransparency(Float transparency) {
			this.overlayTransparency = transparency;
			return this;
		}

		public QRCodeBuilder withErrorCorrectionLevel(ErrorCorrectionLevel errorCorrectionLevel) {
			this.errorCorrectionLevel = errorCorrectionLevel;
			return this;
		}

	}
}
