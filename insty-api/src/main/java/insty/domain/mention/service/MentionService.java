package insty.domain.mention.service;

import insty.domain.mention.dto.MentionUserSearchReq;
import insty.domain.mention.dto.MentionUserSearchRes;
import insty.domain.mention.implement.MentionReader;
import insty.domain.mention.implement.MentionWriter;
import insty.domain.user.implement.UserFileReader;
import insty.model.community.CommunityAnswer;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentionService {

    private final MentionReader mentionReader;
    private final MentionWriter mentionWriter;
    private final UserFileReader userFileReader;

    /**
     * 멘션 가능한 사용자 목록을 검색한다
     */
    public List<MentionUserSearchRes> searchMentionableUsers(MentionUserSearchReq req, Long userId) {
        List<User> users = mentionReader.searchMentionableUsers(req.size(), req.keyword(), userId);
        List<MentionUserSearchRes> profileImages = users.stream().map( user ->
                MentionUserSearchRes.from(user, userFileReader.getProfileImageUrl(user))
        ).toList();
        return profileImages;
    }

    /**
     * 댓글에서 멘션을 파싱하고 저장하며 알림을 발송한다
     */
    @Transactional
    public List<Mention> processMentions(CommunityAnswer communityAnswer, User mentionerUser, String content, String questionTitle) {

        List<Mention> mentions = mentionWriter.parseAndSaveMentions(communityAnswer, mentionerUser, content);

        return mentions;
    }
}
