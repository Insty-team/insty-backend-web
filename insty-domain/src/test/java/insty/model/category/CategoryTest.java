package insty.model.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.CategoryErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CategoryTest {

    @Test
    void create_정상() {
        // given
        Category parentCategory = null;
        int depth = 0;
        String categoryName = "카테고리 이름";
        int sortOrder = 1;

        // when
        Category category = Category.create(parentCategory, depth, categoryName, sortOrder);

        // then
        assertThat(category).isNotNull();
        assertThat(category.getParentCategory()).isEqualTo(parentCategory);
        assertThat(category.getDepth()).isEqualTo(depth);
        assertThat(category.getCategoryName()).isEqualTo(categoryName);
        assertThat(category.getSortOrder()).isEqualTo(sortOrder);
        assertThat(category.isUsed()).isTrue();
    }

    @Test
    void create_에러_깊이가_0_미만이다() {
        // given
        Category parentCategory = null;
        int depth = -1;
        String categoryName = "카테고리 이름";
        int sortOrder = 1;

        // when

        // then
        assertThatThrownBy(() -> Category.create(parentCategory, depth, categoryName, sortOrder))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CategoryErrorCode.CATEGORY_INVALID_DEPTH);
    }

    @Test
    void create_에러_최상위_카테고리인데_depth가_0이_아니다() {
        // given
        Category parentCategory = null;
        int depth = 1;
        String categoryName = "최상위 카테고리";
        int sortOrder = 1;

        // when

        // then
        assertThatThrownBy(() -> Category.create(parentCategory, depth, categoryName, sortOrder))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CategoryErrorCode.CATEGORY_INVALID_DEPTH);
    }

    @Test
    void create_에러_하위_카테고리인데_depth가_0이다() {
        // given
        Category parentCategory = Mockito.mock(Category.class);
        int depth = 0;
        String categoryName = "하위 카테고리";
        int sortOrder = 1;

        // when

        // then
        assertThatThrownBy(() -> Category.create(parentCategory, depth, categoryName, sortOrder))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CategoryErrorCode.CATEGORY_INVALID_DEPTH);
    }
}