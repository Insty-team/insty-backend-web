package insty.domain.community.dto;

public record AcceptAnswerResultRes(
        Long answerId,
        boolean accepted
) {}