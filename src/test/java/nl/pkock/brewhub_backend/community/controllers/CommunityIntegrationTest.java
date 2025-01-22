package nl.pkock.brewhub_backend.community.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.auth.security.JwtTokenProvider;
import nl.pkock.brewhub_backend.auth.security.UserPrincipal;
import nl.pkock.brewhub_backend.community.dto.CreateAnswerRequest;
import nl.pkock.brewhub_backend.community.dto.VoteRequest;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.models.VoteType;
import nl.pkock.brewhub_backend.community.repositories.*;
import nl.pkock.brewhub_backend.inventory.repository.IngredientRepository;
import nl.pkock.brewhub_backend.order.repository.OrderRepository;
import nl.pkock.brewhub_backend.rating.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommunityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Question testQuestion;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Clean up test data
        ratingRepository.deleteAll();
        reportRepository.deleteAll();
        voteRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        ingredientRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user with encoded password
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(Set.of(UserRole.USER));
        testUser = userRepository.save(testUser);

        // Generate JWT token
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        jwtToken = tokenProvider.generateToken(authentication);

        // Create test question
        testQuestion = new Question();
        testQuestion.setTitle("Test Question About Brewing Methods");
        testQuestion.setContent("I have been trying different brewing methods for my coffee lately. " +
                "I've experimented with pour-over, French press, and drip coffee, but I'm curious about " +
                "other methods. Can anyone share their experiences with alternative brewing methods and " +
                "how they affect the taste of the coffee? I'm particularly interested in cold brew and AeroPress.");
        testQuestion.setAuthor(testUser);
        testQuestion.setActive(true);
        testQuestion = questionRepository.save(testQuestion);
    }

    @Test
    void createAnswer_Success() throws Exception {
        CreateAnswerRequest request = new CreateAnswerRequest();
        request.setContent("Here is a detailed response about brewing methods that meets the minimum length requirement. " +
                "I find that using the AeroPress gives a clean and balanced cup of coffee. The brewing process is quick " +
                "and the cleanup is easy. I recommend starting with a medium-fine grind and water at 175°F.");
        request.setQuestionId(testQuestion.getId());

        mockMvc.perform(post("/api/community/answers")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(request.getContent()));
    }

    @Test
    void voteOnQuestion_Success() throws Exception {
        VoteRequest request = new VoteRequest();
        request.setType(VoteType.UPVOTE);
        request.setQuestionId(testQuestion.getId());

        mockMvc.perform(post("/api/community/votes/question")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}