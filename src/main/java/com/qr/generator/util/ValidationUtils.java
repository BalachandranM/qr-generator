package com.qr.generator.util;

import java.lang.reflect.InvocationTargetException;

import com.qr.generator.exception.QrGenerationException;

public class ValidationUtils {

	private ValidationUtils() {
		super();
	}

	public static <T extends QrGenerationException> void isTrue(boolean expression, Class<T> exceptionType,
			String message) {
		if (!expression)
			try {
				throw exceptionType.getConstructor(String.class).newInstance(message);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | QrGenerationException e) {
				e.printStackTrace();
			}
	}

}
