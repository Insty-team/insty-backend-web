package insty.domain.community.dto;

public record CommunityQuestionSearchFilter(
        String query,       // 검색어
        String notuesd,     // 사용되지 않는 임시 필드
        Boolean isAnswered, // 채택 여부
        Long courseId,      // 강의 필터링
        Long userId         // 작성자 필터링
) {}