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
public class SurveyRepository extends JpaRepository<Survey,Long> {

    public Optional<Survey> findById(Long surveyId);

    public Page<Survey> findByCreatedBy(Long userId, Pageable pageable);

    public long countByCreatedBy(Long userId);

    public List<Survey> findByIdIn(List<Long> surveyIds);

    public List<Survey> findByIdIn(List<Long> surveyIds, Sort sort);

}
