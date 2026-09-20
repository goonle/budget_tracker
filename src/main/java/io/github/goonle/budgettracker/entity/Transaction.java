package io.github.goonle.budgettracker.entity;

import java.time.LocalTime;
import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name="merchant_id")
    private Merchant merchant;

    private String type;

    private float amount;

    private Instant date;

    private String note;

    private String raw_hash;

    private int week_number;
    private int month_number;
    private int year_number;

    private Instant created_at;
    private Instant updated_at;

}