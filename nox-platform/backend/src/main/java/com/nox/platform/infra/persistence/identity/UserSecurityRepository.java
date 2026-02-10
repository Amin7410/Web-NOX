package com.nox.platform.infra.persistence.identity;

import com.nox.platform.core.identity.model.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, UUID> {
}
