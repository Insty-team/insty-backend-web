package insty.domain.example.service;

import insty.domain.common.PaginationReq;
import insty.domain.example.dto.ExampleReq;
import insty.domain.example.dto.ExampleRes;
import insty.domain.example.implement.ExamplePGProcessor;
import insty.domain.example.implement.ExampleReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleReader exampleReader;
    private final ExamplePGProcessor examplePGProcessor;

    public ExampleRes example(PaginationReq paginationReq, ExampleReq exampleReq) {
        exampleReader.readExample();
        examplePGProcessor.process();

        return new ExampleRes();
    }
}
