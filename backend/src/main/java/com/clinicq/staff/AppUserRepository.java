package com.clinicq.staff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

	Optional<AppUser> findByMobile(String mobile);

	boolean existsByMobile(String mobile);

	Optional<AppUser> findByIdAndClinicId(UUID id, UUID clinicId);

	List<AppUser> findByClinicIdOrderByCreatedAt(UUID clinicId);
}
