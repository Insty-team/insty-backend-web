package insty.domain.example.service;

import insty.domain.common.PaginationReq;
import insty.domain.example.dto.ExampleReq;
import insty.domain.example.dto.ExampleRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExampleService {

    public ExampleRes example(PaginationReq paginationReq, ExampleReq exampleReq) {
        return new ExampleRes();
    }
}
