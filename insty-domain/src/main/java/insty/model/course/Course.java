package insty.model.course;

import insty.model.BaseEntity;
import insty.model.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // TODO - nullable=false 설정하기
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int price;

    private int viewCount;

    private int likeCount;

    @Column(length = 100)
    private String targetAudience;

    private Long thumbnailId; // TODO - file 객체로 바꾸기

    private boolean isShow;


    // TODO - 유저도 필수로 받기
    public static Course create(String title, String description, int price, String targetAudience, Long thumbnailId,
                                boolean isShow) {
        return Course.builder()
                .user(null)
                .title(title)
                .description(description)
                .price(price)
                .viewCount(0)
                .likeCount(0)
                .targetAudience(targetAudience)
                .thumbnailId(thumbnailId)
                .isShow(isShow)
                .build();
    }
}
