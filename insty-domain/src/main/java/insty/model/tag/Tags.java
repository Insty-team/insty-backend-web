package insty.model.tag;

import insty.error.TagErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "tags", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tags extends BaseEntity { // 테스트에 @Tag와 겹치므로 Tags로 사용

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String tagName;


    public static Tags create(String tagName) {
        validateCreate(tagName);
        return Tags.builder()
                .tagName(tagName)
                .build();
    }

    private static void validateCreate(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            log.error("Tags 생성 오류 - tagName : 비었음");
            throw new CustomException(TagErrorCode.TAG_CREATE_ERROR);
        }
    }
}
