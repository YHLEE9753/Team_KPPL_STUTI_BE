package prgrms.project.stuti.domain.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import prgrms.project.stuti.domain.studygroup.model.PreferredMbti;

public interface QuestionRepository extends JpaRepository<PreferredMbti, Long> {
}
