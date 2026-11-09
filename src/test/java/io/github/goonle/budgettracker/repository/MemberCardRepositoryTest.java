package io.github.goonle.budgettracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.goonle.budgettracker.TestcontainersConfiguration;
import io.github.goonle.budgettracker.entity.AppUser;
import io.github.goonle.budgettracker.entity.Member;
import io.github.goonle.budgettracker.entity.MemberCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class MemberCardRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired MemberCardRepository repository;

    // ---- findByUserIdAndCardLast4 ----

    @Test
    void findByUserIdAndCardLast4_existingCard_returnsCard() {
        AppUser user = user("jun");
        MemberCard card = card(user, member(user, "Jun"), "1234");

        assertThat(repository.findByUserIdAndCardLast4(user.getId(), "1234"))
                .hasValueSatisfying(found -> assertThat(found.getId()).isEqualTo(card.getId()));
    }

    @Test
    void findByUserIdAndCardLast4_unknownCard_returnsEmpty() {
        AppUser user = user("jun");
        card(user, member(user, "Jun"), "1234");

        assertThat(repository.findByUserIdAndCardLast4(user.getId(), "9999")).isEmpty();
    }

    @Test
    void findByUserIdAndCardLast4_otherUsersCard_returnsEmpty() {
        AppUser owner = user("jun");
        AppUser other = user("other");
        card(owner, member(owner, "Jun"), "1234");

        assertThat(repository.findByUserIdAndCardLast4(other.getId(), "1234")).isEmpty();
    }

    // ---- constraints from V2 ----

    @Test
    void save_sameLast4ForSameUser_isRejected() {
        AppUser user = user("jun");
        Member jun = member(user, "Jun");
        card(user, jun, "1234");

        assertThatThrownBy(() -> repository.saveAndFlush(newCard(user, jun, "1234")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_sameLast4ForDifferentUsers_isAllowed() {
        AppUser a = user("a");
        AppUser b = user("b");
        card(a, member(a, "A"), "1234");

        MemberCard saved = repository.saveAndFlush(newCard(b, member(b, "B"), "1234"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_last4NotFourDigits_isRejected() {
        AppUser user = user("jun");

        assertThatThrownBy(() -> repository.saveAndFlush(newCard(user, member(user, "Jun"), "12a4")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---- helpers ----

    private AppUser user(String username) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPasswordHash("test-hash");
        return em.persistAndFlush(u);
    }

    private Member member(AppUser user, String name) {
        Member m = new Member();
        m.setUser(user);
        m.setName(name);
        return em.persistAndFlush(m);
    }

    private MemberCard card(AppUser user, Member member, String last4) {
        return em.persistAndFlush(newCard(user, member, last4));
    }

    private MemberCard newCard(AppUser user, Member member, String last4) {
        MemberCard c = new MemberCard();
        c.setUser(user);
        c.setMember(member);
        c.setCardLast4(last4);
        return c;
    }
}
