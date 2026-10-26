package com.clinicq.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

interface OtpRepository extends JpaRepository<Otp, UUID> {

	long countByMobileAndCreatedAtAfter(String mobile, Instant since);

	Optional<Otp> findFirstByMobileAndPurposeOrderByCreatedAtDesc(String mobile, String purpose);

	// Atomic so parallel guesses can't exceed the limit; 0 = limit reached.
	@Transactional
	@Modifying
	@Query("update Otp o set o.attempts = o.attempts + 1 where o.id = :id and o.attempts < :max")
	int incrementAttempts(UUID id, int max);

	// 0 = already used (a parallel verify won).
	@Transactional
	@Modifying
	@Query("update Otp o set o.usedAt = :now where o.id = :id and o.usedAt is null")
	int markUsed(UUID id, Instant now);
}
