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
class AnswerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final long TEST_USER_ID = 1L;
    private static final long TEST_ANSWER_ID = 1L;
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
    @DisplayName("getAnswers - successfully retrieve answers")
    void getAnswers_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/community/answers/question/" + TEST_QUESTION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("createAnswer - with valid input")
    void createAnswer_Success() throws Exception {
        String validContent = "This is a test answer that meets the minimum length requirement of 20 characters";
        String answerRequest = """
            {
                "content": "%s",
                "questionId": %d
            }
            """.formatted(validContent, TEST_QUESTION_ID);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answerRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(validContent));
    }

    @Test
    @DisplayName("updateAnswer - with valid input")
    void updateAnswer_Success() throws Exception {
        String validContent = "This is an updated answer that meets the minimum length requirement";
        String updateRequest = """
            {
                "content": "%s",
                "questionId": %d
            }
            """.formatted(validContent, TEST_QUESTION_ID);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/community/answers/" + TEST_ANSWER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(validContent));
    }

    @Test
    @DisplayName("deleteAnswer - successful deletion")
    void deleteAnswer_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/community/answers/" + TEST_ANSWER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("acceptAnswer - successful acceptance")
    void acceptAnswer_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/answers/" + TEST_ANSWER_ID + "/accept")
                        .param("questionId", String.valueOf(TEST_QUESTION_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    @DisplayName("toggleVerifiedStatus - successful toggle")
    void toggleVerifiedStatus_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/community/answers/" + TEST_ANSWER_ID + "/verify")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").exists());
    }
}