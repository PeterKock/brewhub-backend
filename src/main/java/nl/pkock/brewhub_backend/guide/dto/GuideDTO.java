package nl.pkock.brewhub_backend.guide.dto;

import lombok.Data;
import java.util.List;

@Data
public class GuideDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer timeToRead;
    private List<SectionDTO> sections;
    private List<String> tips;
}

