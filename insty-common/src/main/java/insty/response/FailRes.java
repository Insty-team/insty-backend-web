package insty.response;

public record FailRes<T>(
        boolean success,
        ErrorInfo<T> error
) {
}
