package com.qr.generator.exceptions;

public class QrGenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public QrGenerationException() {
		super();
	}

	public QrGenerationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public QrGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public QrGenerationException(String message) {
		super(message);
	}

	public QrGenerationException(Throwable cause) {
		super(cause);
	}

}
