package com.e2edocs.repository;

import com.e2edocs.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHashAndUsedFalseAndExpiresAtAfter(String tokenHash, Instant now);
    void deleteByUserId(String userId);
}
