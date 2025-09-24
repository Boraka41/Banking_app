package com.example.banking.service;

import com.example.banking.exception.KeycloakAuthenticationException;
import com.example.banking.dto.responses.UserResponse;
import com.example.banking.entity.User;
import com.example.banking.mapper.UserMapper;
import com.example.banking.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    @PreAuthorize("hasAnyRole('banking_admin', 'banking_user')")
    @Cacheable(value = "user-profiles", key = "#authentication.principal.claims['sub']")
    public UserResponse me(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new KeycloakAuthenticationException("Invalid authentication type");
        }
        String keycloakId = jwt.getClaimAsString("sub");
        log.debug("Cache miss - retrieving user profile for keycloakId: {}", keycloakId);

        User user = userRepo.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new KeycloakAuthenticationException(
                        String.format("User not found with identifier: %s", keycloakId)
                ));

        UserResponse response = userMapper.toResponseDto(user);
        log.info("User profile retrieved from DB and cached for keycloakId: {}", keycloakId);
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @Cacheable(value = "admin-users", key = "'all'")
    public List<UserResponse> listAll() {
        log.debug("Cache miss - admin accessing all users");
        List<User> users = userRepo.findAll();
        List<UserResponse> response = users.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
        log.info("{} users retrieved from DB and cached for admin", users.size());
        return response;
    }

    @PreAuthorize("hasAnyRole('ROLE_banking_admin')")
    @Cacheable(value = "users", key = "#id")
    public UserResponse getById(Long id) {
        log.debug("Cache miss - retrieving user by id: {}", id);
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        UserResponse response = userMapper.toResponseDto(user);
        log.info("User retrieved from DB and cached for id: {}", id);
        return response;
    }

}
