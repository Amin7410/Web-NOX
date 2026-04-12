package com.nox.platform.module.tenant.service;

import java.time.OffsetDateTime;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.module.tenant.service.command.CreateOrganizationCommand;
import com.nox.platform.module.tenant.service.command.UpdateOrganizationCommand;
import com.nox.platform.shared.abstraction.SlugGenerator;
import com.nox.platform.shared.abstraction.TimeProvider;
import com.nox.platform.shared.event.OrganizationCreatedEvent;
import com.nox.platform.shared.event.OrganizationDeletedEvent;
import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.infrastructure.aspect.AuditTargetOrg;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final RoleService roleService;
    private final OrgMemberService orgMemberService;
    private final UserRepository userRepository;
    private final TimeProvider timeProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final SlugGenerator slugGenerator;

    @Transactional
    public Organization createOrganization(CreateOrganizationCommand command) {
        User creator = userRepository.findByEmail(command.creatorEmail())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Creator user not found"));

        Organization organization = Organization.create(
                command.name(),
                generateUniqueSlug(command.name()),
                timeProvider.now()
        );
        organization = organizationRepository.save(organization);

        roleService.provisionDefaultRoles(organization);
        orgMemberService.provisionInitialOwner(organization, creator);

        eventPublisher.publishEvent(new OrganizationCreatedEvent(organization.getId(), creator.getId()));

        return organization;
    }

    @Transactional(readOnly = true)
    public List<Organization> getOrganizationsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

        return orgMemberService.getOrganizationsForUser(user.getId())
                .stream()
                .map(OrgMember::getOrganization)
                .collect(Collectors.toList());
    }

    public Organization getOrganizationById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found"));
    }

    public Organization getOrganizationBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found"));
    }

    @Transactional
    public Organization updateOrganization(UpdateOrganizationCommand command) {
        Organization org = getOrganizationById(command.orgId());

        if (command.name() != null && !command.name().isBlank() && !org.getName().equals(command.name())) {
            org.updateMetadata(command.name(), generateUniqueSlug(command.name()));
        }

        if (command.settings() != null) {
            org.updateSettings(command.settings());
        }

        org.updateTimestamp(timeProvider.now());
        return organizationRepository.save(org);
    }

    @Transactional
    public void deleteOrganization(@AuditTargetOrg UUID orgId) {
        Organization org = getOrganizationById(orgId);
        OffsetDateTime now = timeProvider.now();
        org.softDelete(now);
        org.updateTimestamp(now);
        organizationRepository.save(org);

        orgMemberService.softDeleteByOrgId(orgId, now);
        roleService.softDeleteByOrgId(orgId, now);

        eventPublisher.publishEvent(new OrganizationDeletedEvent(orgId));
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = slugGenerator.generate(name);
        String finalSlug = baseSlug;
        int counter = 1;

        while (organizationRepository.existsBySlug(finalSlug)) {
            String randomHash = UUID.randomUUID().toString().substring(0);
            finalSlug = baseSlug + "-" + randomHash;
            counter++;
            if (counter > 20) {
                throw new DomainException("SLUG_GENERATION_FAILED", "Failed to generate a unique slug after 20 attempts.");
            }
        }
        return finalSlug;
    }
}


