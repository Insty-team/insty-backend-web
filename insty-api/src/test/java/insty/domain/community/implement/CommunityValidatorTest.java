package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.domain.community.repository.CommunityPostRepository;
import insty.domain.course.implement.CourseReader;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.community.CommunityCommentFixtureBuilder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityValidatorTest {

    @Mock
    private CommunityPostRepository communityPostRepository;
    @Mock
    private CommunityCommentRepository communityCommentRepository;
    @Mock
    private CommunityPostFileReader communityPostFileReader;
    @Mock
    private CommunityCommentFileReader communityCommentFileReader;
    @Mock
    private CourseReader courseReader;

    @InjectMocks
    private CommunityValidator communityValidator;

    @Test
    void validatePostExists_삭제된_게시글이면_예외() {
        // given
        CommunityPost deletedPost = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        deletedPost.markAsDeleted();
        when(communityPostRepository.findById(1L)).thenReturn(Optional.of(deletedPost));

        // when / then
        assertThatThrownBy(() -> communityValidator.validatePostExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_POST_ALREADY_DELETED);
    }

    @Test
    void validateCommentExists_삭제된_댓글이면_예외() {
        // given
        CommunityComment deletedComment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        deletedComment.markAsDeleted();
        when(communityCommentRepository.findById(1L)).thenReturn(Optional.of(deletedComment));

        // when / then
        assertThatThrownBy(() -> communityValidator.validateCommentExists(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_COMMENT_ALREADY_DELETED);
    }

    @Test
    void validatePostFileCountForUpdate_최대개수초과시_예외() {
        // given
        Long postId = 1L;
        when(communityPostFileReader.getCurrentFileCount(postId)).thenReturn(1);
        List<MultipartFile> addFiles = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{1}),
                new MockMultipartFile("f2", "f2.png", "image/png", new byte[]{1})
        ); // 추가 2개, 현재 1개 -> 총 3개

        // when / then
        assertThatThrownBy(() -> communityValidator.validatePostFileCountForUpdate(postId, addFiles, List.of()))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
    }

    @Test
    void validateCommentFileCountForCreate_최대개수초과시_예외() {
        // given
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{1}),
                new MockMultipartFile("f2", "f2.png", "image/png", new byte[]{1})
        );

        // when / then
        assertThatThrownBy(() -> communityValidator.validateCommentFileCountForCreate(files))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
    }

    @Test
    void validateFiles_빈파일이면_예외() {
        // given
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{}),
                new MockMultipartFile("f2", "f2.png", "image/png", new byte[]{1})
        );

        // when / then
        assertThatThrownBy(() -> communityValidator.validateFiles(files))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_FILE_IS_EMPTY);
    }
}
