package com.clinicq.clinic;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicq.common.ApiException;

@Service
public class ClinicService {

	private static final Duration TRIAL = Duration.ofDays(14);

	private final ClinicRepository clinics;

	ClinicService(ClinicRepository clinics) {
		this.clinics = clinics;
	}

	/** Caller owns the transaction (signup creates clinic + owner together). */
	public Clinic create(String name, String city) {
		String slug = SlugGenerator.generate(name, city, clinics::existsBySlug);
		return clinics.save(new Clinic(slug, name, city, Instant.now().plus(TRIAL)));
	}

	public Clinic get(UUID clinicId) {
		return clinics.findById(clinicId).orElseThrow(ApiException::notFound);
	}

	@Transactional
	public Clinic update(UUID clinicId, ClinicController.ClinicUpdate u) {
		Clinic clinic = get(clinicId);
		clinic.update(u.name().strip(), blankToNull(u.address()), blankToNull(u.city()), blankToNull(u.phone()),
				blankToNull(u.whatsapp()), blankToNull(u.mapUrl()), blankToNull(u.about()));
		return clinic;
	}

	private static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s.strip();
	}
}
