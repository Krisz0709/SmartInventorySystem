package hu.smartinventory.security;

import hu.smartinventory.user.entity.User;
import hu.smartinventory.user.repository.UserRepository;
import hu.smartinventory.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username
                ));

        String[] authorities =
                userRoleRepository.findAllByUserIdWithRole(user.getId())
                        .stream()
                        .map(userRole -> userRole.getRole().getCode())
                        .toArray(String[]::new);

        if (authorities.length == 0) {
            throw new UsernameNotFoundException(
                    "The user has no assigned role: " + username
            );
        }

        boolean accountLocked =
                user.getLockedUntil() != null
                        && user.getLockedUntil().isAfter(Instant.now());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!user.isActive())
                .accountLocked(accountLocked)
                .build();
    }
}