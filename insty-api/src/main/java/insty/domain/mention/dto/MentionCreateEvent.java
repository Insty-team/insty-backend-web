package insty.domain.mention.dto;

import insty.model.mention.MentionTargetType;

public record MentionCreateEvent(
        Long mentionerUserId,
        MentionTargetType targetType,
        Long targetId,
        String content
) {

    public static MentionCreateEvent of(Long mentionerUserId, MentionTargetType targetType, Long targetId, String content) {
        return new MentionCreateEvent(mentionerUserId, targetType, targetId, content);
    }
}
