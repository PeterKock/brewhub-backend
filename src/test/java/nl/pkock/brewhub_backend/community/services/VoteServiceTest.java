package nl.pkock.brewhub_backend.community.services;

import nl.pkock.brewhub_backend.auth.models.User;
import nl.pkock.brewhub_backend.auth.repository.UserRepository;
import nl.pkock.brewhub_backend.community.dto.VoteRequest;
import nl.pkock.brewhub_backend.community.models.*;
import nl.pkock.brewhub_backend.community.repositories.AnswerRepository;
import nl.pkock.brewhub_backend.community.repositories.QuestionRepository;
import nl.pkock.brewhub_backend.community.repositories.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VoteService voteService;

    private User testUser;
    private Question testQuestion;
    private Answer testAnswer;
    private VoteRequest voteRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Setup common test objects
        testUser = new User();
        testUser.setId(1L);

        testQuestion = new Question();
        testQuestion.setId(1L);

        testAnswer = new Answer();
        testAnswer.setId(1L);

        voteRequest = new VoteRequest();
        voteRequest.setType(VoteType.UPVOTE);
        voteRequest.setQuestionId(1L);
        voteRequest.setAnswerId(1L);
    }

    @Test
    void voteOnQuestion_NewVote_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(voteRepository.findByUserIdAndQuestionId(1L, 1L)).thenReturn(Optional.empty());

        // Act
        voteService.voteOnQuestion(voteRequest, 1L);

        // Assert
        verify(voteRepository).save(any(Vote.class));
        verify(voteRepository, never()).delete(any(Vote.class));
    }

    @Test
    void voteOnQuestion_ExistingVoteSameType_DeletesVote() {
        // Arrange
        Vote existingVote = new Vote();
        existingVote.setType(VoteType.UPVOTE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(voteRepository.findByUserIdAndQuestionId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Act
        voteService.voteOnQuestion(voteRequest, 1L);

        // Assert
        verify(voteRepository).delete(existingVote);
        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void voteOnQuestion_ExistingVoteDifferentType_UpdatesVote() {
        // Arrange
        Vote existingVote = new Vote();
        existingVote.setType(VoteType.DOWNVOTE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(testQuestion));
        when(voteRepository.findByUserIdAndQuestionId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Act
        voteService.voteOnQuestion(voteRequest, 1L);

        // Assert
        verify(voteRepository).save(existingVote);
        verify(voteRepository, never()).delete(any(Vote.class));
        assertEquals(VoteType.UPVOTE, existingVote.getType());
    }

    @Test
    void voteOnAnswer_NewVote_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(voteRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.empty());

        // Act
        voteService.voteOnAnswer(voteRequest, 1L);

        // Assert
        verify(voteRepository).save(any(Vote.class));
        verify(voteRepository, never()).delete(any(Vote.class));
    }

    @Test
    void voteOnAnswer_ExistingVoteSameType_DeletesVote() {
        // Arrange
        Vote existingVote = new Vote();
        existingVote.setType(VoteType.UPVOTE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(voteRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Act
        voteService.voteOnAnswer(voteRequest, 1L);

        // Assert
        verify(voteRepository).delete(existingVote);
        verify(voteRepository, never()).save(any(Vote.class));
    }

    @Test
    void voteOnAnswer_ExistingVoteDifferentType_UpdatesVote() {
        // Arrange
        Vote existingVote = new Vote();
        existingVote.setType(VoteType.DOWNVOTE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(testAnswer));
        when(voteRepository.findByUserIdAndAnswerId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Act
        voteService.voteOnAnswer(voteRequest, 1L);

        // Assert
        verify(voteRepository).save(existingVote);
        verify(voteRepository, never()).delete(any(Vote.class));
        assertEquals(VoteType.UPVOTE, existingVote.getType());
    }

    @Test
    void voteOnQuestion_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                voteService.voteOnQuestion(voteRequest, 1L));
    }

    @Test
    void voteOnQuestion_QuestionNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                voteService.voteOnQuestion(voteRequest, 1L));
    }

    @Test
    void voteOnAnswer_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                voteService.voteOnAnswer(voteRequest, 1L));
    }

    @Test
    void voteOnAnswer_AnswerNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                voteService.voteOnAnswer(voteRequest, 1L));
    }
}