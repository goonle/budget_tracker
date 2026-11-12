package io.github.goonle.budgettracker.service;

import io.github.goonle.budgettracker.entity.AppUser;

/** Who is using the app right now. Single-user for now; replaced by Spring Security in E6. */
public interface CurrentUser {
    AppUser get();
}
