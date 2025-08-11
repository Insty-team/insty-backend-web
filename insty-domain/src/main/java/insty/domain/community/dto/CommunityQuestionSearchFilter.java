package insty.domain.community.dto;

import lombok.Builder;

@Builder
public record CommunityQuestionSearchFilter(
        String query,       // 검색어
        Boolean isAnswered, // 채택 여부
        Long courseId,      // 강의 필터링
        Long userId         // 작성자 필터링
) {}