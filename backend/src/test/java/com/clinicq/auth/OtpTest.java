package com.clinicq.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.clinicq.IntegrationTest;

class OtpTest extends IntegrationTest {

	@Autowired
	JdbcTemplate jdbc;

	private int attempts(String mobile) {
		return jdbc.queryForObject("select attempts from otp where mobile = ? order by created_at desc limit 1",
				Integer.class, "+91" + mobile);
	}

	private static String wrong(String code) {
		return code.equals("000000") ? "111111" : "000000";
	}

	@Test
	void wrongCodeIncrementsAttemptsAndSixthIsRejected() throws Exception {
		String mobile = newMobile();
		String code = sendOtp(mobile);
		for (int i = 1; i <= 5; i++) {
			verify(mobile, wrong(code)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("otp_invalid"));
			assertThat(attempts(mobile)).isEqualTo(i);
		}
		// Even the right code is refused once the attempts are used up.
		verify(mobile, code).andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("otp_attempts_exceeded"));
	}

	@Test
	void expiredCodeIsInvalid() throws Exception {
		String mobile = newMobile();
		String code = sendOtp(mobile);
		jdbc.update("update otp set expires_at = now() - interval '1 second' where mobile = ?", "+91" + mobile);
		verify(mobile, code).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("otp_invalid"));
	}

	@Test
	void usedCodeCannotBeReplayed() throws Exception {
		String mobile = newMobile();
		String code = sendOtp(mobile);
		verify(mobile, code).andExpect(status().isOk());
		verify(mobile, code).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("otp_invalid"));
	}

	@Test
	void fourthSendWithinWindowIsRateLimited() throws Exception {
		String mobile = newMobile();
		sendOtp(mobile);
		sendOtp(mobile);
		sendOtp(mobile);
		postJson("/api/auth/otp/send", "{\"mobile\":\"%s\"}".formatted(mobile))
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("otp_rate_limited"));
	}

	@Test
	void invalidMobileRejected() throws Exception {
		postJson("/api/auth/otp/send", "{\"mobile\":\"12345\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("invalid_mobile"));
	}
}
