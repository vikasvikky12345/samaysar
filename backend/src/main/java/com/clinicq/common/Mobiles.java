package com.clinicq.common;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

public final class Mobiles {

	private static final Pattern INDIAN_MOBILE = Pattern.compile("^[6-9]\\d{9}$");

	private Mobiles() {
	}

	/** 10-digit Indian mobile → E.164, or 400 invalid_mobile. */
	public static String toE164(String tenDigits) {
		if (tenDigits == null || !INDIAN_MOBILE.matcher(tenDigits).matches()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_mobile", "Enter a valid 10-digit mobile number");
		}
		return "+91" + tenDigits;
	}

	/** For logs: +919876543210 → +91******3210. */
	public static String mask(String e164) {
		if (e164 == null || e164.length() < 7) {
			return "***";
		}
		return e164.substring(0, 3) + "*".repeat(e164.length() - 7) + e164.substring(e164.length() - 4);
	}
}
