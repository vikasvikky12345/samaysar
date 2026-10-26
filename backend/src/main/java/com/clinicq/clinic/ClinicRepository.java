package com.clinicq.clinic;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, UUID> {

	boolean existsBySlug(String slug);
}
