# API 엔드포인트 정리

- 기준: `insty-api/src/main/java` 컨트롤러 어노테이션
- Summary/Description: `@Operation` 기준(없으면 간단 요약)
- Auth: `@PreAuthorize` 또는 `@SecurityRequirement` 기준

## Health
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| GET | /api/health | 헬스 체크 | - | 없음 | `@Hidden` |

## 인증/토큰
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| POST | /api/v1/auth/login | 사용자 이메일 로그인 | 이메일과 비밀번호로 로그인합니다. | 없음 | - |
| GET | /api/v1/auth/login/authorize/{socialName} | 사용자 소셜 로그인 인가코드 얻기 | 소셜 로그인을 하기위해 인가코드를 얻습니다. | 없음 | - |
| POST | /api/v1/auth/login/{socialName} | 사용자 소셜 로그인 | 소셜 인증으로 로그인합니다. | 없음 | - |
| POST | /api/v1/auth/reissue | AccessToken 재발급 | RefreshToken을 이용해 AccessToken을 재발급받습니다. | JWT | Authorization 헤더의 RefreshToken 사용 |
| POST | /api/v1/auth/logout | 사용자 로그아웃 | 로그아웃을 요청합니다. | JWT | - |
| POST | /api/v1/auth/email-verification/send | 이메일 인증 | 요청한 메일로 이메일 인증 번호가 전달됩니다. | 없음 | - |
| POST | /api/v1/auth/email-verification/verify | 이메일 인증 확인 | 이메일과 인증코드로 확인을 합니다. | 없음 | - |
| POST | /api/v1/auth/password-reset/send-mail | 비밀번호 찾기 이메일 전송 | 비밀번호 변경을 위해서 이메일을 통해 인증번호를 사용자에게 전송한다 | 없음 | - |
| POST | /api/v1/auth/password-reset/verify | 이메일 인증코드 기반으로 인증 | 전송된 이메일 인증코드를 기반으로 사용자를 검증한다 | 없음 | - |
| POST | /api/v1/auth/password-reset/update | 이메일 인증된 상태에서 비밀번호 변경 | 이메일 인증코드로 인증된 사용자가 비밀번호를 변경한다 | 없음 | - |

## 유저
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| POST | /api/v1/users | 이메일 회원 가입 | 이메일로 회원 가입을 진행합니다. | 없음 | - |
| GET | /api/v1/users/email/check | 이메일 중복 체크 | 이메일이 이미 사용중인지 중복체크를 합니다. | 없음 | - |
| GET | /api/v1/users/nickname/check | 닉네임 중복 체크 | 닉네임이 이미 사용중인지 중복체크를 합니다. | 없음 | - |
| GET | /api/v1/users/profile | 내 사용자 정보 조회 | 사용자가 가지고 있는 토큰 기반으로 사용자 정보를 조회합니다. | JWT (LEARNER/CREATOR) | - |
| PUT | /api/v1/users/profile/me | 내 사용자 정보 수정 | 내 사용자 정보를 수정합니다. | JWT (LEARNER/CREATOR) | multipart/form-data |
| PATCH | /api/v1/users/profile/userType | 사용자 타입 변경 | 사용자 타입을 변경합니다. | JWT (LEARNER/CREATOR) | - |
| PATCH | /api/v1/users/profile/email-agree | 사용자 이메일 수신 동의 상태 값 변경 | 로그아웃을 요청합니다. | JWT (LEARNER/CREATOR) | - |
| PATCH | /api/v1/users/profile/password | 내 비밀번호 수정 수정 | 내 비밀번호를 수정합니다. | JWT (LEARNER/CREATOR) | - |
| DELETE | /api/v1/users/withdraw | 탈퇴 | 사용자 정보를 탈퇴합니다. | JWT (LEARNER/CREATOR) | - |

## 유저 알림 설정 (사용중지)
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| GET | /api/v1/users/notification-preferences | 사용자 알림 설정 조회 (사용중지) | 로그인 사용자의 알림/이메일 수신 설정 조회. | JWT (LEARNER/CREATOR) | Deprecated, `CommonErrorCode.DEPRECATED_API` |
| PUT | /api/v1/users/notification-preferences | 사용자 알림 설정 변경(사용중지) | 알림 타입별 인앱/이메일 수신 설정 변경. | JWT (LEARNER/CREATOR) | Deprecated, `CommonErrorCode.DEPRECATED_API` |

## 강의
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| POST | /api/v1/courses | 강의 게시 | 새로운 강의를 게시한다. | JWT (CREATOR) | multipart/form-data |
| PUT | /api/v1/courses/{courseId} | 강의 수정 | 강의를 수정한다. | JWT (CREATOR) | multipart/form-data |
| DELETE | /api/v1/courses/{courseId} | 강의 삭제 | 강의를 삭제한다. | JWT (CREATOR) | - |
| GET | /api/v1/courses/creator/{courseId} | 강의 상세조회(크리에이터용) - 제거 예정 | `/api/v1/courses/{courseId}`와 동일하며 곧 제거될 예정입니다. | JWT (CREATOR) | Deprecated |
| GET | /api/v1/courses/{courseId} | 강의 상세조회 | 강의를 상세조회한다. | JWT (LEARNER/CREATOR) | - |
| GET | /api/v1/courses | 강의 목록조회 | 강의 목록을 조회한다. | JWT (LEARNER/CREATOR) | - |
| GET | /api/v1/courses/my | 내가 업로드한 강의 목록조회 | 해당 크리에이터가 업로드한 강의 목록을 조회한다. | JWT (CREATOR) | - |
| GET | /api/v1/courses/courseProgress | 내가 수강중인 강의 목록조회 | 해당 러너가 수강중인 강의 목록을 조회한다. | JWT (LEARNER) | - |
| POST | /api/v1/courses/courseProgress/{courseId} | 강좌 수강하기 | 러너가 수강신청을 통해 강의를 수강한다. | JWT (LEARNER) | - |
| GET | /api/v1/courses/courseProgress/{courseId}/exists | 강좌 수강 여부 조회 | userId와 courseId를 기준으로 강좌 수강 여부를 단일 조회한다. | JWT (LEARNER) | - |
| PUT | /api/v1/courses/{courseId}/visibility | 강좌의 visible 상태 변경 | 강좌의 visible상태를 변경함으로써 러너에게 보여질지 말지를 결정할 수 있다. | JWT (CREATOR) | - |

## 강의 Q&A
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| GET | /api/v1/courses/{courseId}/questions | 질문 목록 검색 (Q&A/강좌 공용) | 질문 목록을 조회한다. (제목/내용 키워드 검색) | JWT (LEARNER/CREATOR) | - |
| GET | /api/v1/courses/{courseId}/questions/me | 내가 작성한 질문 검색 (Q&A/강좌 공용) | 러너 자신이 작성한 질문 목록을 조회한다. (인증 사용자 기준) | JWT (LEARNER) | - |
| GET | /api/v1/courses/{courseId}/questions/{questionId} | 질문 상세 조회 | 질문의 상세 정보를 조회한다. (답변 최신순) | JWT (LEARNER/CREATOR) | - |
| POST | /api/v1/courses/{courseId}/questions | 질문 작성 | 질문을 생성한다. | JWT (LEARNER) | multipart/form-data |
| PATCH | /api/v1/courses/{courseId}/questions/{questionId} | 질문 수정 | 질문을 수정한다. | JWT (LEARNER) | multipart/form-data |
| DELETE | /api/v1/courses/{courseId}/questions/{questionId} | 질문 삭제 | 질문을 삭제한다. | JWT (LEARNER) | - |
| GET | /api/v1/courses/{courseId}/questions/{questionId}/answers | 답변 조회 | 질문의 답변 목록을 페이지네이션으로 조회한다. (최신순) | JWT (LEARNER/CREATOR) | - |
| GET | /api/v1/courses/{courseId}/questions/{questionId}/answers/accepted | 채택된 답변 조회 | 질문에서 채택된 답변을 조회한다. | JWT (LEARNER/CREATOR) | - |
| POST | /api/v1/courses/{courseId}/questions/{questionId}/answers | 답변 작성 | 질문에 답변을 생성한다. | JWT (LEARNER/CREATOR) | multipart/form-data |
| PATCH | /api/v1/courses/{courseId}/questions/{questionId}/answers/{answerId} | 답변 수정 | 답변을 수정한다. | JWT (LEARNER/CREATOR) | multipart/form-data |
| DELETE | /api/v1/courses/{courseId}/questions/{questionId}/answers/{answerId} | 답변 삭제 | 답변을 삭제한다. | JWT (LEARNER/CREATOR) | - |
| POST | /api/v1/courses/{courseId}/questions/{questionId}/answers/{answerId}/accept | 답변 채택 | 질문자가 답변을 채택/해제한다. (토글) | JWT (LEARNER) | - |

## 멘션
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| GET | /api/v1/mentions/users/search | 멘션 가능한 사용자 검색 | 멘션할 수 있는 사용자 목록을 검색한다. (본인 제외) | JWT (LEARNER/CREATOR) | - |

## 알림
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| GET | /api/v1/notification | 사용자 알림 조회 | 로그인한 사용자의 알림 목록을 조회한다. | JWT (LEARNER/CREATOR) | - |
| POST | /api/v1/notification/{notificationId}/read | 알림 읽음 처리 및 이동 | 알림을 읽음 처리하고, 해당 알림의 리다이렉트 URL을 반환한다. | JWT (LEARNER/CREATOR) | - |

## 사용자 알림 설정
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| GET | /api/v1/notification/settings | 내 알림 설정 조회 | 로그인한 사용자의 모든 알림 타입별 설정을 조회합니다. | JWT (LEARNER/CREATOR) | - |
| PUT | /api/v1/notification/settings | 특정 알림 타입 설정 변경 | 특정 알림 타입에 대한 인앱/이메일 설정을 변경합니다. | JWT (LEARNER/CREATOR) | - |
| PUT | /api/v1/notification/settings/bulk | 모든 알림 일괄 설정 | 모든 알림 타입의 설정을 한 번에 켜거나 끕니다. | JWT (LEARNER/CREATOR) | - |

## 영상
| Method | Path | Summary | Description | Auth | Notes |
| --- | --- | --- | --- | --- | --- |
| POST | /api/v1/videos/upload/course | 강의 영상 업로드 | 강의 영상을 업로드하기 위한 URL을 제공받는다. | JWT (CREATOR) | - |
| POST | /api/v1/videos/upload/question | 질문 영상 업로드 | 질문 영상을 업로드하기 위한 URL을 제공받는다. | JWT (LEARNER) | - |
| POST | /api/v1/videos/upload/answer | 답변 영상 업로드 | 답변 영상을 업로드하기 위한 URL을 제공받는다. | JWT (LEARNER/CREATOR) | - |
| GET | /api/v1/videos/{videoUuid}/thumbnail | 영상 썸네일 조회 | 영상에 대한 썸네일을 제공받는다. | JWT (LEARNER/CREATOR) | - |
| POST | /api/v1/videos/playlist | 영상 조회 | HLS 영상 url을 제공받는다. | JWT (LEARNER/CREATOR) | 응답에 쿠키 포함 |
| POST | /api/v1/videos/preview | 영상 미리보기 | 1분 미리보기 영상 url을 제공받는다. | JWT (LEARNER/CREATOR) | 응답에 쿠키 포함 |
