package insty.domain.user.service;

import insty.domain.user.dto.request.UserNotificationPreferenceUpdateReq;
import insty.domain.user.dto.response.UserNotificationPreferenceRes;
import insty.domain.user.implement.UserNotificationPreferenceReader;
import insty.domain.user.implement.UserNotificationPreferenceWriter;
import insty.domain.user.implement.UserReader;
import insty.model.user.User;
import insty.model.user.UserNotificationPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserNotificationPreferenceService {

    private final UserNotificationPreferenceReader userNotificationPreferenceReader;
    private final UserNotificationPreferenceWriter userNotificationPreferenceWriter;
    private final UserReader userReader;

    public UserNotificationPreferenceRes getUserNotificationPreference(Long userId) {
        User user = userReader.getUser(userId);
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return UserNotificationPreferenceRes.from(preference);
    }

    public UserNotificationPreferenceRes updateUserMentionSettings(Long userId, 
                                                                  boolean notificationEnabled, 
                                                                  boolean emailEnabled) {
        User user = userReader.getUser(userId);
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        UserNotificationPreference updatedPreference = userNotificationPreferenceWriter.updateUserMentionSettings(
                preference, notificationEnabled, emailEnabled);
        return UserNotificationPreferenceRes.from(updatedPreference);
    }

    public UserNotificationPreferenceRes updateNewQuestionSettings(Long userId, 
                                                                  boolean notificationEnabled, 
                                                                  boolean emailEnabled) {
        User user = userReader.getUser(userId);
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        UserNotificationPreference updatedPreference = userNotificationPreferenceWriter.updateNewQuestionSettings(
                preference, notificationEnabled, emailEnabled);
        return UserNotificationPreferenceRes.from(updatedPreference);
    }

    public UserNotificationPreferenceRes updateNewAnswerSettings(Long userId, 
                                                                boolean notificationEnabled, 
                                                                boolean emailEnabled) {
        User user = userReader.getUser(userId);
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        UserNotificationPreference updatedPreference = userNotificationPreferenceWriter.updateNewAnswerSettings(
                preference, notificationEnabled, emailEnabled);
        return UserNotificationPreferenceRes.from(updatedPreference);
    }

    public UserNotificationPreferenceRes updateAnswerAcceptedSettings(Long userId, 
                                                                     boolean notificationEnabled, 
                                                                     boolean emailEnabled) {
        User user = userReader.getUser(userId);
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        UserNotificationPreference updatedPreference = userNotificationPreferenceWriter.updateAnswerAcceptedSettings(
                preference, notificationEnabled, emailEnabled);
        return UserNotificationPreferenceRes.from(updatedPreference);
    }

    public UserNotificationPreferenceRes updateAllSettings(Long userId, UserNotificationPreferenceUpdateReq req) {
        User user = userReader.getUser(userId);
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);

        userNotificationPreferenceWriter.updateUserMentionSettings(
                preference, req.userMentionNotificationEnabled(), req.userMentionEmailEnabled());
        userNotificationPreferenceWriter.updateNewQuestionSettings(
                preference, req.newQuestionNotificationEnabled(), req.newQuestionEmailEnabled());
        userNotificationPreferenceWriter.updateNewAnswerSettings(
                preference, req.newAnswerNotificationEnabled(), req.newAnswerEmailEnabled());
        userNotificationPreferenceWriter.updateAnswerAcceptedSettings(
                preference, req.answerAcceptedNotificationEnabled(), req.answerAcceptedEmailEnabled());
        userNotificationPreferenceWriter.updateRequestedCourseRegistrationSettings(
                preference, req.requestedCourseRegistrationNotificationEnabled(), req.requestedCourseRegistrationEmailEnabled());

        return UserNotificationPreferenceRes.from(preference);
    }

    @Transactional(readOnly = true)
    public boolean shouldReceiveUserMentionEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveUserMentionEmail();
    }

    @Transactional(readOnly = true)
    public boolean shouldReceiveNewQuestionEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveNewQuestionEmail();
    }

    @Transactional(readOnly = true)
    public boolean shouldReceiveNewAnswerEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveNewAnswerEmail();
    }

    @Transactional(readOnly = true)
    public boolean shouldReceiveAnswerAcceptedEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveAnswerAcceptedEmail();
    }

    @Transactional(readOnly = true)
    public boolean shouldReceiveRequestedCourseRegistrationEmail(User user) {
        UserNotificationPreference preference = userNotificationPreferenceReader.getOrCreateDefaultPreference(user);
        return preference.shouldReceiveRequestedCourseRegistrationEmail();
    }
}