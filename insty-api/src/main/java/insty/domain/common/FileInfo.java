package insty.domain.common;

import insty.model.file.File;

public record FileInfo(
        Long id,
        String name,
        String contentType,
        long size,
        String url
) {

    public static FileInfo from(File file, String domain) {
        return new FileInfo(file.getId(), file.getOriginalName(), file.getContentType(), file.getSize(),
                file.getUrl(domain));
    }
}
