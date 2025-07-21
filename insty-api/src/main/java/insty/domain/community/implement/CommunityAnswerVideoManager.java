package insty.domain.community.implement;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityAnswerVideoManager {


    /**
     * 답변에 비디오 추가(교체)
     */
    public UUID saveAnswerVideo(UUID uuid){
        if(uuid == null){
            return null;
        }
        // todo : UUID 비디오가 등록된 존재인지 검증
        UUID updatedVideoUrl = UUID.randomUUID();
        // todo : 답변 비디오 업데이트
        return updatedVideoUrl;
    }


}
