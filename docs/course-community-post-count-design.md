# 강좌 리스트용 커뮤니티 글 개수 설계서

## 1) 목적
- 강좌 리스트 화면에서 각 강좌에 달린 커뮤니티 게시글 수를 표시한다.
- 별도 커뮤니티 count API를 추가하지 않고, 강좌 리스트 응답에 포함한다.

## 2) 범위
### 포함
- 강좌 리스트 API 응답에 `communityPostCount` 필드 추가
- 조회 성능을 고려한 집계 방식 정의

### 제외
- 커뮤니티 게시글 상세/댓글/좋아요와의 연동 UI
- 관리자 통계/리포트

## 3) 요구사항
1. 강좌 리스트에서 각 강좌의 커뮤니티 게시글 수를 확인할 수 있어야 한다.
2. 삭제된 게시글은 개수에 포함하지 않는다(`is_deleted = false`).
3. 페이징된 리스트에서도 정확한 개수가 제공되어야 한다.

## 4) 데이터/집계 방식
### 옵션 A (권장) - 조회 시점 집계
- `community_posts` 테이블에서 강좌 ID 기준으로 COUNT
- 강좌 목록 조회 시 `courseIds` 목록을 받아 `IN (...)` 그룹 카운트 조회

예시 쿼리:
```sql
SELECT course_id, COUNT(*) AS cnt
FROM web_service.community_posts
WHERE course_id IN (:courseIds)
  AND is_deleted = false
GROUP BY course_id;
```

장점: 추가 컬럼/동기화 없음  
단점: 리스트 조회 시 추가 쿼리 필요

### 옵션 B - 캐시/집계 컬럼
- `courses.community_post_count` 컬럼 추가
- 게시글 생성/삭제 시 증감

장점: 리스트 조회가 빠름  
단점: 동기화 로직/마이그레이션 필요

> 현재 요구 사항은 단순 표시이므로 **옵션 A**를 우선 적용한다.

## 5) API 변경
### 대상 API
- 강좌 목록조회: `GET /api/v1/courses` (`CourseSearchInfo`)

> 필요 시 이후 `GET /api/v1/courses/my`, `GET /api/v1/courses/courseProgress`로 확장 가능.  
> 현재 요구사항은 공용 강좌 리스트에만 적용한다.

### 응답 필드 추가
```
communityPostCount: number
```

예시(일부):
```json
{
  "items": [
    {
      "courseId": 1,
      "title": "강좌 제목",
      "communityPostCount": 12
    }
  ]
}
```

> `pagination.totalElements`는 기존 목록 기준을 유지하며, `communityPostCount`는 각 강좌 단위의 수치이다.

## 6) 구현 개요
1. 강좌 리스트 조회 후 `courseIds` 추출
2. `community_posts`에서 `course_id` 기준 그룹 카운트 조회
3. 리스트 DTO에 `communityPostCount` 매핑

## 7) 테스트
- 강좌 리스트 응답에 `communityPostCount`가 포함되는지 확인
- 삭제된 커뮤니티 글은 카운트에 포함되지 않는지 확인
- 게시글이 없는 강좌는 `0`으로 내려가는지 확인

## 8) 오픈 이슈
- 다수 강좌를 조회할 때 카운트 쿼리가 추가되므로, N+1이 발생하지 않도록 반드시 `IN` 그룹 쿼리로 처리한다.
- `community_posts`의 인덱스가 `course_id, is_deleted` 조합인지 확인한다(현재 프로젝트에 인덱스가 존재함).
- 기존 `commentCount` 필드는 QnA용이므로, 커뮤니티 개수는 `communityPostCount`로 분리한다.
- 강좌 노출 조건(`is_show`, `is_deleted`)은 기존 목록 필터를 따르며, 커뮤니티 카운트는 해당 강좌에 연결된 게시글만 집계한다.
