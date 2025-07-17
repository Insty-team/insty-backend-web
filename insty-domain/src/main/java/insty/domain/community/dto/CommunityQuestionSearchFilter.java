package insty.domain.community.dto;

public record CommunityQuestionSearchFilter(
        Long courseId,
        Boolean isAnswered,
        String keyword
) {}