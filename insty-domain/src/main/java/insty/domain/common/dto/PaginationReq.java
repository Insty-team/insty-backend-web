package insty.domain.common.dto;

public record PaginationReq(
        int page,
        int pageSize
) {

    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }
}
