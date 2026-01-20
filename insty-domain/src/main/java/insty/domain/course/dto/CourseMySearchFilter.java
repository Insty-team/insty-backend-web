package insty.domain.course.dto;

public record CourseMySearchFilter(
        String title,
        Boolean isShow,
        CourseMyCourseSortType sortType
) {
    private static final CourseMyCourseSortType DEFAULT_SORT_TYPE = CourseMyCourseSortType.LATEST;

    public CourseMyCourseSortType getSortType() {
        return sortType == null ? DEFAULT_SORT_TYPE : sortType;
    }

    public String getTitleOrNullIfBlank() {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title.trim();
    }
}
