package hu.smartinventory.user.bootstrap;

import hu.smartinventory.config.AdminBootstrapProperties;
import hu.smartinventory.user.entity.Role;
import hu.smartinventory.user.entity.User;
import hu.smartinventory.user.entity.UserRole;
import hu.smartinventory.user.entity.UserRoleId;
import hu.smartinventory.user.repository.RoleRepository;
import hu.smartinventory.user.repository.UserRepository;
import hu.smartinventory.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevAdminInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE_CODE = "ROLE_ADMIN";

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            log.info("Development admin bootstrap is disabled.");
            return;
        }

        String username = requireText(
                properties.getUsername(),
                "ADMIN_USERNAME must not be empty."
        );

        Role adminRole = roleRepository.findByCode(ADMIN_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        ADMIN_ROLE_CODE + " role was not found."
                ));

        User admin = userRepository.findByUsername(username)
                .orElse(null);

        if (admin == null) {
            String password = requireText(
                    properties.getPassword(),
                    "ADMIN_PASSWORD must be set."
            );

            String email = normalize(properties.getEmail());

            if (email != null && userRepository.existsByEmail(email)) {
                throw new IllegalStateException(
                        "The configured admin email is already in use."
                );
            }

            admin = new User(
                    username,
                    email,
                    passwordEncoder.encode(password),
                    normalize(properties.getFullName())
            );

            admin = userRepository.saveAndFlush(admin);

            log.info("Development admin user created: {}", username);
        } else {
            log.info("Development admin user already exists: {}", username);
        }

        UserRoleId userRoleId = new UserRoleId(
                admin.getId(),
                adminRole.getId()
        );

        if (!userRoleRepository.existsById(userRoleId)) {
            UserRole userRole = new UserRole(
                    admin,
                    adminRole,
                    null
            );

            userRoleRepository.save(userRole);

            log.info(
                    "{} role assigned to user {}.",
                    ADMIN_ROLE_CODE,
                    username
            );
        }
    }

    private static String requireText(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }

        return value.trim();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}