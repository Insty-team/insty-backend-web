package insty.advice;

public record ParameterData(
        String key,
        String value,
        String reason
) {

    public static ParameterData of(String key, String value, String reason) {
        return new ParameterData(key, value, reason);
    }
}
