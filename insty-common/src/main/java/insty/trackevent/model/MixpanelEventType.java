package insty.trackevent.model;

// 이벤트 네이밍: <DOMAIN>_<ACTION>_<RESULT/STATE>
public enum MixpanelEventType {

    // USER
    AUTH_SIGNED_UP,          // 회원가입 완료
    AUTH_LOGGED_IN,          // 로그인 성공
    AUTH_EMAIL_VERIFIED,     // 이메일 인증 완료

    // COURSE
    COURSE_VIEWED,           // 강의 상세 진입
    COURSE_LEARNING_STARTED, // 강의 수강 시작

    // COMMUNITY
    COMMUNITY_QUESTION_CREATED, // 질문 작성
    COMMUNITY_ANSWER_CREATED,   // 답변 작성
    COMMUNITY_ANSWER_ACCEPTED   // 답변 채택
}