package insty.uuid;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RandomUuidProvider implements UuidProvider {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
