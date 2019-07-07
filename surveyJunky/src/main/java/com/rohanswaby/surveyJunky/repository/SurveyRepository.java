package com.rohanswaby.surveyJunky.repository;

import com.rohanswaby.surveyJunky.models.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public abstract class SurveyRepository implements JpaRepository<Survey, Long> {

    public abstract Optional<Survey> findById(Long surveyId);

    public abstract Page<Survey> findByCreatedBy(Long userId, Pageable pageable);

    public abstract long countByCreatedBy(Long userId);

    public abstract List<Survey> findByIdIn(List<Long> surveyIds);

    public abstract List<Survey> findByIdIn(List<Long> surveyIds, Sort sort);

}
