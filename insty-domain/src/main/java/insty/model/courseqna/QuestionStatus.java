package insty.model.courseqna;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionStatus {
    WAITING("답변 대기"),
    ANSWERED("답변 작성됨"),
    ACCEPTED("답변 채택됨");

    private final String description;
}
