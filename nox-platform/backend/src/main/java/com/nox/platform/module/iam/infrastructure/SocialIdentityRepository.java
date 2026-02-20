package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.SocialIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialIdentityRepository extends JpaRepository<SocialIdentity, UUID> {
    Optional<SocialIdentity> findByProviderAndProviderId(String provider, String providerId);

    Page<SocialIdentity> findByUserId(UUID userId, Pageable pageable);
}
