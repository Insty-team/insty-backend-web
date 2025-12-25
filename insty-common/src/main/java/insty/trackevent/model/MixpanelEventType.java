package insty.trackevent.model;

// 이벤트 네이밍: <DOMAIN>_<ACTION>_<RESULT/STATE>
public enum MixpanelEventType {

    // 공용
    NONE,                       // 이벤트에 사용하는 용도가 아닌, 내부 처리용

    // USER
    AUTH_SIGNED_UP,             // 회원가입 완료(성공)
    AUTH_SIGNUP_FAILED,         // 회원가입 실패(롤백/예외)
    AUTH_LOGGED_IN,             // 로그인 성공
    AUTH_LOGIN_FAILED,          // 로그인 실패
    AUTH_EMAIL_VERIFIED,        // 이메일 인증 완료
    AUTH_EMAIL_VERIFY_FAILED,   // 이메일 인증 실패

    // COURSE
    COURSE_LEARNING_STARTED,       // 강의 수강 시작(성공: 등록/진행 상태 전이 커밋 후)
    COURSE_LEARNING_START_FAILED,  // 강의 수강 시작 실패(등록/상태 전이 실패)

    // COURSE
    COURSE_QUESTION_CREATED,    // 질문 작성 성공
    COURSE_QUESTION_CREATE_FAILED, // 질문 작성 실패
    COURSE_ANSWER_CREATED,      // 답변 작성 성공
    COURSE_ANSWER_CREATE_FAILED,// 답변 작성 실패
    COURSE_ANSWER_ACCEPTED,     // 답변 채택 성공
    COURSE_ANSWER_ACCEPT_FAILED // 답변 채택 실패
}