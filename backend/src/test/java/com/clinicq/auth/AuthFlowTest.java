package com.clinicq.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import com.clinicq.IntegrationTest;

class AuthFlowTest extends IntegrationTest {

	private ResultActions refresh(String token) throws Exception {
		return mvc.perform(post("/api/auth/refresh").cookie(new Cookie("sq_refresh", token)));
	}

	private static String refreshCookie(MockHttpServletResponse res) {
		String setCookie = res.getHeader("Set-Cookie");
		assertThat(setCookie).contains("HttpOnly", "Secure", "SameSite=Strict", "Path=/api/auth", "Max-Age=2592000");
		return res.getCookie("sq_refresh").getValue();
	}

	@Test
	void fullFlow() throws Exception {
		String mobile = newMobile();

		// send → verify (new mobile) → SIGNUP_REQUIRED
		String signupBody = verify(mobile, sendOtp(mobile)).andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SIGNUP_REQUIRED"))
			.andExpect(jsonPath("$.accessToken").doesNotExist())
			.andReturn().getResponse().getContentAsString();
		String signupToken = JsonPath.read(signupBody, "$.signupToken");

		// signup token is not an access token
		mvc.perform(get("/api/me").header("Authorization", "Bearer " + signupToken))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("unauthorized"));

		// signup → clinic + OWNER
		MockHttpServletResponse signup = postJson("/api/auth/signup", """
				{"signupToken":"%s","ownerName":"Dr Amit Sharma","clinicName":"Flow Dental","city":"Indore"}"""
			.formatted(signupToken))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.expiresInSeconds").value(900))
			.andExpect(jsonPath("$.user.role").value("OWNER"))
			.andExpect(jsonPath("$.user.mobile").value("+91" + mobile))
			.andExpect(jsonPath("$.user.clinic.planStatus").value("TRIAL"))
			.andReturn().getResponse();
		String access = JsonPath.read(signup.getContentAsString(), "$.accessToken");
		String refresh1 = refreshCookie(signup);

		// access token is not a signup token
		postJson("/api/auth/signup", """
				{"signupToken":"%s","ownerName":"X","clinicName":"Y","city":"Z"}""".formatted(access))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("signup_token_invalid"));

		mvc.perform(get("/api/me").header("Authorization", "Bearer " + access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Dr Amit Sharma"))
			.andExpect(jsonPath("$.clinic.name").value("Flow Dental"));

		// refresh rotates the cookie
		MockHttpServletResponse refreshed = refresh(refresh1).andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andReturn().getResponse();
		String refresh2 = refreshCookie(refreshed);
		assertThat(refresh2).isNotEqualTo(refresh1);

		// reusing the old token → 401 and the whole family is revoked
		refresh(refresh1).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("refresh_invalid"));
		refresh(refresh2).andExpect(status().isUnauthorized());

		// log in again (existing user → LOGGED_IN), then logout revokes and clears the cookie
		MockHttpServletResponse login = verify(mobile, sendOtp(mobile)).andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("LOGGED_IN"))
			.andReturn().getResponse();
		String refresh3 = refreshCookie(login);
		mvc.perform(post("/api/auth/logout").cookie(new Cookie("sq_refresh", refresh3)))
			.andExpect(status().isNoContent())
			.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
		refresh(refresh3).andExpect(status().isUnauthorized());
	}

	@Test
	void refreshWithoutCookieIs401() throws Exception {
		mvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("refresh_invalid"));
	}

	@Test
	void signupValidation() throws Exception {
		postJson("/api/auth/signup", "{\"signupToken\":\"x\",\"ownerName\":\"\",\"clinicName\":\"A\",\"city\":\"B\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));
	}
}
