package com.clinicq.common;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		AuthenticationEntryPoint unauthorized = (req, res, ex) -> problem(res, 401, "unauthorized", "Login required");
		AccessDeniedHandler forbidden = (req, res, ex) -> problem(res, 403, "forbidden", "Not allowed for your role");
		// CSRF off: no session; the only cookie (refresh) is SameSite=Strict and scoped to /api/auth.
		http.csrf(c -> c.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(a -> a
				.requestMatchers("/api/auth/**", "/actuator/health", "/error").permitAll()
				.requestMatchers(HttpMethod.PUT, "/api/clinic").hasRole("OWNER")
				.requestMatchers("/api/staff/**").hasRole("OWNER")
				.anyRequest().authenticated())
			.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(roleConverter()))
				.authenticationEntryPoint(unauthorized)
				.accessDeniedHandler(forbidden))
			.exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden));
		return http.build();
	}

	private static JwtAuthenticationConverter roleConverter() {
		JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
		authorities.setAuthoritiesClaimName("role");
		authorities.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(authorities);
		return converter;
	}

	// Constant strings only, so hand-written JSON is safe here.
	private static void problem(HttpServletResponse res, int status, String code, String detail) throws IOException {
		res.setStatus(status);
		res.setContentType("application/problem+json");
		res.getWriter().write("{\"type\":\"/problems/%s\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\",\"code\":\"%s\"}"
			.formatted(code, status == 401 ? "Unauthorized" : "Forbidden", status, detail, code));
	}
}
