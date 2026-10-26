package com.clinicq.clinic;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicq.common.CurrentUser;

@RestController
@RequestMapping("/api/clinic")
class ClinicController {

	record ClinicResponse(UUID id, String slug, String name, String address, String city, String phone,
			String whatsapp, String mapUrl, String about, String timezone, String planStatus, Instant trialEndsAt) {

		static ClinicResponse of(Clinic c) {
			return new ClinicResponse(c.getId(), c.getSlug(), c.getName(), c.getAddress(), c.getCity(), c.getPhone(),
					c.getWhatsapp(), c.getMapUrl(), c.getAbout(), c.getTimezone(), c.getPlanStatus(),
					c.getTrialEndsAt());
		}
	}

	record ClinicUpdate(
			@NotBlank @Size(max = 120) String name,
			@Size(max = 300) String address,
			@Size(max = 80) String city,
			@Pattern(regexp = "^(\\d{10})?$", message = "must be 10 digits") String phone,
			@Pattern(regexp = "^(\\d{10})?$", message = "must be 10 digits") String whatsapp,
			@Size(max = 500) @Pattern(regexp = "^(https://\\S+)?$", message = "must be an https URL") String mapUrl,
			@Size(max = 1000) String about) {
	}

	private final ClinicService clinics;

	ClinicController(ClinicService clinics) {
		this.clinics = clinics;
	}

	@GetMapping
	ClinicResponse get(@AuthenticationPrincipal Jwt jwt) {
		return ClinicResponse.of(clinics.get(CurrentUser.of(jwt).clinicId()));
	}

	@PutMapping
	ClinicResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ClinicUpdate body) {
		return ClinicResponse.of(clinics.update(CurrentUser.of(jwt).clinicId(), body));
	}
}
