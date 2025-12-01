package insty.domain.user.dto.response;

public record NicknameCheckRes(boolean available){
    public static NicknameCheckRes of(boolean duplicateFlag){
        return new NicknameCheckRes(!duplicateFlag);
    }
}
