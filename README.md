## ✨ Husky & Commitlint

Git 훅을 이용해 커밋 메세지 형식을 강제하여 일관성을 유지합니다.

### 설치

```
npm install
```

- 이후 `.husky`에 `_`폴더 생겼는지 확인

### 커밋 메세지 규칙

- `Type(필수)`: Commit의 종류. commit을 할 때, type에 상응하는 이모지가 자동으로 붙습니다.
- `Scope(선택)`: Commit의 범위. 기능, 함수, 페이지, API 등 자유롭게 선택할 수 있습니다.
- `Subject(필수)`: Commit의 제목. 되도록 간결하게 작성하고, 명사형 어미로 끝나도록 합니다.
- `Body(선택)`: Commit의 내용. 어떤 이유로, 어떻게 변경했는지 작성합니다.
- `Footer(선택)`: Commit의 추가 정보. 이슈 트래킹이나 참고 사항을 기록합니다.

### 헤더 예시

```
<type>(optional scope): <subject>

✨ Feat(login/SignUp): 회원가입 기능 추가
🐛 Fix(login): 로그인 기능 수정
⭐️ Style: 코드 포맷 변경
♻️ Refactor(SignUp): 회원 가입 로직 개선
📁 File: 이미지 파일 추가
✅ Test: 테스트 코드 추가
📝 Docs: README.md 업데이트
🔥 Remove: 사용하지 않는 파일 제거
💚 Ci: 자동 배포 스크립트 변동
🔖 Release: 릴리즈 버전 1.0.3
🔧 Chore: 설정파일 수정
```

### 메세지 구조

```
<type>(optional scope): <subject>

[optional body]

[optional footer(s)]
```

<br>

## 🌿 브랜치 전략

이 프로젝트는 효율적인 협업과 안정적인 배포를 위해 다음과 같은 브랜치 전략을 사용합니다.

### 브랜치 구조

- **main**: 실제 운영(프로덕션) 환경에 배포되는 최종 코드가 관리되는 브랜치입니다.
- **dev**: 기능 개발 및 버그 수정 작업을 병합하는 통합 개발 브랜치입니다.
- **feature/***: 새로운 기능 개발을 위한 브랜치입니다. 브랜치 이름은 `feature/기능명` 형태로 생성합니다.
- **fix/***: 버그 수정 작업을 위한 브랜치입니다. 브랜치 이름은 `fix/버그명` 형태로 생성합니다.

### 브랜치 전략 예시

```
main
│
└── dev
    ├── feature/login
    ├── feature/signup
    └── fix/login-error
```

### 작업 흐름

1. dev 브랜치에서 feature 또는 fix 브랜치를 생성합니다.
2. 각 브랜치에서 작업을 완료한 후 dev로 PR을 보냅니다.
3. dev에서 충분히 테스트한 후 main으로 PR을 보냅니다.
4. main 브랜치는 항상 배포 가능한 상태를 유지합니다.