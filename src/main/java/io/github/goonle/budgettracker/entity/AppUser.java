package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AppUser {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="app_user_seq")
    @SequenceGenerator(name="app_user_seq", sequenceName="app_user_id_seq", allocationSize=1)
    private Long id;

    private String username;
    private String passwordHash;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
    
    @OneToMany(mappedBy="user")
    private List<Transaction> transactions;


}