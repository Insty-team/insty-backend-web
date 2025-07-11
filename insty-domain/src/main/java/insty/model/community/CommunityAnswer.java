package insty.model.community;

import insty.model.BaseEntity;
import insty.model.file.File;
import insty.model.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

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
    User user;

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

    public static CommunityAnswer create(CommunityQuestion communityQuestion, User user, String content) {
        return CommunityAnswer.builder()
                .communityQuestion(communityQuestion)
                .user(user)
                .content(content)
                .isDeleted(false)
                .isAccepted(false)
                .build();
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
}
