package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Merchant {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    private String name;
    private Instant created_at;
    private Instant updated_at;

    @ManyToOne
    @JoinColumn(name = "default_category_id")
    private Category defaultCategory;

    @OneToMany(mappedBy="merchant")
    private List<MerchantAlias> aliases;

    @OneToMany(mappedBy="merchant")
    private List<Transaction> transactions;
}