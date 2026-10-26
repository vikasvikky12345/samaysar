package com.clinicq.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.clinicq.common.CurrentUser;

@RestController
class AuthController {

	static final String REFRESH_COOKIE = "sq_refresh";
	private static final long ACCESS_SECONDS = TokenService.ACCESS_TTL.toSeconds();

	record SendOtpRequest(String mobile) {
	}

	record SendOtpResponse(long expiresInSeconds) {
	}

	record VerifyOtpRequest(String mobile, String code) {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	record VerifyOtpResponse(String status, String accessToken, Long expiresInSeconds, Me user, String signupToken) {
	}

	record SignupRequest(@NotBlank String signupToken, @NotBlank @Size(max = 120) String ownerName,
			@NotBlank @Size(max = 120) String clinicName, @NotBlank @Size(max = 80) String city) {
	}

	record AuthResponse(String accessToken, long expiresInSeconds, Me user) {
	}

	private final AuthService auth;

	AuthController(AuthService auth) {
		this.auth = auth;
	}

	@PostMapping("/api/auth/otp/send")
	ResponseEntity<SendOtpResponse> sendOtp(@RequestBody SendOtpRequest body) {
		auth.sendOtp(body.mobile());
		return ResponseEntity.accepted().body(new SendOtpResponse(OtpService.TTL.toSeconds()));
	}

	@PostMapping("/api/auth/otp/verify")
	ResponseEntity<VerifyOtpResponse> verifyOtp(@RequestBody VerifyOtpRequest body) {
		AuthService.VerifyResult result = auth.verifyOtp(body.mobile(), body.code());
		if (result.session() == null) {
			return ResponseEntity.ok(new VerifyOtpResponse("SIGNUP_REQUIRED", null, null, null, result.signupToken()));
		}
		AuthService.Session s = result.session();
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshCookie(s.refreshToken(), AuthService.REFRESH_TTL.toSeconds()))
			.body(new VerifyOtpResponse("LOGGED_IN", s.accessToken(), ACCESS_SECONDS, s.user(), null));
	}

	@PostMapping("/api/auth/signup")
	ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest body) {
		AuthService.Session s = auth.signup(body.signupToken(), body.ownerName(), body.clinicName(), body.city());
		return withCookie(ResponseEntity.status(HttpStatus.CREATED), s);
	}

	@PostMapping("/api/auth/refresh")
	ResponseEntity<AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String token) {
		return withCookie(ResponseEntity.ok(), auth.refresh(token));
	}

	@PostMapping("/api/auth/logout")
	ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String token) {
		auth.logout(token);
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookie("", 0)).build();
	}

	@GetMapping("/api/me")
	Me me(@AuthenticationPrincipal Jwt jwt) {
		return auth.me(CurrentUser.of(jwt));
	}

	private static ResponseEntity<AuthResponse> withCookie(ResponseEntity.BodyBuilder builder, AuthService.Session s) {
		return builder.header(HttpHeaders.SET_COOKIE, refreshCookie(s.refreshToken(), AuthService.REFRESH_TTL.toSeconds()))
			.body(new AuthResponse(s.accessToken(), ACCESS_SECONDS, s.user()));
	}

	private static String refreshCookie(String value, long maxAgeSeconds) {
		return ResponseCookie.from(REFRESH_COOKIE, value)
			.httpOnly(true)
			.secure(true)
			.sameSite("Strict")
			.path("/api/auth")
			.maxAge(maxAgeSeconds)
			.build()
			.toString();
	}
}
