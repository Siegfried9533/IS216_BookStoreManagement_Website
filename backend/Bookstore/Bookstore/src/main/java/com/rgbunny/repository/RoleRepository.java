package com.rgbunny.repository;

import com.rgbunny.model.AppRole;
import com.rgbunny.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Boolean existsByRoleName(AppRole appRole);

    Optional<Role> findByRoleName(AppRole appRole);
}
