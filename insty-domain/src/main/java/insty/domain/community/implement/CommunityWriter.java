public void deleteAnswer(CommunityAnswer communityAnswer) {
    communityAnswerRepository.delete(communityAnswer);
}

public void acceptAnswer(CommunityQuestion communityQuestion, CommunityAnswer communityAnswer) {
    // 기존에 채택된 답변이 있다면 해제
    List<CommunityAnswer> existingAnswers = communityQuestion.getAnswers();
    if (existingAnswers != null) {
        existingAnswers.stream()
                .filter(CommunityAnswer::isAccepted)
                .forEach(CommunityAnswer::unaccept);
    }
    
    // 새로운 답변 채택
    communityAnswer.accept();
    communityQuestion.markAsAnswered();
    
    communityAnswerRepository.save(communityAnswer);
    communityQuestionRepository.save(communityQuestion);
}

public void unacceptAnswer(CommunityQuestion communityQuestion) {
    // 모든 답변의 채택 상태 해제
    List<CommunityAnswer> answers = communityQuestion.getAnswers();
    if (answers != null) {
        answers.stream()
                .filter(CommunityAnswer::isAccepted)
                .forEach(answer -> {
                    answer.unaccept();
                    communityAnswerRepository.save(answer);
                });
    }
    
    communityQuestion.markAsUnanswered();
    communityQuestionRepository.save(communityQuestion);
} 