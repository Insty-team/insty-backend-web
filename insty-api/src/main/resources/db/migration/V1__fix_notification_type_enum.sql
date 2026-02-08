UPDATE web_service.user_notification_settings
SET notification_type = 'NEW_COURSE_QUESTION'
WHERE notification_type = 'NEW_COMMUNITY_QUESTION';
