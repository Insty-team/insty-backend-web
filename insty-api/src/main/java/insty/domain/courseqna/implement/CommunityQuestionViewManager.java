package insty.domain.courseqna.implement;

import insty.domain.courseqna.repository.CourseQuestionViewRepository;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CourseQuestionView;
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

	private final CourseQuestionViewRepository courseQuestionViewRepository;
	private final CommunityQuestionReader communityQuestionReader;

	/**
	 * 질문 조회 기록을 업데이트한다.
	 */
	public void recordQuestionView(CourseQuestion question, Long userId) {
		courseQuestionViewRepository.findByQuestionIdAndUserId(question.getId(), userId)
				.ifPresentOrElse(
						CourseQuestionView::updateLastViewedAt,
						() -> {
							try {
								CourseQuestionView newView = CourseQuestionView.create(question, userId);
								courseQuestionViewRepository.save(newView);
							} catch (org.springframework.dao.DataIntegrityViolationException e) {
								courseQuestionViewRepository.findByQuestionIdAndUserId(question.getId(), userId)
										.ifPresent(CourseQuestionView::updateLastViewedAt);
							}
						}
				);
	}

	/**
	 * 질문 작성자 또는 강의 개시자인 경우 조회 기록을 업데이트한다.
	 */
	public void recordQuestionViewIfAuthorOrCreator(Long questionId, Long userId) {
		CourseQuestion question = communityQuestionReader.getCommunityQuestionWithFilesById(questionId);
		
		// 질문 작성자 또는 강의 개시자인 경우에만 조회 기록 업데이트
		Long creatorId = communityQuestionReader.getCreatorIdByQuestionId(questionId);
		if (question.getUser().getId().equals(userId) || creatorId.equals(userId)) {
			recordQuestionView(question, userId);
		}
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
	 * 크리에이터가 마지막으로 질문을 조회한 시점 이후에 새로운 답변이 있는지 확인한다.
	 * @return true: 마지막 조회 이후 새로운 답변이 있음, false: 마지막 조회 이후 새로운 답변 없음
	 */
	public boolean hasNewAnswersAfterCreatorLastView(Long questionId, Long creatorId) {
		return hasNewAnswers(questionId, creatorId);
	}

	/**
	 * 특정 질문에 대해 새로운 답변이 있는지 확인한다.
	 * 조회 기록이 있으면 마지막 조회 시간 이후 타인이 단 답변을 확인하고,
	 * 조회 기록이 없으면 타인이 단 답변이 있는지 확인한다.
	 */
	private boolean hasNewAnswers(Long questionId, Long viewerId) {
		return courseQuestionViewRepository.findByQuestionIdAndUserId(questionId, viewerId)
				.map(view -> courseQuestionViewRepository.hasNewAnswersAfter(questionId, viewerId, view.getLastViewedAt()))
				.orElseGet(() -> courseQuestionViewRepository.existsOtherUserAnswers(questionId, viewerId));
	}

}
