package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="category_seq")
    @SequenceGenerator(name="category_seq", sequenceName="category_id_seq", allocationSize=1)
    private Long id;
    private String name;
    private int depth;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @OneToMany(mappedBy="category")
    private List<Transaction> transactions;

    @OneToMany(mappedBy="defaultCategory")
    private List<Merchant> merchants;

}