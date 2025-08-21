package insty.domain.mention.service;

import insty.domain.mention.dto.MentionedUserInfo;
import insty.domain.mention.dto.MentionUserSearchReq;
import insty.domain.mention.dto.MentionUserSearchRes;
import insty.domain.mention.implement.MentionParser;
import insty.domain.mention.implement.MentionReader;
import insty.domain.mention.implement.MentionWriter;
import insty.domain.notification.event.MentionCreatedEvent;
import insty.domain.user.implement.UserFileReader;
import insty.model.community.CommunityAnswer;
import insty.model.mention.Mention;
import insty.model.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MentionService {

    private final MentionReader mentionReader;
    private final MentionWriter mentionWriter;
    private final MentionParser mentionParser;
    private final UserFileReader userFileReader;
    private final ApplicationEventPublisher eventPublisher;

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
        List<MentionedUserInfo> mentionedUserInfos = mentionParser.parseMentionedUserInfos(content, mentionerUser);
        mentionWriter.validateMentionCooldown(mentionedUserInfos, mentionerUser);

        List<Mention> savedMentions = mentionWriter.saveMentions(mentionedUserInfos, mentionerUser, communityAnswer);
        for(Mention mention : savedMentions){
            eventPublisher.publishEvent(new MentionCreatedEvent(mention, questionTitle));
        }
        
        return savedMentions;
    }
}
