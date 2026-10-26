package com.clinicq.staff;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.clinicq.common.CurrentUser;
import com.clinicq.common.Role;

@RestController
@RequestMapping("/api/staff")
class StaffController {

	record StaffResponse(UUID id, String name, String mobile, Role role, boolean active, Instant createdAt) {

		static StaffResponse of(AppUser u) {
			return new StaffResponse(u.getId(), u.getName(), u.getMobile(), u.getRole(), u.isActive(), u.getCreatedAt());
		}
	}

	record NewStaff(@NotBlank @Size(max = 120) String name, String mobile, @NotNull Role role) {
	}

	record StaffPatch(Boolean active, Role role) {
	}

	private final StaffService staff;

	StaffController(StaffService staff) {
		this.staff = staff;
	}

	@GetMapping
	List<StaffResponse> list(@AuthenticationPrincipal Jwt jwt) {
		return staff.list(CurrentUser.of(jwt).clinicId()).stream().map(StaffResponse::of).toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	StaffResponse add(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NewStaff body) {
		return StaffResponse.of(staff.add(CurrentUser.of(jwt).clinicId(), body));
	}

	@PatchMapping("/{id}")
	StaffResponse patch(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @RequestBody StaffPatch body) {
		return StaffResponse.of(staff.patch(CurrentUser.of(jwt), id, body));
	}
}
