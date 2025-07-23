package insty.domain.community.implement;

import insty.domain.common.FileCreateReq;
import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.error.CommunityErrorCode;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CommunityAnswerFileWriter {

    private final FileWriter fileWriter;
    private final CommunityAnswerFileRepository communityAnswerFileRepository;
    private final AppProperties appProperties;

    // todo : 필요시 설정 값으로 이동
    private final int MAX_FILE_COUNT = 10;

    /**
     * 답변 이미지 파일 저장
     */
    public List<FileInfo> saveAnswerFiles(CommunityAnswer answer, List<MultipartFile> addFiles) {
        checkMaxFileCount(answer, addFiles, List.of());
        saveFiles(answer, addFiles);
        return communityAnswerFileRepository.findAllByCommunityAnswerId(answer.getId()).stream()
                .map(caf -> FileInfo.from(caf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 답변 파일 업데이트 (삭제/추가 분리)
     */
    public List<FileInfo> updateAnswerFiles(CommunityAnswer answer, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        checkMaxFileCount(answer, addFiles, deleteFileIds);
        // 삭제
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            communityAnswerFileRepository.deleteByAnswerIdAndFileIdIn(answer.getId(), deleteFileIds);
        }
        // 추가
        saveFiles(answer, addFiles);
        // 최종 파일 정보 반환
        return communityAnswerFileRepository.findAllByCommunityAnswerId(answer.getId()).stream()
                .map(caf -> FileInfo.from(caf.getFile(), appProperties.getDomain()))
                .toList();
    }

    /**
     * 답변 파일 저장 (공통)
     */
    private List<CommunityAnswerFile> saveFiles(CommunityAnswer answer, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        List<FileCreateReq> fileCreateReqs = files.stream()
                .map(file -> new FileCreateReq(file, FileContainerType.ANSWER_IMAGE, answer.getId()))
                .toList();
        List<File> savedFiles = fileWriter.saveFiles(fileCreateReqs);
        List<CommunityAnswerFile> answerFiles = savedFiles.stream()
                .map(file -> CommunityAnswerFile.create(answer, file))
                .toList();
        communityAnswerFileRepository.saveAll(answerFiles);
        return answerFiles;
    }


    /**
     * 최대 파일 개수 체크
     */
    private void checkMaxFileCount(CommunityAnswer answer, List<MultipartFile> addFiles, List<Long> deleteFileIds) {
        int currentCount = communityAnswerFileRepository.countByCommunityAnswerId(answer.getId());
        int addCount = (addFiles == null) ? 0 : (int) addFiles.stream().filter(f -> f != null && !f.isEmpty()).count();
        int deleteCount = (deleteFileIds == null) ? 0 : deleteFileIds.size();
        int finalCount = currentCount - deleteCount + addCount;
        if (finalCount > MAX_FILE_COUNT) {
            throw new insty.exception.CustomException(CommunityErrorCode.COMMUNITY_MAX_FILE_COUNT_EXCEEDED);
        }
    }
}