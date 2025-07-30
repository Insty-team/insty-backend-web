package insty.model.community;

import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.file.File;
import insty.model.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "community_answers", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CommunityQuestion communityQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @OneToOne(cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name = "answer_image_id", nullable = true)
    private File answerImage;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    @Column(nullable = false, name = "is_accepted")
    @Builder.Default
    private boolean isAccepted = false;

    @OneToMany(mappedBy = "communityAnswer", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Builder.Default
    private List<CommunityAnswerFile> attachments = new ArrayList<>();


    public static CommunityAnswer create(CommunityQuestion communityQuestion, User user, String content) {
        validateCreate(communityQuestion, user, content);
        return CommunityAnswer.builder()
                .communityQuestion(communityQuestion)
                .user(user)
                .content(content)
                .isDeleted(false)
                .isAccepted(false)
                .build();
    }

    private static void validateCreate(CommunityQuestion communityQuestion, User user, String content) {
        if (communityQuestion == null) {
            log.error("생성 오류 - communityQuestion : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (user == null) {
            log.error("생성 오류 - user : null");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
        if (content == null || content.trim().isEmpty()) {
            log.error("생성 오류 - content : 비었음");
            throw new CustomException(CommunityErrorCode.COMMUNITY_CREATE_ERROR);
        }
    }

    public void update(String content) {
        this.content = content;
    }

    public void accept() {
        this.isAccepted = true;
    }

    public void unaccept() {
        this.isAccepted = false;
    }

    public void removeAllFiles() {
        this.answerImage = null;
        this.attachments.clear();
    }

    public void markAsDeleted() {
        isDeleted = true;
    }
}
