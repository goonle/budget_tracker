package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MerchantAlias {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    private Instant created_at;
    private Instant updated_at;

}