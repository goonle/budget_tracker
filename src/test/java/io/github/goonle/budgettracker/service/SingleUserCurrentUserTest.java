package io.github.goonle.budgettracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.goonle.budgettracker.entity.AppUser;
import io.github.goonle.budgettracker.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SingleUserCurrentUserTest {

    @Mock AppUserRepository appUserRepository;
    @InjectMocks SingleUserCurrentUser currentUser;

    @BeforeEach
    void setUp() {
        // @Value isn't filled without a Spring context, so set it by hand.
        ReflectionTestUtils.setField(currentUser, "username", "owner");
    }

    @Test
    void get_userExists_returnsUser() {
        AppUser owner = new AppUser();
        owner.setUsername("owner");
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.of(owner));

        assertThat(currentUser.get()).isSameAs(owner);
    }

    @Test
    void get_userMissing_throwsWithHelpfulMessage() {
        when(appUserRepository.findByUsername("owner")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUser.get())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("V4");
    }
}
