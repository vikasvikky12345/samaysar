package com.clinicq.staff;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinicq.common.ApiException;
import com.clinicq.common.CurrentUser;
import com.clinicq.common.Mobiles;
import com.clinicq.common.Role;

@Service
class StaffService {

	private final AppUserRepository users;

	StaffService(AppUserRepository users) {
		this.users = users;
	}

	List<AppUser> list(UUID clinicId) {
		return users.findByClinicIdOrderByCreatedAt(clinicId);
	}

	@Transactional
	AppUser add(UUID clinicId, StaffController.NewStaff s) {
		String mobile = Mobiles.toE164(s.mobile());
		requireStaffRole(s.role());
		if (users.existsByMobile(mobile)) {
			throw new ApiException(HttpStatus.CONFLICT, "mobile_already_registered", "This mobile is already registered");
		}
		return users.save(new AppUser(clinicId, mobile, s.name().strip(), s.role()));
	}

	@Transactional
	AppUser patch(CurrentUser me, UUID id, StaffController.StaffPatch p) {
		AppUser user = users.findByIdAndClinicId(id, me.clinicId()).orElseThrow(ApiException::notFound);
		if (user.getId().equals(me.userId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "cannot_modify_self", "You cannot change your own account");
		}
		if (p.role() != null) {
			requireStaffRole(p.role());
			user.setRole(p.role());
		}
		if (p.active() != null) {
			user.setActive(p.active());
		}
		return user;
	}

	private static void requireStaffRole(Role role) {
		if (role != Role.DOCTOR && role != Role.RECEPTION) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "validation_failed", "role must be DOCTOR or RECEPTION");
		}
	}
}
