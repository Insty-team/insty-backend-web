package insty.domain.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CoursePostReq(
        @NotNull @Size(min = 1, max = 255)
        String title,
        String description,
        @Size(min = 1, max = 100)
        String targetAudience,
        @Min(0)
        int price,
        @NotNull @Valid
        List<CourseInstallEnvChecklistInfo> installEnvChecklist,
        @NotNull
        List<String> keyPoints,
        @NotNull
        List<String> tags
) {
}
