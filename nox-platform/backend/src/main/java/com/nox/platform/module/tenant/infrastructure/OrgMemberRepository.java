package com.nox.platform.module.tenant.infrastructure;

import com.nox.platform.module.tenant.domain.OrgMember;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgMemberRepository extends JpaRepository<OrgMember, UUID> {

    Page<OrgMember> findByOrganizationId(UUID orgId, Pageable pageable);

    long countByOrganizationIdAndRoleName(UUID orgId, String roleName);

    List<OrgMember> findByUserId(UUID userId);

    @Cacheable(value = "org_members", key = "#orgId + '-' + #userId")
    Optional<OrgMember> findByOrganizationIdAndUserId(UUID orgId, UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID orgId, UUID userId);
}
