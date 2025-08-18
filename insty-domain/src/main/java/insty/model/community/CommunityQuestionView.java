package insty.model.community;

import insty.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "community_question_views", schema = "web_service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityQuestionView extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private CommunityQuestion communityQuestion;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt;

    public static CommunityQuestionView create(CommunityQuestion communityQuestion, Long userId) {
        CommunityQuestionView view = new CommunityQuestionView();
        view.communityQuestion = communityQuestion;
        view.userId = userId;
        view.lastViewedAt = Instant.now();
        return view;
    }

    public void updateLastViewedAt() {
        this.lastViewedAt = Instant.now();
    }
}
