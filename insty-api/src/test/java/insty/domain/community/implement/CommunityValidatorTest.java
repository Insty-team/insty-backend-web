package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityValidatorTest {

    @InjectMocks
    private CommunityValidator communityValidator;

    @Mock
    private CommunityReader communityReader;

    @Test
    void validateQuestionOwner_정상() {
        // given
        Long questionId = 1L;
        Long userId = 1L;
        CommunityQuestion question = mock(CommunityQuestion.class);
        User user = mock(User.class);

        // mock
        when(communityReader.getCommunityQuestionDetailsById(questionId.toString()))
                .thenReturn(question);
        when(question.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // when

        // then
        assertThatCode(() -> communityValidator.validateQuestionOwner(questionId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionOwner_에러_질문_작성자가_아님() {
        // given
        Long questionId = 1L;
        Long userId = 1L;
        Long differentUserId = 2L;
        CommunityQuestion question = mock(CommunityQuestion.class);
        User user = mock(User.class);

        // mock
        when(communityReader.getCommunityQuestionDetailsById(questionId.toString()))
                .thenReturn(question);
        when(question.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(differentUserId);

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateQuestionOwner(questionId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED);
    }

    @Test
    void validateAnswerOwner_정상() {
        // given
        Long answerId = 1L;
        Long userId = 1L;
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);

        // mock
        when(communityReader.getCommunityAnswerById(answerId.toString()))
                .thenReturn(answer);
        when(answer.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // when

        // then
        assertThatCode(() -> communityValidator.validateAnswerOwner(answerId, userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerOwner_에러_답변_작성자가_아님() {
        // given
        Long answerId = 1L;
        Long userId = 1L;
        Long differentUserId = 2L;
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);

        // mock
        when(communityReader.getCommunityAnswerById(answerId.toString()))
                .thenReturn(answer);
        when(answer.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(differentUserId);

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateAnswerOwner(answerId, userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED);
    }

    @Test
    void validateQuestionExists_정상() {
        // given
        Long questionId = 1L;
        CommunityQuestion question = mock(CommunityQuestion.class);

        // mock
        when(communityReader.getCommunityQuestionDetailsById(questionId.toString()))
                .thenReturn(question);

        // when
        CommunityQuestion result = communityValidator.validateQuestionExists(questionId);

        // then
        assertThatCode(() -> communityValidator.validateQuestionExists(questionId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerExists_정상() {
        // given
        Long answerId = 1L;
        CommunityAnswer answer = mock(CommunityAnswer.class);

        // mock
        when(communityReader.getCommunityAnswerById(answerId.toString()))
                .thenReturn(answer);

        // when
        CommunityAnswer result = communityValidator.validateAnswerExists(answerId);

        // then
        assertThatCode(() -> communityValidator.validateAnswerExists(answerId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerBelongsToQuestion_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Long questionId = 1L;

        // mock
        when(question.getId()).thenReturn(questionId);
        when(answer.getCommunityQuestion()).thenReturn(question);
        when(answer.getCommunityQuestion().getId()).thenReturn(questionId);

        // when

        // then
        assertThatCode(() -> communityValidator.validateAnswerBelongsToQuestion(answer, question))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerBelongsToQuestion_에러_답변이_해당_질문에_속하지_않음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityQuestion differentQuestion = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Long questionId = 1L;
        Long differentQuestionId = 2L;

        // mock
        when(question.getId()).thenReturn(questionId);
        when(differentQuestion.getId()).thenReturn(differentQuestionId);
        when(answer.getCommunityQuestion()).thenReturn(differentQuestion);

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateAnswerBelongsToQuestion(answer, question))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
    }

    @Test
    void validateQuestionRequest_정상() {
        // given
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, 1L, 1L, "테스트 제목", "테스트 내용"
        );

        // when

        // then
        assertThatCode(() -> communityValidator.validateQuestionRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionRequest_에러_제목이_없음() {
        // given
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, 1L, 1L, null, "테스트 내용"
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateQuestionRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
    }

    @Test
    void validateQuestionRequest_에러_제목이_빈문자열() {
        // given
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, 1L, 1L, "   ", "테스트 내용"
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateQuestionRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
    }

    @Test
    void validateQuestionRequest_에러_내용이_없음() {
        // given
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, 1L, 1L, "테스트 제목", null
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateQuestionRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateQuestionRequest_에러_강의ID가_없음() {
        // given
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, null, 1L, "테스트 제목", "테스트 내용"
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateQuestionRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_COURSE_ID_IS_REQUIRED);
    }

    @Test
    void validateQuestionRequest_에러_사용자ID가_없음() {
        // given
        CommunityQuestionReq req = CommunityQuestionReq.create(
                1L, 1L, null, "테스트 제목", "테스트 내용"
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateQuestionRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
    }

    @Test
    void validateAnswerRequest_정상() {
        // given
        CommunityAnswerReq req = CommunityAnswerReq.create(
                "1", 1L, "테스트 답변 내용"
        );

        // when

        // then
        assertThatCode(() -> communityValidator.validateAnswerRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerRequest_에러_내용이_없음() {
        // given
        CommunityAnswerReq req = CommunityAnswerReq.create(
                "1", 1L, null
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateAnswerRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateAnswerRequest_에러_질문ID가_없음() {
        // given
        CommunityAnswerReq req = CommunityAnswerReq.create(
                null, 1L, "테스트 답변 내용"
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateAnswerRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_ID_IS_REQUIRED);
    }

    @Test
    void validateAnswerRequest_에러_사용자ID가_없음() {
        // given
        CommunityAnswerReq req = CommunityAnswerReq.create(
                "1", null, "테스트 답변 내용"
        );

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateAnswerRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
    }

    @Test
    void validateFiles_정상_파일이_없음() {
        // given
        List<MultipartFile> files = null;

        // when

        // then
        assertThatCode(() -> communityValidator.validateFiles(files))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFiles_정상_빈_리스트() {
        // given
        List<MultipartFile> files = List.of();

        // when

        // then
        assertThatCode(() -> communityValidator.validateFiles(files))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFiles_에러_빈_파일() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);

        // mock
        when(file.isEmpty()).thenReturn(true);

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateFiles(files))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_FILE_IS_EMPTY);
    }

    @Test
    void validateAndParseVideoUuid_정상_유효한_UUID() {
        // given
        String videoUuid = "00000000-0000-0000-0000-000000000001";

        // when
        UUID result = communityValidator.validateAndParseVideoUuid(videoUuid);

        // then
        assertThatCode(() -> communityValidator.validateAndParseVideoUuid(videoUuid))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_정상_null() {
        // given
        String videoUuid = null;

        // when
        UUID result = communityValidator.validateAndParseVideoUuid(videoUuid);

        // then
        assertThatCode(() -> communityValidator.validateAndParseVideoUuid(videoUuid))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_정상_빈문자열() {
        // given
        String videoUuid = "";

        // when
        UUID result = communityValidator.validateAndParseVideoUuid(videoUuid);

        // then
        assertThatCode(() -> communityValidator.validateAndParseVideoUuid(videoUuid))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_에러_잘못된_UUID_형식() {
        // given
        String videoUuid = "invalid-uuid";

        // when

        // then
        assertThatThrownBy(() -> communityValidator.validateAndParseVideoUuid(videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_INVALID_VIDEO_UUID);
    }
}