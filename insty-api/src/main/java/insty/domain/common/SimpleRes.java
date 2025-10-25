package insty.domain.common;

public record SimpleRes<T>(T data) {
    public static <T> SimpleRes<T> from(T data) {
        return new SimpleRes<>(data);
    }
}