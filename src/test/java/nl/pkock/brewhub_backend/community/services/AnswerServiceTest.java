package nl.pkock.brewhub_backend.community.services;

import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.models.UserRole;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.community.dto.AnswerDTO;
import nl.pkock.brewhub_backend.community.dto.CreateAnswerRequest;
import nl.pkock.brewhub_backend.community.models.Answer;
import nl.pkock.brewhub_backend.community.models.Question;
import nl.pkock.brewhub_backend.community.models.Vote;
import nl.pkock.brewhub_backend.community.models.VoteType;
import nl.pkock.brewhub_backend.community.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;
import nl.pkock.brewhub_backend.community.repositories.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private AnswerService answerService;

    private User testUser;
    private Question testQuestion;
    private Answer testAnswer;
    private CreateAnswerRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(Set.of(UserRole.USER));

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setAuthor(testUser);

        testAnswer = new Answer();
        testAnswer.setId(1L);
        testAnswer.setContent("Test answer content");
        testAnswer.setAuthor(testUser);
        testAnswer.setQuestion(testQuestion);
        testAnswer.setCreatedAt(LocalDateTime.now());
        testAnswer.setActive(true);

        createRequest = new CreateAnswerRequest();
        createRequest.setContent("New answer content");
        createRequest.setQuestionId(1L);
    }

    @Test
    void getAnswersByQuestionId_ReturnsAnswerList() {
        // Arrange
        List<Answer> answers = Collections.singletonList(testAnswer);
        when(answerRepository.findByQuestionIdAndActiveTrue(1L)).thenReturn(answers);

        // Act
        List<AnswerDTO> result = answerService.getAnswersByQuestionId(1L, 1L);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(testAnswer.getContent(), result.get(0).getContent());
    }

    @Test
    void getAnswersByQuestionId_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(answerRepository.findByQuestionIdAndActiveTrue(1L)).thenReturn(Collections.emptyList());

        // Act
        List<AnswerDTO> result = answerService.getAnswersByQuestionId(1L, 1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void createAnswer_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(answerRepository.save(any(Answer.class))).thenReturn(testAnswer);

        // Act
        AnswerDTO result = answerService.createAnswer(createRequest, 1L);

        // Assert
        assertNotNull(result);
        verify(answerRepository).save(any(Answer.class));
    }

    @Test
    void createAnswer_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.createAnswer(createRequest, 1L));
    }

    @Test
    void createAnswer_QuestionNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.createAnswer(createRequest, 1L));
    }

    @Test
    void getAnswersByQuestionId_IncludesUserVote() {
        // Arrange
        List<Answer> answers = Collections.singletonList(testAnswer);
        Vote userVote = new Vote();
        userVote.setId(1L);
        userVote.setType(VoteType.UPVOTE);
        userVote.setUser(testUser);

        when(answerRepository.findByQuestionIdAndActiveTrue(1L)).thenReturn(answers);
        when(voteRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.of(userVote));

        // Act
        List<AnswerDTO> result = answerService.getAnswersByQuestionId(1L, 1L);

        // Assert
        assertFalse(result.isEmpty());
        AnswerDTO answerDTO = result.get(0);
        assertNotNull(answerDTO.getUserVote());
        assertEquals(VoteType.UPVOTE, answerDTO.getUserVote().getType());
    }

    @Test
    void updateAnswer_Success() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(answerRepository.save(any(Answer.class))).thenReturn(testAnswer);

        // Act
        AnswerDTO result = answerService.updateAnswer(1L, createRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(createRequest.getContent(), testAnswer.getContent());
    }

    @Test
    void updateAnswer_AnswerNotFound_ThrowsException() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.updateAnswer(1L, createRequest, 1L));
    }

    @Test
    void updateAnswer_UnauthorizedUser_ThrowsException() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.updateAnswer(1L, createRequest, 2L));
    }

    @Test
    void deleteAnswer_Success() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));

        // Act
        answerService.deleteAnswer(1L, 1L);

        // Assert
        verify(answerRepository).save(testAnswer);
        assertFalse(testAnswer.isActive());
    }

    @Test
    void deleteAnswer_AnswerNotFound_ThrowsException() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.deleteAnswer(1L, 1L));
    }

    @Test
    void deleteAnswer_UnauthorizedUser_ThrowsException() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.deleteAnswer(1L, 2L));
    }

    @Test
    void acceptAnswer_Success() {
        // Arrange
        List<Answer> answers = new ArrayList<>();
        Answer previousAcceptedAnswer = new Answer();
        previousAcceptedAnswer.setAccepted(true);
        answers.add(previousAcceptedAnswer);
        answers.add(testAnswer);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(answerRepository.findByQuestionIdAndActiveTrue(1L)).thenReturn(answers);
        when(answerRepository.save(any(Answer.class))).thenReturn(testAnswer);

        // Act
        AnswerDTO result = answerService.acceptAnswer(1L, 1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(testAnswer.isAccepted());
        assertFalse(previousAcceptedAnswer.isAccepted());
    }

    @Test
    void acceptAnswer_QuestionNotFound_ThrowsException() {
        // Arrange
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.acceptAnswer(1L, 1L, 1L));
    }

    @Test
    void acceptAnswer_UnauthorizedUser_ThrowsException() {
        // Arrange
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.acceptAnswer(1L, 1L, 2L));
    }

    @Test
    void toggleVerifiedStatus_Success() {
        // Arrange
        testAnswer.setVerifiedAnswer(false);
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(answerRepository.save(any(Answer.class))).thenReturn(testAnswer);

        // Act
        AnswerDTO result = answerService.toggleVerifiedStatus(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(testAnswer.isVerifiedAnswer());
    }

    @Test
    void toggleVerifiedStatus_AnswerNotFound_ThrowsException() {
        // Arrange
        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                answerService.toggleVerifiedStatus(1L, 1L));
    }

    @Test
    void calculateVoteCount_WithMixedVotes_ReturnsCorrectCount() {
        // Arrange
        List<Vote> votes = new ArrayList<>(Arrays.asList(
                createVote(VoteType.UPVOTE),
                createVote(VoteType.UPVOTE),
                createVote(VoteType.DOWNVOTE)
        ));
        testAnswer.setVotes(votes);

        when(answerRepository.findByQuestionIdAndActiveTrue(1L))
                .thenReturn(Collections.singletonList(testAnswer));

        // Act
        List<AnswerDTO> results = answerService.getAnswersByQuestionId(1L, 1L);

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.get(0).getVoteCount());
    }

    private Vote createVote(VoteType type) {
        Vote vote = new Vote();
        vote.setType(type);
        return vote;
    }
}