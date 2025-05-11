package insty.response;

public record SuccessRes<T>(
        boolean success,
        T data
) {

    public static <T> SuccessRes<T> of(T data) {
        return new SuccessRes<>(true, data);
    }

    public static <T> SuccessRes<T> of() {
        return of(null);
    }
}
