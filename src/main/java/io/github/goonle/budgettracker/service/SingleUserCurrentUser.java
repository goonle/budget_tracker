package io.github.goonle.budgettracker.service;

import io.github.goonle.budgettracker.entity.AppUser;
import io.github.goonle.budgettracker.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SingleUserCurrentUser implements CurrentUser {

    private final AppUserRepository appUserRepository;

    @Value("${app.single-user.username:owner}")
    private String username;

    @Override
    public AppUser get() {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Single-user mode: user '" + username + "' not found. Is migration V4 applied?"));
    }
}
