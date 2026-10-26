package com.clinicq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.ThreadLocalRandom;

import com.jayway.jsonpath.JsonPath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.clinicq.auth.OtpSender;

/**
 * Base for tests that need the full app. Runs against a throwaway Testcontainers Postgres
 * (never the .env database) and captures OTP codes instead of logging them.
 */
@SpringBootTest(properties = { "app.jwt.secret=test-only-secret-0123456789abcdefghijklmnop", "app.otp.sender=test" })
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
public abstract class IntegrationTest {

	public static class CapturingOtpSender implements OtpSender {

		private volatile String lastCode;

		@Override
		public void send(String mobile, String code) {
			lastCode = code;
		}

		public String lastCode() {
			return lastCode;
		}
	}

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected CapturingOtpSender otpSender;

	/** Fresh 10-digit mobile per test so tests sharing the DB don't collide. */
	protected static String newMobile() {
		return "9" + ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
	}

	protected ResultActions postJson(String url, String json) throws Exception {
		return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
	}

	protected String sendOtp(String mobile) throws Exception {
		postJson("/api/auth/otp/send", "{\"mobile\":\"%s\"}".formatted(mobile)).andExpect(status().isAccepted());
		return otpSender.lastCode();
	}

	protected ResultActions verify(String mobile, String code) throws Exception {
		return postJson("/api/auth/otp/verify", "{\"mobile\":\"%s\",\"code\":\"%s\"}".formatted(mobile, code));
	}

	/** Signs up a new clinic owner; returns the access token. */
	protected String signupOwner(String mobile, String clinicName, String city) throws Exception {
		String signupToken = JsonPath.read(verify(mobile, sendOtp(mobile)).andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SIGNUP_REQUIRED"))
			.andReturn().getResponse().getContentAsString(), "$.signupToken");
		String body = postJson("/api/auth/signup", """
				{"signupToken":"%s","ownerName":"Dr Owner","clinicName":"%s","city":"%s"}"""
			.formatted(signupToken, clinicName, city)).andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}

	/** Logs in an existing user via OTP; returns the access token. */
	protected String login(String mobile) throws Exception {
		String body = verify(mobile, sendOtp(mobile)).andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("LOGGED_IN"))
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}
}

@TestConfiguration(proxyBeanMethods = false)
class IntegrationTestConfig {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgres() {
		return new PostgreSQLContainer("postgres:18-alpine");
	}

	@Bean
	IntegrationTest.CapturingOtpSender otpSender() {
		return new IntegrationTest.CapturingOtpSender();
	}
}
