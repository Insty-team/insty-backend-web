package insty.domain.community.dto;

public record AcceptAnswerResult(
        Long answerId,
        boolean accepted
) {}