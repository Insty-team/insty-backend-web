package insty.mail.builder;

import insty.mail.MailPayload;
import insty.mail.MailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MailType에 따라 적절한 MailBuilder를 반환하는 팩토리
 */
@Component
@RequiredArgsConstructor
public class MailFactory {

    private final AuthMailBuilder authMailBuilder;
    private final CommunityQuestionMailBuilder communityQuestionMailBuilder;
    private final NewAnswerMailBuilder newAnswerMailBuilder;
    private final AnswerAcceptMailBuilder answerAcceptMailBuilder;
    private final MentionMailBuilder mentionMailBuilder;

    /**
     * MailType에 맞는 MailBuilder 반환
     *
     * @param type 메일 타입
     * @param <T>  Payload 타입
     * @return 해당 타입의 MailBuilder
     */
    @SuppressWarnings("unchecked")
    public <T extends MailPayload> MailBuilder<T> getBuilder(MailType type) {
        return (MailBuilder<T>) switch (type) {
            case AUTH -> authMailBuilder;
            case COMMUNITY_QUESTION -> communityQuestionMailBuilder;
            case COMMUNITY_ANSWER -> newAnswerMailBuilder;
            case COMMUNITY_ANSWER_ACCEPT -> answerAcceptMailBuilder;
            case MENTION -> mentionMailBuilder;
        };
    }
}
