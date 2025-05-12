package insty.domain.example.implement;

import insty.domain.example.repository.ExampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExampleReader {

    private final ExampleRepository exampleRepository;

    public void readExample() {
    }
}
