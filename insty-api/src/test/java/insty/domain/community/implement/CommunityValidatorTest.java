package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityValidatorTest {

    @InjectMocks
    private CommunityValidator communityValidator;

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
                .courseId(1L).userId(1L).title("제목").content("내용")
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
                .courseId(1L).userId(1L).title("   ").content("내용")
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
                .courseId(1L).userId(1L).title("제목").content(null)
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
                .courseId(null).userId(1L).title("제목").content("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_COURSE_ID_IS_REQUIRED);
    }

    @Test
    void validateQuestionCreateRequest_에러_사용자ID가_없음() {
        // 사용자ID가 없을 때 예외가 발생한다.
        // given
        CommunityQuestionCreateReq req = CommunityQuestionCreateReq.builder()
                .courseId(1L).userId(null).title("제목").content("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateQuestionCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
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
                .questionId(1L).userId(1L).content("답변 내용")
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
                .questionId(1L).userId(1L).content(null)
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateAnswerCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_CONTENT_IS_REQUIRED);
    }

    @Test
    void validateAnswerCreateRequest_에러_사용자ID가_없음() {
        // 사용자ID가 없을 때 예외가 발생한다.
        // given
        CommunityAnswerCreateReq req = CommunityAnswerCreateReq.builder()
                .questionId(1L).userId(null).content("답변 내용")
                .build();

        // when & then
        assertThatThrownBy(() -> communityValidator.validateAnswerCreateRequest(req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_USER_ID_IS_REQUIRED);
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