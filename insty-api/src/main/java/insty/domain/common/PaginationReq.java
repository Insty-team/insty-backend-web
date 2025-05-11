package insty.domain.common;

public record PaginationReq(
        int page,
        int perPage
) {

    public long getOffset() {
        return (long) (page - 1) * perPage;
    }
}
