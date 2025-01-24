package nl.pkock.brewhub_backend.guide.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Section {
    private String title;
    private String content;
}
