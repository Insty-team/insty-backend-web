package insty.domain.example.controller;

import insty.domain.common.PaginationReq;
import insty.domain.example.dto.ExampleReq;
import insty.domain.example.dto.ExampleRes;
import insty.domain.example.service.ExampleService;
import insty.global.response.SuccessRes;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    @GetMapping
    public SuccessRes<ExampleRes> example(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "perPage", defaultValue = "10") @Min(1) int perPage,
            @ModelAttribute @Validated ExampleReq exampleReq
    ) {
        PaginationReq paginationReq = new PaginationReq(page, perPage);
        return SuccessRes.of(exampleService.example(paginationReq, exampleReq));
    }
}
