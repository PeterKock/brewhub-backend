package nl.pkock.brewhub_backend.guide.service;

import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.guide.dto.*;
import nl.pkock.brewhub_backend.guide.models.Guide;
import nl.pkock.brewhub_backend.guide.models.Section;
import nl.pkock.brewhub_backend.guide.repository.GuideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {

    @Mock
    private GuideRepository guideRepository;

    @InjectMocks
    private GuideService guideService;

    private Guide testGuide;
    private UserPrincipal testUser;
    private List<Section> testSections;
    private final List<String> testTips = List.of("Tip 1", "Tip 2");

    @BeforeEach
    void setUp() {
        Section section = new Section();
        section.setTitle("Test Section");
        section.setContent("Test Content");
        testSections = List.of(section);

        testGuide = createTestGuide();
        testUser = new UserPrincipal(1L, "test@test.com", "password", List.of());
    }

    private Guide createTestGuide() {
        Guide guide = new Guide();
        guide.setId(1L);
        guide.setTitle("Beginner's Guide");
        guide.setDescription("A guide for beginners");
        guide.setCategory("getting-started");
        guide.setTimeToRead(15);
        guide.setSections(testSections);
        guide.setTips(testTips);
        guide.setCreatedBy(1L);
        guide.setLastModifiedBy(1L);
        return guide;
    }

    private CreateGuideRequest createTestRequest() {
        CreateGuideRequest request = new CreateGuideRequest();
        request.setTitle(testGuide.getTitle());
        request.setDescription(testGuide.getDescription());
        request.setCategory(testGuide.getCategory());
        request.setTimeToRead(testGuide.getTimeToRead());

        SectionDTO sectionDTO = createSectionDTO();
        request.setSections(List.of(sectionDTO));
        request.setTips(testTips);
        return request;
    }

    private UpdateGuideRequest createUpdateRequest() {
        UpdateGuideRequest request = new UpdateGuideRequest();
        request.setTitle("Updated Guide");
        request.setDescription(testGuide.getDescription());
        request.setCategory(testGuide.getCategory());
        request.setTimeToRead(testGuide.getTimeToRead());

        SectionDTO sectionDTO = createSectionDTO();
        request.setSections(List.of(sectionDTO));
        request.setTips(testTips);
        return request;
    }

    private SectionDTO createSectionDTO() {
        SectionDTO sectionDTO = new SectionDTO();
        sectionDTO.setTitle(testSections.get(0).getTitle());
        sectionDTO.setContent(testSections.get(0).getContent());
        return sectionDTO;
    }

    @Test
    void getAllGuides_ShouldReturnListOfGuides() {
        when(guideRepository.findAll()).thenReturn(List.of(testGuide));

        List<GuideDTO> result = guideService.getAllGuides();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGuide.getTitle(), result.get(0).getTitle());
        verify(guideRepository).findAll();
    }

    @Test
    void getGuideById_WhenGuideExists_ShouldReturnGuide() {
        when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));

        GuideDTO result = guideService.getGuideById(1L);

        assertNotNull(result);
        assertEquals(testGuide.getTitle(), result.getTitle());
        verify(guideRepository).findById(1L);
    }

    @Test
    void createGuide_ShouldSaveAndReturnGuide() {
        CreateGuideRequest request = createTestRequest();
        when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

        GuideDTO result = guideService.createGuide(request, testUser);

        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getSections().size(), result.getSections().size());
        verify(guideRepository).save(any(Guide.class));
    }

    @Test
    void updateGuide_WhenGuideExists_ShouldUpdateAndReturnGuide() {
        UpdateGuideRequest request = createUpdateRequest();
        when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
        when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

        GuideDTO result = guideService.updateGuide(1L, request, testUser);

        assertNotNull(result);
        verify(guideRepository).findById(1L);
        verify(guideRepository).save(any(Guide.class));
    }

    @Test
    void searchGuides_ShouldReturnMatchingGuides() {
        String searchTerm = "Beginner";
        when(guideRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(List.of(testGuide));

        List<GuideDTO> result = guideService.searchGuides(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains(searchTerm));
        verify(guideRepository).findByTitleContainingIgnoreCase(searchTerm);
    }

    @Test
    void getGuidesByCategory_ShouldReturnGuidesWithMatchingCategory() {
        String category = "getting-started";
        when(guideRepository.findByCategory(category))
                .thenReturn(List.of(testGuide));

        List<GuideDTO> result = guideService.getGuidesByCategory(category);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(category, result.get(0).getCategory());
        verify(guideRepository).findByCategory(category);
    }
}