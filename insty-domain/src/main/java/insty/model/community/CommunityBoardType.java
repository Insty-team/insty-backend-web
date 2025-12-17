package insty.model.community;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityBoardType {
    QNA,       // 강의 Q&A
    FEED       // 커뮤니티
}
