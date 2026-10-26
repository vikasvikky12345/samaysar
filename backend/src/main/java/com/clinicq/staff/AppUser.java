package com.clinicq.staff;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import com.clinicq.common.Role;

@Entity
public class AppUser {

	@Id
	@GeneratedValue
	private UUID id;
	private UUID clinicId;
	private String mobile;
	private String name;
	@Enumerated(EnumType.STRING)
	private Role role;
	private boolean active = true;
	private Instant createdAt = Instant.now();

	protected AppUser() {
	}

	public AppUser(UUID clinicId, String mobile, String name, Role role) {
		this.clinicId = clinicId;
		this.mobile = mobile;
		this.name = name;
		this.role = role;
	}

	void setActive(boolean active) { this.active = active; }
	void setRole(Role role) { this.role = role; }

	public UUID getId() { return id; }
	public UUID getClinicId() { return clinicId; }
	public String getMobile() { return mobile; }
	public String getName() { return name; }
	public Role getRole() { return role; }
	public boolean isActive() { return active; }
	public Instant getCreatedAt() { return createdAt; }
}
