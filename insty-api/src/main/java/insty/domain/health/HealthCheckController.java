package insty.domain.health;

import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    @GetMapping
    public SuccessRes<?> courseCreate() {
        return SuccessRes.of(null);
    }
}
