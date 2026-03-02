package com.nox.platform.module.iam.infrastructure;

import com.nox.platform.module.iam.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "security" })
    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludeDeleted(@org.springframework.data.repository.query.Param("email") String email);

    boolean existsByEmail(String email);
}
