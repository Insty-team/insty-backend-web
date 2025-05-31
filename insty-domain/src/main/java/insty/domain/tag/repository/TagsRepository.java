package insty.domain.tag.repository;

import insty.model.tag.Tags;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepository extends JpaRepository<Tags, Long> {

    Set<Tags> findByTagNameIn(Set<String> tagNames);
}
