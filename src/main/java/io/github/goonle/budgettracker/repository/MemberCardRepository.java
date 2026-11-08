package io.github.goonle.budgettracker.repository;

import io.github.goonle.budgettracker.entity.MemberCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberCardRepository extends JpaRepository<MemberCard, Long> {
    Optional<MemberCard> findByUserIdAndCardLast4(Long userId, String cardLast4);
    Optional<MemberCard> findByUserId(String cardLast4);
}
