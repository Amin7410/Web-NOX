package com.nox.platform.module.tenant.service;

import java.text.Normalizer;
import java.util.Locale;
import com.nox.platform.module.iam.domain.User;
import com.nox.platform.module.iam.infrastructure.UserRepository;
import com.nox.platform.module.tenant.domain.OrgMember;
import com.nox.platform.module.tenant.domain.Organization;
import com.nox.platform.module.tenant.domain.Role;
import com.nox.platform.module.tenant.infrastructure.OrgMemberRepository;
import com.nox.platform.module.tenant.infrastructure.OrganizationRepository;
import com.nox.platform.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final RoleService roleService;
    private final OrgMemberRepository orgMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public Organization createOrganization(String name, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Creator user could not be found", 404));

        String slug = generateUniqueSlug(name);

        Organization organization = Organization.builder()
                .name(name)
                .slug(slug)
                .settings(Map.of("theme", "system"))
                .build();
        organization = organizationRepository.save(organization);

        Role ownerRole = roleService.createRole(organization, "OWNER", List.of("*"));
        roleService.createRole(organization, "ADMIN", List.of("iam:manage", "billing:manage", "workspace:manage"));
        roleService.createRole(organization, "MEMBER", List.of("workspace:read"));

        OrgMember ownerMember = OrgMember.builder()
                .organization(organization)
                .user(creator)
                .role(ownerRole)
                .invitedBy(creator) // Self-invited essentially
                .build();
        orgMemberRepository.save(ownerMember);

        return organization;
    }

    public Organization getOrganizationById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found", 404));
    }

    public Organization getOrganizationBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainException("ORG_NOT_FOUND", "Organization not found", 404));
    }

    @Transactional
    public Organization updateOrganization(UUID orgId, String name, Map<String, Object> settings) {
        Organization org = getOrganizationById(orgId);

        if (name != null && !name.isBlank() && !org.getName().equals(name)) {
            org.setName(name);
            org.setSlug(generateUniqueSlug(name));
        }

        if (settings != null) {
            org.setSettings(settings);
        }

        return organizationRepository.save(org);
    }

    @Transactional
    public void deleteOrganization(UUID orgId) {
        Organization org = getOrganizationById(orgId);
        org.softDelete();
        organizationRepository.save(org);
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = toSlug(name);
        String finalSlug = baseSlug;
        int counter = 1;

        while (organizationRepository.existsBySlug(finalSlug)) {
            String randomHash = UUID.randomUUID().toString().substring(0, 6);
            finalSlug = baseSlug + "-" + randomHash;
            counter++;
            if (counter > 20) {
                throw new DomainException("SLUG_GENERATION_FAILED",
                        "Failed to generate a unique slug after 20 attempts.", 500);
            }
        }
        return finalSlug;
    }

    private String toSlug(String input) {
        if (input == null)
            return "";
        String nonLatin = "[^\\w-]";
        String whiteSpace = "[\\s]";
        String nowhitespace = input.replaceAll(whiteSpace, "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll(nonLatin, "");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
}
