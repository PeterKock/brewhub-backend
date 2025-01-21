package nl.pkock.brewhub_backend.community.models;

import jakarta.persistence.*;
import lombok.Data;
import nl.pkock.brewhub_backend.auth.models.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "votes")
@Data
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}