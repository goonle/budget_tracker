package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MerchantAlias {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="merchant_alias_seq")
    @SequenceGenerator(name="merchant_alias_seq", sequenceName="merchant_alias_id_seq", allocationSize=1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    private String rawPattern;

    private Instant createdAt;
    private Instant updatedAt;

}