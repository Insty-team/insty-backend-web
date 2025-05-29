package insty.domain.course.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseInstallEnvChecklistInfo(
        @NotNull @Size(min = 1, max = 255)
        String content,
        boolean isSupported
) {
}
