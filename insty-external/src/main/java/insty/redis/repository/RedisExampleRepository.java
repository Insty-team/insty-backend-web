package insty.redis.repository;

import insty.redis.domain.RedisExampleEntity;
import org.springframework.data.repository.CrudRepository;

public interface RedisExampleRepository extends CrudRepository<RedisExampleEntity, String> {

}
