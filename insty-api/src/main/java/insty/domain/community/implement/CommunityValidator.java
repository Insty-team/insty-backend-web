package insty.domain.community.implement;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.domain.community.repository.CommunityPostRepository;
import insty.domain.course.implement.CourseReader;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityPost;
import insty.model.course.Course;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class CommunityValidator {

    private static final int MAX_POST_FILE_COUNT = 2;
    private static final int MAX_COMMENT_FILE_COUNT = 2;

    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostFileReader communityPostFileReader;
    private final CommunityCommentFileReader communityCommentFileReader;
    private final CourseReader courseReader;

    public Course validateCourse(Long courseId) {
        return courseReader.getCourseById(courseId);
    }

    public CommunityPost validatePostExists(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (post.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_POST_ALREADY_DELETED);
        }
        return post;
    }

    public CommunityComment validateCommentExists(Long commentId) {
        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
        if (comment.isDeleted()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_COMMENT_ALREADY_DELETED);
        }
        return comment;
    }

    public void validatePostAuthor(Long userId, CommunityPost post) {
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_NOT_POST_AUTHOR);
        }
    }

    public void validatePostBelongsToCourse(CommunityPost post, Long courseId) {
        if (!post.getCourse().getId().equals(courseId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
    }

    public void validateCommentAuthor(Long userId, CommunityComment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_NOT_COMMENT_AUTHOR);
        }
    }

    public void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_TITLE_REQUIRED);
        }
    }

    public void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_CONTENT_REQUIRED);
        }
    }

    public void validateFiles(List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null) {
                continue;
            }
            if (file.isEmpty()) {
                throw new CustomException(CommunityErrorCode.COMMUNITY_FILE_IS_EMPTY);
            }
        }
    }

    public void validatePostFileCountForCreate(List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        int count = (int) files.stream().filter(f -> f != null && !f.isEmpty()).count();
        if (count > MAX_POST_FILE_COUNT) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
        }
    }

    public void validatePostFileCountForUpdate(Long postId, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        int currentCount = communityPostFileReader.getCurrentFileCount(postId);
        int addCount = (addFiles == null) ? 0 : (int) addFiles.stream().filter(f -> f != null && !f.isEmpty()).count();
        int deleteCount = (deleteFileIds == null) ? 0 : deleteFileIds.size();
        int finalCount = currentCount - deleteCount + addCount;
        if (finalCount > MAX_POST_FILE_COUNT) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
        }
    }

    public void validateCommentFileCountForCreate(List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        int count = (int) files.stream().filter(f -> f != null && !f.isEmpty()).count();
        if (count > MAX_COMMENT_FILE_COUNT) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
        }
    }

    public void validateCommentFileCountForUpdate(Long commentId, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        int currentCount = communityCommentFileReader.getCurrentFileCount(commentId);
        int addCount = (addFiles == null) ? 0 : (int) addFiles.stream().filter(f -> f != null && !f.isEmpty()).count();
        int deleteCount = (deleteFileIds == null) ? 0 : deleteFileIds.size();
        int finalCount = currentCount - deleteCount + addCount;
        if (finalCount > MAX_COMMENT_FILE_COUNT) {
            throw new CustomException(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
        }
    }
}
