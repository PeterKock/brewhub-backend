package nl.pkock.brewhub_backend.community.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.pkock.brewhub_backend.community.dto.CreateAnswerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnswerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(value = "test_user", roles = "USER")
    void getAnswersByQuestionId_ReturnsAnswerList() throws Exception {
        mockMvc.perform(get("/api/questions/1/answers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(value = "test_user", roles = "USER")
    void createAnswer_Success() throws Exception {
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Test answer");
        request.setQuestionId(1L);

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(value = "test_user", roles = "USER")
    void deleteAnswer_Success() throws Exception {
        mockMvc.perform(delete("/api/answers/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAnswersByQuestionId_UnauthenticatedFails() throws Exception {
        mockMvc.perform(get("/api/questions/1/answers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(value = "test_user", roles = "USER")
    void updateAnswer_Success() throws Exception {
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Updated content");
        request.setQuestionId(1L);

        mockMvc.perform(put("/api/answers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}