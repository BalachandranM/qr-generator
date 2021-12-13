package com.qr.generator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.qr.generator.exceptions.QrGenerationException;
import com.qr.generator.utils.ValidationUtils;

public class QRCode {
	public enum Shape {
		RECTANGLE, OVAL, ARC, ROUNDED_RECTANGLE
	}

	private int width;
	private int height;
	private int quietZone;
	private Color qrColor;
	private Color qrBackgroundColor;
	private Color finderPatternOuterColor;
	private Color finderPatternMedianColor;
	private Color finderPatternInnerColor;
	private int finderPatternSize;
	private float qrScaleDownFactor;
	private String data;
	private Map<EncodeHintType, Object> encodingHints;
	private Shape qrShape;
	private Shape finderPatternShape;
	private int arcDegree = 150;
	private boolean overlay = false;
	private Float overlayRatio;
	private Float overlayTransparency;
	private byte[] overlayImage;

	private BufferedImage generatedImage;

	private QRCode(Builder builder) {
		ValidationUtils.isTrue(builder.encodingHints != null, QrGenerationException.class,
				"A valid encoding hints is needed");
		encodingHints = builder.encodingHints;
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
		finderPatternSize = builder.finderPatternSize;
		qrScaleDownFactor = builder.qrScaleDownFactor;
		qrShape = builder.qrShape;
		finderPatternShape = builder.finderPatternShape;
	}

	private BufferedImage encode() throws QrGenerationException {
		BufferedImage encodedImage;
		try {
//			BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height, encodingHints);
//			encodedImage = MatrixToImageWriter.toBufferedImage(matrix);
			encodedImage = renderQRImage(Encoder.encode(data, (ErrorCorrectionLevel) encodingHints
					.getOrDefault(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H), encodingHints));
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
		int qrDotSize = (int) (multiple * qrScaleDownFactor);
		for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple)
			for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple)
				if ((input.get(inputX, inputY) == 1) && (!(inputX <= finderPatternSize && inputY <= finderPatternSize
						|| inputX >= inputWidth - finderPatternSize && inputY <= finderPatternSize
						|| inputX <= finderPatternSize && inputY >= inputHeight - finderPatternSize)))
					setShape(graphics, qrShape, outputX, outputY, qrDotSize, qrDotSize);
		int shapeDimension = multiple * finderPatternSize;
		drawFinderPatternStyle(graphics, leftPadding, topPadding, shapeDimension);
		drawFinderPatternStyle(graphics, leftPadding + (inputWidth - finderPatternSize) * multiple, topPadding,
				shapeDimension);
		drawFinderPatternStyle(graphics, leftPadding, topPadding + (inputHeight - finderPatternSize) * multiple,
				shapeDimension);
		return image;
	}

	private void setShape(Graphics2D graphics, Shape shape, int x, int y, int width, int height) {
		switch (shape) {
		case RECTANGLE:
			graphics.fillRect(x, y, width, height);
			break;
		case OVAL:
			graphics.fillOval(x, y, width, height);
			break;
		case ARC:
			graphics.fillArc(x, y, width, height, arcDegree, arcDegree);
			break;
		case ROUNDED_RECTANGLE:
			graphics.fillRoundRect(x, y, width, height, arcDegree, arcDegree);
			break;
		default:
			graphics.fillRect(x, y, width, height);
			break;
		}
	}

	private void drawFinderPatternStyle(Graphics2D graphics, int x, int y, int shapeDimension) {
		final int outerBorderDiameter = shapeDimension * 5 / 7;
		final int outerBorderOffset = shapeDimension / 7;
		final int medianBorderDiameter = shapeDimension * 3 / 7;
		final int medianBorderOffset = shapeDimension * 2 / 7;
		graphics.setColor(finderPatternOuterColor);
		setShape(graphics, finderPatternShape, x, y, shapeDimension, shapeDimension);
		graphics.setColor(finderPatternMedianColor);
		setShape(graphics, finderPatternShape, x + outerBorderOffset, y + outerBorderOffset, outerBorderDiameter,
				outerBorderDiameter);
		graphics.setColor(finderPatternInnerColor);
		setShape(graphics, finderPatternShape, x + medianBorderOffset, y + medianBorderOffset, medianBorderDiameter,
				medianBorderDiameter);
	}

	public BufferedImage setOverlayImage(BufferedImage qrcode) throws QrGenerationException {
		BufferedImage overlayResult = null;
		try {
			InputStream is = new ByteArrayInputStream(overlayImage);
			BufferedImage newBi;
			newBi = ImageIO.read(is);
			Integer scaledWidth = Math.round(qrcode.getWidth() * overlayRatio);
			Integer scaledHeight = Math.round(qrcode.getHeight() * overlayRatio);
			BufferedImage scaledOverlay = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics g = scaledOverlay.createGraphics();
			g.drawImage(newBi.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH), 0, 0,
					new Color(0, 0, 0), null);
			g.dispose();
			Integer deltaHeight = qrcode.getHeight() - scaledOverlay.getHeight();
			Integer deltaWidth = qrcode.getWidth() - scaledOverlay.getWidth();
			overlayResult = new BufferedImage(qrcode.getWidth(), qrcode.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) overlayResult.getGraphics();
			g2.drawImage(qrcode, 0, 0, null);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayTransparency));
			g2.drawImage(scaledOverlay, deltaWidth / 2, deltaHeight / 2, null);
		} catch (IOException e) {
			throw new QrGenerationException("Unable to embed the logo in the QR", e);
		}
		return overlayResult;
	}

	public byte[] toBytes(String fileFormat) throws QrGenerationException {
		fileFormat = StringUtils.isBlank(fileFormat) ? "png" : fileFormat;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(toImage(), fileFormat, baos);
		} catch (QrGenerationException e) {
			throw e;
		} catch (IOException e) {
			throw new QrGenerationException("Unable to generate the QR in bytes", e);
		}
		return baos.toByteArray();
	}

	public BufferedImage toImage() throws QrGenerationException {
		if (this.generatedImage == null)
			this.generatedImage = encode();
		return this.generatedImage;
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

	public QRCode verify() throws QrGenerationException {
		try {
			Result actualData = decode(toImage());
			if (actualData != null && !actualData.getText().equals(this.data)) {
				throw new QrGenerationException(
						"The data contained in the qrCode is not as expected: " + this.data + " actual: " + actualData);
			}
		} catch (Exception e) {
			throw new QrGenerationException("Verifying qr code failed!", e);
		}
		return this;
	}

	private Result decode(BufferedImage qrcode) throws NotFoundException, ChecksumException, FormatException {
		return new QRCodeReader().decode(
				new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(qrcode))), getDecodeHints());
	}

	private Map<DecodeHintType, Object> getDecodeHints() {
		Map<DecodeHintType, Object> decodeHints = new EnumMap<>(DecodeHintType.class);
		decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
		decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		decodeHints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		return decodeHints;
	}

	public static class Builder {

		/** QR properties with default values **/
		private Map<EncodeHintType, Object> encodingHints = defaultEncodeHints();
		private int width = 100;
		private int height = 100;
		private Float overlayRatio = 0.25f;
		private Float overlayTransparency = 1f;
		private int quietZone = 1;
		private Color qrColor = Color.BLACK.darker().brighter();
		private Color qrBackgroundColor = Color.WHITE.darker().brighter();
		private Color finderPatternOuterColor = Color.BLACK.darker().brighter();
		private Color finderPatternMedianColor = Color.WHITE.darker().brighter();
		private Color finderPatternInnerColor = Color.BLACK.darker().brighter();
		private int finderPatternSize = 15;
		private float qrScaleDownFactor = 0.95f;
		private Shape qrShape = Shape.RECTANGLE;
		private Shape finderPatternShape = Shape.RECTANGLE;
		/**/

		private boolean overlay = false;
		private String data;
		private byte[] overlayImage;

		private Builder() {
		}

		public static Builder get() {
			return new Builder();
		}

		public QRCode build() {
			return new QRCode(this);
		}

		public Builder withSize(Integer width, Integer height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Builder withData(String data) {
			this.data = data;
			return this;
		}

		public Builder withQuietZone(int quietZone) {
			this.quietZone = quietZone;
			return this;
		}

		public Builder withQrColor(Color qrColor) {
			this.qrColor = qrColor;
			return this;
		}

		public Builder withQrBackgroundColor(Color qrBackgroundColor) {
			this.qrBackgroundColor = qrBackgroundColor;
			return this;
		}

		public Builder withFinderPatternOuterColor(Color finderPatternOuterColor) {
			this.finderPatternOuterColor = finderPatternOuterColor;
			return this;
		}

		public Builder withFinderPatternMedianColor(Color finderPatternMedianColor) {
			this.finderPatternMedianColor = finderPatternMedianColor;
			return this;
		}

		public Builder withFinderPatternInnerColor(Color finderPatternInnerColor) {
			this.finderPatternInnerColor = finderPatternInnerColor;
			return this;
		}

		public Builder withOverlay(byte[] overlayImage) {
			this.overlay = true;
			this.overlayImage = overlayImage;
			return this;
		}

		public Builder withOverlayRatio(Float ratio) {
			this.overlayRatio = ratio;
			return this;
		}

		public Builder withOverlayTransparency(Float transparency) {
			this.overlayTransparency = transparency;
			return this;
		}

		public Builder withQrScaleDownFactor(Float scaleDownFactor) {
			this.qrScaleDownFactor = scaleDownFactor;
			return this;
		}

		public Builder withFinderPatternSize(int finderPatternSize) {
			this.finderPatternSize = finderPatternSize;
			return this;
		}

		public Builder withQrShape(Shape shape) {
			this.qrShape = shape;
			return this;
		}

		public Builder withFinderPatternShape(Shape shape) {
			this.finderPatternShape = shape;
			return this;
		}

		public Builder withEncodingHints(Map<EncodeHintType, Object> encodingHints) {
			this.encodingHints = encodingHints;
			return this;
		}

		private Map<EncodeHintType, Object> defaultEncodeHints() {
			Map<EncodeHintType, Object> encodeHints = new EnumMap<>(EncodeHintType.class);
			encodeHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			encodeHints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
			return encodeHints;
		}
	}
}
