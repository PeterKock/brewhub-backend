package nl.pkock.brewhub_backend.guide.dto;

import java.util.List;

public interface GuideRequest {
    String getTitle();
    String getDescription();
    String getCategory();
    Integer getTimeToRead();
    List<SectionDTO> getSections();
    List<String> getTips();
}