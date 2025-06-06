package com.rgbunny.config;

import com.rgbunny.repository.RoleRepository;
import com.rgbunny.model.AppRole;
import com.rgbunny.model.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists(AppRole.ROLE_ADMIN);
        createRoleIfNotExists(AppRole.ROLE_USER);
        createRoleIfNotExists(AppRole.ROLE_EMPLOYEE);
    }

    private void createRoleIfNotExists(AppRole roleName) {
        if (!roleRepository.existsByRoleName(roleName)) {
            Role role = new Role(roleName);
            roleRepository.save(role);
        }
    }
}
