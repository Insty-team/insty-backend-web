package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.repository.CommunityAnswerRepository;
import insty.domain.community.repository.CommunityQuestionRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;
import java.util.List;
import java.util.Optional;
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
    private CommunityQuestionRepository communityQuestionRepository;
    @Mock
    private CommunityAnswerRepository communityAnswerRepository;

    @Test
    void validateQuestionExists_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.isDeleted()).thenReturn(false);
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.of(question));
        // when & then
        assertThatCode(() -> communityValidator.validateQuestionExists(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionExists_에러_존재하지않음() {
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityValidator.validateQuestionExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_NOT_FOUND);
    }

    @Test
    void validateQuestionExists_에러_삭제됨() {
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.isDeleted()).thenReturn(true);
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> communityValidator.validateQuestionExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_QUESTION_ALREADY_DELETED);
    }

    @Test
    void validateAnswerExists_정상() {
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.isDeleted()).thenReturn(false);
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatCode(() -> communityValidator.validateAnswerExists(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerExists_에러_존재하지않음() {
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityValidator.validateAnswerExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_FOUND);
    }

    @Test
    void validateAnswerExists_에러_삭제됨() {
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.isDeleted()).thenReturn(true);
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> communityValidator.validateAnswerExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_ALREADY_DELETED);
    }

    @Test
    void validateQuestionAuthor_정상() {
        CommunityQuestion question = mock(CommunityQuestion.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(question.getUser()).thenReturn(user);
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatCode(() -> communityValidator.validateQuestionAuthor(10L, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionAuthor_에러_작성자_아님() {
        CommunityQuestion question = mock(CommunityQuestion.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(99L);
        when(question.getUser()).thenReturn(user);
        when(communityQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> communityValidator.validateQuestionAuthor(10L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_NOT_QUESTION_AUTHOR);
    }

    @Test
    void validateAnswerAuthor_정상() {
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        when(answer.getUser()).thenReturn(user);
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatCode(() -> communityValidator.validateAnswerAuthor(10L, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerAuthor_에러_작성자_아님() {
        CommunityAnswer answer = mock(CommunityAnswer.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(99L);
        when(answer.getUser()).thenReturn(user);
        when(communityAnswerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> communityValidator.validateAnswerAuthor(10L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_NOT_ANSWER_AUTHOR);
    }

    @Test
    void validateAnswerBelongsToQuestion_정상() {
        // 답변이 해당 질문에 속하는지 정상적으로 검증한다.
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Long questionId = 1L;
        when(question.getId()).thenReturn(questionId);
        when(answer.getCommunityQuestion()).thenReturn(question);
        when(answer.getCommunityQuestion().getId()).thenReturn(questionId);
        // when & then
        assertThatCode(() -> communityValidator.validateAnswerBelongsToQuestion(answer, question)).doesNotThrowAnyException();
    }

    @Test
    void validateAnswerBelongsToQuestion_에러_답변이_해당_질문에_속하지_않음() {
        // 답변이 해당 질문에 속하지 않을 때 예외가 발생한다.
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityQuestion differentQuestion = mock(CommunityQuestion.class);
        CommunityAnswer answer = mock(CommunityAnswer.class);
        Long questionId = 1L;
        Long differentQuestionId = 2L;
        when(question.getId()).thenReturn(questionId);
        when(differentQuestion.getId()).thenReturn(differentQuestionId);
        when(answer.getCommunityQuestion()).thenReturn(differentQuestion);
        // when & then
        assertThatThrownBy(() -> communityValidator.validateAnswerBelongsToQuestion(answer, question))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
    }

    @Test
    void validateQuestionCreateRequest_정상() {
        // 질문 생성 요청 데이터 검증이 정상 동작한다.
        // given
        CommunityQuestionCreateReq req = CommunityQuestionCreateReq.builder()
                .courseId(1L).title("제목").content("내용")
                .build();

        // when & then
        assertThatCode(() -> communityValidator.validateQuestionCreateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateQuestionCreateRequest_에러_제목이_빈문자열() {
        // 제목이 빈문자열이면 예외를 발생시킨다.
        // given
        CommunityQuestionCreateReq req = CommunityQuestionCreateReq.builder()
                .courseId(1L).title("   ").content("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
    }

    @Test
    void validateQuestionCreateRequest_에러_내용이_없음() {
        // 내용이 없을 때 예외가 발생한다.
        // given
        CommunityQuestionCreateReq req = CommunityQuestionCreateReq.builder()
                .courseId(1L).title("제목").content(null)
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateQuestionCreateRequest_에러_강의ID가_없음() {
        // 강의ID가 없을 때 예외가 발생한다.
        // given
        CommunityQuestionCreateReq req = CommunityQuestionCreateReq.builder()
                .courseId(null).title("제목").content("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_COURSE_ID_IS_REQUIRED);
    }

    @Test
    void validateQuestionUpdateRequest_정상() {
        // 질문 수정 요청 데이터 검증이 정상 동작한다.
        // given
        CommunityQuestionUpdateReq req = CommunityQuestionUpdateReq.builder()
                .questionId(1L).title("제목").content("내용")
                .build();
        // when & then
        assertThatCode(() -> communityValidator.validateQuestionUpdateRequest(req)).doesNotThrowAnyException();
    }

    @Test
    void validateQuestionUpdateRequest_에러_제목이_없음() {
        // 제목이 없을 때 예외가 발생한다.
        // given
        CommunityQuestionUpdateReq req = CommunityQuestionUpdateReq.builder()
                .questionId(1L).title(null).content("내용")
                .build();
        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionUpdateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
    }

    @Test
    void validateQuestionUpdateRequest_에러_제목이_빈문자열() {
        // 제목이 빈 문자열일 때 예외가 발생한다.
        // given
        CommunityQuestionUpdateReq req = CommunityQuestionUpdateReq.builder()
                .questionId(1L).title("   ").content("내용")
                .build();
        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionUpdateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_TITLE_IS_REQUIRED);
    }

    @Test
    void validateQuestionUpdateRequest_에러_내용이_없음() {
        // 내용이 없을 때 예외가 발생한다.
        // given
        CommunityQuestionUpdateReq req = CommunityQuestionUpdateReq.builder()
                .questionId(1L).title("제목").content(null)
                .build();
        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionUpdateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateAnswerCreateRequest_정상() {
        // 답변 생성 요청 데이터 검증이 정상 동작한다.
        // given
        CommunityAnswerCreateReq req = CommunityAnswerCreateReq.builder()
                .questionId(1L).content("답변 내용")
                .build();

        // when & then
        assertThatCode(() -> communityValidator.validateAnswerCreateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerCreateRequest_에러_내용이_없음() {
        // 내용이 없을 때 예외가 발생한다.
        // given
        CommunityAnswerCreateReq req = CommunityAnswerCreateReq.builder()
                .questionId(1L).content(null)
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateAnswerCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateAnswerUpdateRequest_정상() {
        // 답변 수정 요청 데이터 검증이 정상 동작한다.
        // given
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.builder()
                .answerId(1L).content("답변 내용")
                .build();

        // when & then
        assertThatCode(() -> communityValidator.validateAnswerUpdateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAnswerUpdateRequest_에러_내용이_없음() {
        // 내용이 없을 때 예외가 발생한다.
        // given
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.builder()
                .answerId(1L).content(null)
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateAnswerUpdateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateAnswerUpdateRequest_에러_답변ID가_없음() {
        // 답변ID가 없을 때 예외가 발생한다.
        // given
        CommunityAnswerUpdateReq req = CommunityAnswerUpdateReq.builder()
                .answerId(null).content("답변 내용")
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateAnswerUpdateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION);
    }


    @Test
    void validateFiles_정상_파일이_없음() {
        // 파일이 없을 때 정상 동작한다.
        // given
        List<MultipartFile> files = null;
        // when & then
        assertThatCode(() -> communityValidator.validateFiles(files)).doesNotThrowAnyException();
    }

    @Test
    void validateFiles_정상_빈_리스트() {
        // 빈 파일 리스트일 때 정상 동작한다.
        // given
        List<MultipartFile> files = List.of();
        // when & then
        assertThatCode(() -> communityValidator.validateFiles(files)).doesNotThrowAnyException();
    }

    @Test
    void validateFiles_에러_빈_파일() {
        // 빈 파일이 포함되어 있을 때 예외가 발생한다.
        // given
        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);
        when(file.isEmpty()).thenReturn(true);
        // when & then
        assertThatThrownBy(() -> communityValidator.validateFiles(files))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_FILE_IS_EMPTY);
    }

    @Test
    void validateAndParseVideoUuid_정상_유효한_UUID() {
        // 유효한 UUID일 때 정상 동작한다.
        // given
        String videoUuid = "00000000-0000-0000-0000-000000000001";
        // when & then
        assertThatCode(() -> communityValidator.validateAndParseVideoUuid(videoUuid)).doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_정상_null() {
        // null일 때 정상 동작한다.
        // given
        String videoUuid = null;
        // when & then
        assertThatCode(() -> communityValidator.validateAndParseVideoUuid(videoUuid)).doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_정상_빈문자열() {
        // 빈 문자열일 때 정상 동작한다.
        // given
        String videoUuid = "";
        // when & then
        assertThatCode(() -> communityValidator.validateAndParseVideoUuid(videoUuid)).doesNotThrowAnyException();
    }

    @Test
    void validateAndParseVideoUuid_에러_잘못된_UUID_형식() {
        // 잘못된 UUID 형식일 때 예외가 발생한다.
        // given
        String videoUuid = "invalid-uuid";
        // when & then
        assertThatThrownBy(() -> communityValidator.validateAndParseVideoUuid(videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_INVALID_VIDEO_UUID);
    }
}