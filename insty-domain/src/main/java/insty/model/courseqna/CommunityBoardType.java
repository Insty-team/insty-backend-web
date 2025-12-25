package insty.model.courseqna;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityBoardType {
    QNA,       // 강의 Q&A
    COURSE  // 커뮤니티
}
