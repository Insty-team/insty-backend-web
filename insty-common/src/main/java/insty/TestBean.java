package insty;

import org.springframework.stereotype.Component;

// TODO - 프로젝트 초기 설계 후 삭제 요망
@Component
public class TestBean {

    public void dependencyTest() {
        System.out.println("성공적으로 로딩됐습니다.");
    }
}