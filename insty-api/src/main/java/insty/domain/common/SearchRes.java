package insty.domain.common;

import insty.domain.common.dto.PaginationRes;
import java.util.List;

public record SearchRes<T>(
        PaginationRes pagination,
        List<T> items
) {

    public static <T> SearchRes<T> from(PaginationRes pagination, List<T> items) {
        return new SearchRes<>(pagination, items);
    }
}
