package io.github.goonle.budgettracker.entity;

import java.time.Instant;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="user_seq")
    @SequenceGenerator(name="user_seq", sequenceName="user_id_seq", allocationSize=1)
    private Long id;

    private String account_id;
    private String hash_password;

    private String user_name;
    private Instant created_at;
    private Instant updated_at;
    
    @OneToMany(mappedBy="user")
    private List<Transaction> transactions;


}