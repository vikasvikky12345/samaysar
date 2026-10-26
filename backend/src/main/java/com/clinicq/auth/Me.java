package com.clinicq.auth;

import java.time.Instant;
import java.util.UUID;

import com.clinicq.clinic.Clinic;
import com.clinicq.common.Role;
import com.clinicq.staff.AppUser;

record Me(UUID id, String name, String mobile, Role role, ClinicSummary clinic) {

	record ClinicSummary(UUID id, String name, String slug, String planStatus, Instant trialEndsAt) {
	}

	static Me of(AppUser u, Clinic c) {
		return new Me(u.getId(), u.getName(), u.getMobile(), u.getRole(),
				new ClinicSummary(c.getId(), c.getName(), c.getSlug(), c.getPlanStatus(), c.getTrialEndsAt()));
	}
}
