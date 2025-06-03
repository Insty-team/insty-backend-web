package insty.domain.file.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.file.repository.FileRepository;
import insty.model.file.File;
import insty.s3.adapter.S3FileManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FileWriter {

    private final S3FileManager s3FileManager;

    private final FileRepository fileRepository;

    public File saveFile(FileCreateReq req) {
        File file = uploadAndCreateFile(req);
        return fileRepository.save(file);
    }

    public List<File> saveFiles(List<FileCreateReq> reqs) {
        List<File> files = reqs.stream()
                .map(this::uploadAndCreateFile)
                .toList();

        return fileRepository.saveAll(files);
    }

    private File uploadAndCreateFile(FileCreateReq req) {
        String uploadName = s3FileManager.upload(req.file(), req.containerType().toString(),
                req.containerId().toString());
        return File.create(req.containerType(), req.containerId(), uploadName, req.file().getOriginalFilename(),
                req.file().getContentType(), req.file().getSize());
    }
}
