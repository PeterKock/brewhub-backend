package nl.pkock.brewhub_backend.community.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.community.dto.CreateAnswerRequest;
import nl.pkock.brewhub_backend.community.dto.VoteRequest;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.models.VoteType;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CommunityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private User testUser;
    private Question testQuestion;

    @BeforeEach
    void setUp() {
        // Clean up test data
        userRepository.deleteAll();
        questionRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(Set.of(UserRole.USER));
        testUser = userRepository.save(testUser);

        // Create test question
        testQuestion = new Question();
        testQuestion.setTitle("Test Question");
        testQuestion.setContent("Test Content");
        testQuestion.setAuthor(testUser);
        testQuestion.setActive(true);
        testQuestion = questionRepository.save(testQuestion);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createAnswer_Success() throws Exception {
        // Arrange
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Test answer content");
        request.setQuestionId(testQuestion.getId());

        // Act & Assert
        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test answer content"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void voteOnQuestion_Success() throws Exception {
        // Arrange
        VoteRequest request = new VoteRequest();
        request.setType(VoteType.UPVOTE);
        request.setQuestionId(testQuestion.getId());

        // Act & Assert
        mockMvc.perform(post("/api/votes/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}