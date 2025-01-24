package nl.pkock.brewhub_backend.guide.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "guides")
public class Guide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category;
    private Integer timeToRead;

    @ElementCollection
    @CollectionTable(name = "guide_sections", joinColumns = @JoinColumn(name = "guide_id"))
    private List<Section> sections;

    @ElementCollection
    @CollectionTable(name = "guide_tips", joinColumns = @JoinColumn(name = "guide_id"))
    @Column(name = "tip")
    private List<String> tips;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "last_modified_by")
    private Long lastModifiedBy;
}

