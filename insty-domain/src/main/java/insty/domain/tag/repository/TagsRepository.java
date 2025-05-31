package insty.domain.tag.repository;

import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagsRepository extends JpaRepository<Tags, Long> {

    List<Tags> findByTagNameIn(Set<String> tagNames);
}
