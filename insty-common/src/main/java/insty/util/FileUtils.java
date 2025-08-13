package insty.util;

import java.util.Optional;

public class FileUtils {

    public static Optional<String> extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(fileName.substring(dotIndex + 1));
    }

    public static String getFilePath(String directory, String key, String fileName) {
        return "file/" + directory + "/" + key + "/" + fileName;
    }
}
