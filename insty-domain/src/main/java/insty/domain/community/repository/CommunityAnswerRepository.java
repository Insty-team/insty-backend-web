package insty.domain.community.repository;

import insty.model.community.CommunityAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityAnswerRepository extends JpaRepository<CommunityAnswer, Long> {

    //Optional<CommunityAnswer> getCommunityAnswer(Long answerId);
    List<CommunityAnswer> findAllByCommunityQuestionId(Long questionId);
}
