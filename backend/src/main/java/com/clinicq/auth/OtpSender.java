package com.clinicq.auth;

/** Delivers an OTP code to an E.164 mobile. */
public interface OtpSender {

	void send(String mobile, String code);
}
