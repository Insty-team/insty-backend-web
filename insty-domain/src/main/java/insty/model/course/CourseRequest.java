package insty.model.course;

import insty.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "web_service", name = "course_requests")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CourseRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseRequestStatus status;

    public static CourseRequest create(Long requesterId, String title, String content, Long recipientId) {
        return CourseRequest.builder()
                .requesterId(requesterId)
                .title(title)
                .content(content)
                .recipientId(recipientId)
                .status(CourseRequestStatus.WAITING)
                .build();
    }

}
