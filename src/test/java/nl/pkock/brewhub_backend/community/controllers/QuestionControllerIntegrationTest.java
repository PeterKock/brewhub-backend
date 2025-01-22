package nl.pkock.brewhub_backend.community.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;

import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final long TEST_USER_ID = 1L;
    private static final long TEST_QUESTION_ID = 1L;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("testpassword");
        testUser.setRoles(Set.of(UserRole.USER));

        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("getQuestions - get all questions")
    void getQuestions() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/community/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("getQuestionById - get specific question")
    void getQuestionById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/community/questions/" + TEST_QUESTION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("createQuestion - with valid input")
    void createQuestion_whenLoggedIn() throws Exception {
        String validContent = "This is a test question content that meets the minimum length requirement of 20 characters";
        String questionRequest = """
            {
                "title": "Test Question Title",
                "content": "%s"
            }
            """.formatted(validContent);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Question Title"))
                .andExpect(jsonPath("$.content").value(validContent));
    }

    @Test
    @DisplayName("updateQuestion - with valid input")
    void updateQuestion_whenLoggedIn() throws Exception {
        String validContent = "This is an updated question content that meets the minimum length requirement";
        String updateRequest = """
            {
                "title": "Updated Question Title",
                "content": "%s"
            }
            """.formatted(validContent);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/community/questions/" + TEST_QUESTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Question Title"))
                .andExpect(jsonPath("$.content").value(validContent));
    }

    @Test
    @DisplayName("deleteQuestion - successful deletion")
    void deleteQuestion_whenLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/community/questions/" + TEST_QUESTION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("togglePin - successful pin toggle")
    void togglePin_whenLoggedIn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/questions/" + TEST_QUESTION_ID + "/pin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("searchQuestions - with search term")
    void searchQuestions() throws Exception {
        String searchTerm = "test";
        mockMvc.perform(MockMvcRequestBuilders.get("/api/community/questions/search")
                        .param("query", searchTerm)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}