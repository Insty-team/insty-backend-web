package insty.domain.user.dto.response;

public record UserDuplicateCheckRes (
    boolean isAvailable,
    String reason
) {
    public static UserDuplicateCheckRes from(boolean isAvailable, String reason) {
        return new UserDuplicateCheckRes(isAvailable, reason);
    }
}
