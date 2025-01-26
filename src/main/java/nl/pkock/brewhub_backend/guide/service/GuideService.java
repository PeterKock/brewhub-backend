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

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GuideService {
    private final GuideRepository guideRepository;

    @Autowired
    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    public List<GuideDTO> getAllGuides() {
        List<Guide> guides = guideRepository.findAll();
        return guides.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public GuideDTO getGuideById(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + id));
        return mapToDTO(guide);
    }

    @Transactional
    public GuideDTO createGuide(CreateGuideRequest request, UserPrincipal currentUser) {
        Guide guide = new Guide();
        setGuideProperties(guide, request);
        guide.setCreatedBy(currentUser.getId());
        guide.setLastModifiedBy(currentUser.getId());
        Guide savedGuide = guideRepository.save(guide);
        return mapToDTO(savedGuide);
    }

    @Transactional
    public GuideDTO updateGuide(Long id, UpdateGuideRequest request, UserPrincipal currentUser) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + id));
        setGuideProperties(guide, request);
        guide.setLastModifiedBy(currentUser.getId());
        Guide updatedGuide = guideRepository.save(guide);
        return mapToDTO(updatedGuide);
    }

    private void setGuideProperties(Guide guide, GuideRequest request) {
        guide.setTitle(request.getTitle());
        guide.setDescription(request.getDescription());
        guide.setCategory(request.getCategory());
        guide.setTimeToRead(request.getTimeToRead());

        List<Section> sections = request.getSections().stream()
                .map(dto -> {
                    Section section = new Section();
                    section.setTitle(dto.getTitle());
                    section.setContent(dto.getContent());
                    return section;
                })
                .toList();
        guide.setSections(new ArrayList<>(sections));
        guide.setTips(new ArrayList<>(request.getTips()));
    }

    @Transactional
    public void deleteGuide(Long id) {
        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guide not found with id: " + id));
        guideRepository.delete(guide);
    }

    public List<GuideDTO> searchGuides(String searchTerm) {
        List<Guide> guides = guideRepository.findByTitleContainingIgnoreCase(searchTerm);
        return guides.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<GuideDTO> getGuidesByCategory(String category) {
        List<Guide> guides = guideRepository.findByCategory(category);
        return guides.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private GuideDTO mapToDTO(Guide guide) {
        GuideDTO dto = new GuideDTO();
        dto.setId(guide.getId());
        dto.setTitle(guide.getTitle());
        dto.setDescription(guide.getDescription());
        dto.setCategory(guide.getCategory());
        dto.setTimeToRead(guide.getTimeToRead());

        dto.setSections(guide.getSections().stream()
                .map(section -> {
                    SectionDTO sectionDTO = new SectionDTO();
                    sectionDTO.setTitle(section.getTitle());
                    sectionDTO.setContent(section.getContent());
                    return sectionDTO;
                })
                .toList());

        dto.setTips(new ArrayList<>(guide.getTips()));
        return dto;
    }
}