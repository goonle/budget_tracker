package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MemberCard {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_card_seq")
    @SequenceGenerator(name = "member_card_seq", sequenceName = "member_card_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "card_last4", length = 4, nullable = false)
    private String cardLast4;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
