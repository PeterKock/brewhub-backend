package nl.pkock.brewhub_backend.community.models;

import jakarta.persistence.*;
import lombok.Data;
import nl.pkock.brewhub_backend.auth.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answers")
@Data
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean accepted = false;

    @Column(nullable = false)
    private boolean verifiedAnswer = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}