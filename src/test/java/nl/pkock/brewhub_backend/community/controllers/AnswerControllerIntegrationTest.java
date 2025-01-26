package nl.pkock.brewhub_backend.community.controllers;

import nl.pkock.brewhub_backend.community.repositories.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.community.models.Answer;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnswerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ReportRepository reportRepository;

    private User testUser;
    private Question testQuestion;
    private static final long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        reportRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("testpassword");
        testUser.setRoles(Set.of(UserRole.USER));

        // Set up security context
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create test question
        testQuestion = new Question();
        testQuestion.setAuthor(testUser);
        testQuestion.setTitle("Test Question");
        testQuestion.setContent("Test content");
        testQuestion.setCreatedAt(LocalDateTime.now());
        testQuestion.setUpdatedAt(LocalDateTime.now());
        testQuestion.setActive(true);
        testQuestion = questionRepository.save(testQuestion);
    }

    @Test
    void getAnswers_Success() throws Exception {
        createTestAnswer();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/community/answers/question/" + testQuestion.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createAnswer_Success() throws Exception {
        String validContent = "This is a test answer that meets the minimum length requirement of 20 characters";
        String answerRequest = """
        {
            "content": "%s",
            "questionId": %d
        }
        """.formatted(validContent, testQuestion.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(validContent));
    }

    @Test
    void updateAnswer_Success() throws Exception {
        Answer testAnswer = createTestAnswer();

        String validContent = "This is an updated answer that meets the minimum length requirement";
        String updateRequest = """
            {
                "content": "%s",
                "questionId": %d
            }
            """.formatted(validContent, testQuestion.getId());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/community/answers/" + testAnswer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(validContent));
    }

    @Test
    void acceptAnswer_Success() throws Exception {
        Answer testAnswer = createTestAnswer();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/answers/" + testAnswer.getId() + "/accept")
                        .param("questionId", String.valueOf(testQuestion.getId()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void toggleVerifiedStatus_Success() throws Exception {
        Answer testAnswer = createTestAnswer();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/answers/" + testAnswer.getId() + "/verify")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").exists());
    }

    @Test
    void deleteAnswer_Success() throws Exception {
        Answer testAnswer = createTestAnswer();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/community/answers/" + testAnswer.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private Answer createTestAnswer() {
        Answer answer = new Answer();
        answer.setContent("Test content");
        answer.setAuthor(testUser);
        answer.setQuestion(testQuestion);
        answer.setActive(true);
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        return answerRepository.save(answer);
    }
}