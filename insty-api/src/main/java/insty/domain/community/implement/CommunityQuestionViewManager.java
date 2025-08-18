package insty.domain.community.implement;

import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionView;
import insty.domain.community.repository.CommunityQuestionViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional
public class CommunityQuestionViewManager {

	private final CommunityQuestionViewRepository communityQuestionViewRepository;

	/**
	 * 질문 조회 기록을 업데이트한다.
	 */
	public void recordQuestionView(CommunityQuestion question, Long userId) {
		communityQuestionViewRepository.findByQuestionIdAndUserId(question.getId(), userId)
				.ifPresentOrElse(
						view -> view.updateLastViewedAt(),
						() -> {
							CommunityQuestionView newView = CommunityQuestionView.create(question, userId);
							communityQuestionViewRepository.save(newView);
						}
				);
	}

	/**
	 * 여러 질문에 대해 새로운 답변 존재 여부를 조회한다.
	 * 각 질문별로 마지막 조회 시간 이후 타인이 단 답변이 있는지 확인한다.
	 */
	public Map<Long, Boolean> getHasNewAnswersForQuestions(List<Long> questionIds, Long viewerId) {
		return questionIds.stream()
				.collect(Collectors.toMap(
						questionId -> questionId,
						questionId -> hasNewAnswers(questionId, viewerId)
				));
	}

	/**
	 * 특정 질문에 대해 새로운 답변이 있는지 확인한다.
	 * 조회 기록이 있으면 마지막 조회 시간 이후 타인이 단 답변을 확인하고,
	 * 조회 기록이 없으면 타인이 단 답변이 있는지 확인한다.
	 */
	private boolean hasNewAnswers(Long questionId, Long viewerId) {
		return communityQuestionViewRepository.findByQuestionIdAndUserId(questionId, viewerId)
				.map(view -> communityQuestionViewRepository.hasNewAnswersAfter(questionId, viewerId, view.getLastViewedAt()))
				.orElseGet(() -> communityQuestionViewRepository.existsOtherUserAnswers(questionId, viewerId));
	}
}
