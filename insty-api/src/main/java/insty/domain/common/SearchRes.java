package insty.domain.common;

import java.util.List;

public record SearchRes<T>(
        PaginationRes pagination,
        List<T> items
) {
}
