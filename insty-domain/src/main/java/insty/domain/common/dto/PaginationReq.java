package insty.domain.common.dto;

import insty.error.CommonErrorCode;
import insty.exception.CustomException;

public record PaginationReq(
        int page,
        int pageSize
) {
    public PaginationReq {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new CustomException(CommonErrorCode.INVALID_PAGINATION_REQUEST);
        }
    }

    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }
}
