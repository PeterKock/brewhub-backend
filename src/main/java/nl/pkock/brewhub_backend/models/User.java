package nl.pkock.brewhub_backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import java.util.List;

import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column
    private Integer totalRatings;

    @OneToMany(mappedBy = "retailer")
    private List<Rating> receivedRatings;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;
}