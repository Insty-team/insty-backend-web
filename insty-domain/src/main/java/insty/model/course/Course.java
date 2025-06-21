package insty.model.course;

import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.file.File;
import insty.model.user.User;
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
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int likeCount = 0;

    @Column(length = 100)
    private String targetAudience;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_id")
    private File thumbnail;

    @OneToMany(mappedBy = "course")
    private List<CoursePracticeFile> practiceFiles;

    @Column(nullable = false)
    private boolean isShow;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;


    public static Course create(User user, String title, String description, int price, String targetAudience,
                                boolean isShow) {
        validateCreate(title, description, price, targetAudience, isShow);
        return Course.builder()
                .user(user)
                .title(title)
                .description(description)
                .price(price)
                .targetAudience(targetAudience)
                .isShow(isShow)
                .build();
    }

    private static void validateCreate(String title, String description, int price, String targetAudience,
                                       boolean isShow) {
        if (title == null || title.trim().isEmpty()) {
            log.error("생성 오류 - title : 비었음");
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
        if (price < 0) {
            log.error("생성 오류 - price : {}", price);
            throw new CustomException(CourseErrorCode.COURSE_CREATE_ERROR);
        }
    }

    public void update(String title, String description, int price, String targetAudience) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.targetAudience = targetAudience;
    }

    public void deleteLogically() {
        this.isDeleted = true;
    }

    public void updateThumbnail(File thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void deleteThumbnail() {
        this.thumbnail = null;
    }
}
