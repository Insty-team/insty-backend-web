package insty.domain.course.dto;

import insty.model.course.CourseInstallEnvChecklist;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseInstallEnvChecklistInfo(
        @NotNull @Size(min = 1, max = 255)
        String content,
        boolean isSupported
) {

    public static CourseInstallEnvChecklistInfo from(CourseInstallEnvChecklist checklists) {
        return new CourseInstallEnvChecklistInfo(checklists.getContent(), checklists.isSupported());
    }
}
