package insty.model.category;

import insty.error.CategoryErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
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
@Table(name = "categories", schema = "web_service")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    private int depth;

    @Column(nullable = false, length = 50)
    private String categoryName;

    private int sortOrder;

    private boolean isUsed;


    public static Category create(Category parentCategory, int depth, String categoryName, int sortOrder) {
        if (depth < 0) {
            throw new CustomException(CategoryErrorCode.CATEGORY_INVALID_DEPTH);
        }
        if (parentCategory == null && depth == 0) {
            throw new CustomException(CategoryErrorCode.CATEGORY_INVALID_DEPTH);
        }

        return Category.builder()
                .parentCategory(parentCategory)
                .depth(depth)
                .categoryName(categoryName)
                .sortOrder(sortOrder)
                .isUsed(true)
                .build();
    }
}
