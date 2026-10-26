package com.clinicq.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.clinicq.common.Mobiles;

// shortcut: logs OTP codes, replace with MSG91 DLT sender before any real users
@Component
@ConditionalOnProperty(name = "app.otp.sender", havingValue = "log", matchIfMissing = true)
class LogOtpSender implements OtpSender {

	private static final Logger log = LoggerFactory.getLogger(LogOtpSender.class);

	@Override
	public void send(String mobile, String code) {
		log.warn("OTP for {}: {}", Mobiles.mask(mobile), code);
	}
}
