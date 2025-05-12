package insty.domain.example.implement;

import insty.pg.toss.ExampleTossPaymentAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExamplePGProcessor {

    private final ExampleTossPaymentAPI exampleTossPaymentAPI;

    public void process() {
    }
}
