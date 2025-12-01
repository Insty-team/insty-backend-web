package insty.domain.user.dto.response;

public record DuplicateCheckRes(boolean available){
    public static DuplicateCheckRes of(boolean duplicateFlag){
        return new DuplicateCheckRes(!duplicateFlag);
    }
}
