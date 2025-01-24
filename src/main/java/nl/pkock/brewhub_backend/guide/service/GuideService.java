package nl.pkock.brewhub_backend.guide.service;

import nl.pkock.brewhub_backend.guide.models.Guide;
import nl.pkock.brewhub_backend.guide.models.Section;
import nl.pkock.brewhub_backend.guide.repository.GuideRepository;
import nl.pkock.brewhub_backend.guide.dto.*;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class GuideService {
    private final GuideRepository guideRepository;

    @Autowired
    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }

    public Guide getGuideById(Long id) {
        return guideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + id));
    }

    @Transactional
    public Guide createGuide(CreateGuideRequest request, UserPrincipal currentUser) {
        Guide guide = new Guide();
        setGuideProperties(guide, request);
        guide.setCreatedBy(currentUser.getId());
        guide.setLastModifiedBy(currentUser.getId());
        return guideRepository.save(guide);
    }

    @Transactional
    public Guide updateGuide(Long id, UpdateGuideRequest request, UserPrincipal currentUser) {
        Guide guide = getGuideById(id);
        setGuideProperties(guide, request);
        guide.setLastModifiedBy(currentUser.getId());
        return guideRepository.save(guide);
    }

    private void setGuideProperties(Guide guide, GuideRequest request) {
        guide.setTitle(request.getTitle());
        guide.setDescription(request.getDescription());
        guide.setCategory(request.getCategory());
        guide.setTimeToRead(request.getTimeToRead());
        guide.setSections(request.getSections().stream()
                .map(dto -> {
                    Section section = new Section();
                    section.setTitle(dto.getTitle());
                    section.setContent(dto.getContent());
                    return section;
                })
                .collect(Collectors.toList()));
        guide.setTips(request.getTips());
    }

    @Transactional
    public void deleteGuide(Long id) {
        Guide guide = getGuideById(id);
        guideRepository.delete(guide);
    }

    public List<Guide> searchGuides(String searchTerm) {
        return guideRepository.findByTitleContainingIgnoreCase(searchTerm);
    }

    public List<Guide> getGuidesByCategory(String category) {
        return guideRepository.findByCategory(category);
    }
}