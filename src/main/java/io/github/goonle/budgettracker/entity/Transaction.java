package io.github.goonle.budgettracker.entity;

import java.time.LocalTime;
import java.time.Instant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="transaction_seq")
    @SequenceGenerator(name="transaction_seq", sequenceName="transaction_id_seq", allocationSize=1)
    private Long id;

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

    private String rawHash;

    private int weekNumber;
    private int monthNumber;
    private int yearNumber;

    private Instant createdAt;
    private Instant updatedAt;

}