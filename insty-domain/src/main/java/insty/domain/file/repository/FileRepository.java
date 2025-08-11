package insty.domain.file.repository;

import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findAllByContainerTypeAndContainerId(FileContainerType containerType, Long containerId);

    List<File> findAllByContainerTypeAndContainerIdIn(FileContainerType containerType, List<Long> containerIds);
}
