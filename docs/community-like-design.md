# 커뮤니티 좋아요(Like) 기능 설계

## 1) 목적 / 배경
- 커뮤니티 **게시글/댓글**에 좋아요를 추가한다.
- 사용자는 좋아요 버튼을 누르면 좋아요가 추가되고, 다시 누르면 제거된다(토글 UX).
- 좋아요 수는 다른 사용자에게도 보여야 하며, 사용자가 이미 좋아요를 눌렀는지 여부도 UI에 필요하다.
- 동시에 “커뮤니티 포스트 수 정보”를 제공할 수 있어야 한다(코스별 게시글 총 개수 등).

## 2) 범위(Scope)
### In-scope
- 게시글 좋아요 생성/삭제(사용자 기준 1개만 가능)
- 댓글 좋아요 생성/삭제(사용자 기준 1개만 가능)
- 좋아요 수 노출(게시글/댓글)
- “내가 좋아요 눌렀는지” 여부 노출(게시글/댓글)
- 코스별 커뮤니티 게시글 총 개수 조회(“포스트 수 정보”)

### Out-of-scope (이번 기능에서 제외)
- 좋아요한 사용자 목록 조회(누가 눌렀는지 리스트)
- 알림(좋아요 알림), 배지/랭킹
- 신고/차단과 연동된 노출 정책

## 3) 핵심 요구사항
1. 한 사용자는 동일 게시글에 좋아요를 **중복으로 생성할 수 없다**.
2. 한 사용자는 동일 댓글에 좋아요를 **중복으로 생성할 수 없다**.
3. 좋아요 버튼을 다시 누르면 **해당 좋아요가 제거**된다.
4. 게시글/댓글 상세 및 목록에서 **좋아요 수**를 볼 수 있어야 한다.
5. 게시글/댓글에서 **내 좋아요 상태(likedByMe)** 를 알 수 있어야 한다.
6. “커뮤니티 포스트 수 정보”를 조회할 수 있어야 한다(코스별).

## 4) 데이터 모델 설계
> 기존 프로젝트 컨벤션에 맞춰 `insty-domain`에 JPA Entity/Repository를 두고, API/서비스 로직은 `insty-api`에 둔다.

### 4.1) 테이블
#### A. `community_post_likes`
- `id` (PK, bigint)
- `community_post_id` (FK -> `community_posts.id`, not null)
- `user_id` (FK -> `users.id`, not null)
- `created_at`, `updated_at` (BaseEntity 사용 시)
- **UNIQUE(`community_post_id`, `user_id`)**
- Index: (`community_post_id`), (`user_id`)

#### B. `community_comment_likes`
- `id` (PK, bigint)
- `community_comment_id` (FK -> `community_comments.id`, not null)
- `user_id` (FK -> `users.id`, not null)
- `created_at`, `updated_at`
- **UNIQUE(`community_comment_id`, `user_id`)**
- Index: (`community_comment_id`), (`user_id`)

### 4.2) 집계 컬럼(성능/조회提醒)
좋아요 수를 매 요청마다 COUNT로 계산하면 목록/댓글에서 부하가 커질 수 있으므로,
아래 **집계 컬럼(denormalized count)** 을 두는 방식을 권장한다.

- `community_posts.like_count` (int, default 0, not null)
- `community_comments.like_count` (int, default 0, not null)

> 참고: 기존 `Course.likeCount` 패턴과 일관성 유지.

## 5) API 설계
> 토글 UX를 지원하되, 서버 API는 **명시적 like/unlike** 를 제공하는 것을 권장(멱등성 확보, 동시성/재시도에 안전).

### 5.1) 게시글 좋아요
- `POST /api/v1/community/courses/{courseId}/posts/{postId}/likes`
  - 동작: 좋아요 생성(이미 존재하면 멱등 성공)
  - 응답(예): `{ likedByMe: true, likeCount: 10 }`
- `DELETE /api/v1/community/courses/{courseId}/posts/{postId}/likes`
  - 동작: 좋아요 제거(없으면 멱등 성공)
  - 응답(예): `{ likedByMe: false, likeCount: 9 }`

### 5.2) 댓글 좋아요
- `POST /api/v1/community/comments/{commentId}/likes`
- `DELETE /api/v1/community/comments/{commentId}/likes`

> 댓글은 현재 라우팅이 `/api/v1/community/comments/{commentId}` 형태로 존재하므로 이를 따름.

### 5.3) 조회 응답에 포함할 필드
#### 게시글 목록/상세
- `likeCount` (int)
- `likedByMe` (boolean) — 인증 사용자 기준

#### 댓글 목록
- `likeCount` (int)
- `likedByMe` (boolean)

### 5.4) “커뮤니티 포스트 수 정보”
선택지 A(권장): 별도 API
- `GET /api/v1/community/courses/{courseId}/posts/count`
  - 응답(예): `{ count: 123 }`

선택지 B: 이미 존재하는 검색 응답의 `pagination.totalElements` 를 활용
- 목록 API가 항상 total을 내려주므로, UI가 “총 게시글 수” 표시만 필요하면 별도 API 없이도 가능
- 다만 **페이지 호출 없이 count만 필요한 화면** 이 있으면 선택지 A가 유리

## 6) 서비스/도메인 구성(패키지 기준)
### 6.1) Domain(Entity/Repository) - `insty-domain`
- `insty.model.community.CommunityPostLike`
- `insty.model.community.CommunityCommentLike`
- `insty.domain.community.repository.CommunityPostLikeRepository`
- `insty.domain.community.repository.CommunityCommentLikeRepository`

### 6.2) API Layer - `insty-api`
- Controller
  - `CommunityPostController`에 post like/unlike endpoint 추가
  - `CommunityCommentController`에 comment like/unlike endpoint 추가
- Service/Implement
  - `CommunityPostLikeManager`(또는 `CommunityLikeManager`) : 생성/삭제 + count 업데이트 담당
  - `CommunityPostService`, `CommunityCommentService`는 manager를 호출해 응답 DTO에 반영
- Validator
  - 게시글/댓글 존재/삭제 여부 검증은 기존 `CommunityValidator`를 재사용

## 7) 트랜잭션/동시성
### 7.1) 중복 좋아요 방지
- DB UNIQUE 제약으로 최종 보장
- 애플리케이션에서는 “존재 확인 후 insert”를 하더라도 경쟁 상태가 있으므로,
  UNIQUE 위반은 **멱등 처리**(이미 좋아요 상태로 응답)하거나 409로 변환한다.

### 7.2) likeCount 일관성
권장: like/unlike 시 `like_count` 를 DB에서 원자적으로 업데이트
- like: `UPDATE community_posts SET like_count = like_count + 1 WHERE id = :postId`
- unlike: `UPDATE community_posts SET like_count = like_count - 1 WHERE id = :postId AND like_count > 0`

> JPA 엔티티 save 기반 증감은 동시성에서 누락될 수 있으므로, 가능한 한 “증감용 update query”를 사용한다.

## 8) 에러 처리(제안)
기존 `CommunityErrorCode` 외에 아래를 추가할 수 있다(선택):
- `COMMUNITY_POST_LIKE_ALREADY_EXISTS` (409) — 멱등 처리 대신 명시적 에러가 필요할 때
- `COMMUNITY_POST_LIKE_NOT_FOUND` (404/409) — unlike 시 에러가 필요할 때
- 댓글도 동일하게 추가 가능

본 설계에서는 클라이언트 UX/재시도를 고려해 **POST/DELETE는 멱등 성공**을 권장한다.

## 9) 테스트 전략
- 단위 테스트(서비스/매니저):
  - like 생성 시: like row 생성 + likeCount 증가
  - like 중복 요청: likeCount 증가하지 않음(멱등)
  - unlike 시: like row 삭제 + likeCount 감소
  - unlike 중복 요청: likeCount 감소하지 않음(멱등)
- 통합 테스트(가능 시):
  - 실제 DB(H2)에서 UNIQUE 제약 동작 검증
  - 게시글/댓글 조회 응답에 likeCount/likedByMe 반영 검증

## 10) 스키마 반영(운영 반영 가이드)
현재 prod 환경이 `ddl-auto: validate` 이므로 운영 반영 시 DB DDL이 선행되어야 한다.
- `schema.sql`(test) 업데이트
- 운영 DB에 아래 변경 반영(예시):
  - `ALTER TABLE web_service.community_posts ADD COLUMN like_count INT NOT NULL DEFAULT 0;`
  - `ALTER TABLE web_service.community_comments ADD COLUMN like_count INT NOT NULL DEFAULT 0;`
  - `CREATE TABLE web_service.community_post_likes (..., UNIQUE(community_post_id, user_id), ...);`
  - `CREATE TABLE web_service.community_comment_likes (..., UNIQUE(community_comment_id, user_id), ...);`

## 11) 결정 필요(질문)
1. 좋아요 API는 “토글 1개”(`POST /likes/toggle`)로 갈지, “명시적 POST/DELETE”로 갈지?
2. 좋아요 상태(`likedByMe`)는 목록에도 포함할지, 상세에서만 포함할지?
3. “커뮤니티 포스트 수 정보”는 별도 count API가 필요한 화면이 있는지?

