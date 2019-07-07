package com.rohanswaby.surveyJunky.repository;

import com.rohanswaby.surveyJunky.models.ChoiceVoteCount;
import com.rohanswaby.surveyJunky.models.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VoteRepository {

    @Query("SELECT NEW com.example.survey.model.ChoiceVoteCount(vote.choice.id, count(vote.id)) FROM Vote vote WHERE vote.survey.id in :surveyIds GROUP BY vote.choice.id")
    public List<ChoiceVoteCount> countBySurveyIdInGroupByChoiceId(@Param("surveyIds") List<Long> surveyIds);

    @Query("SELECT NEW com.example.survey.model.ChoiceVoteCount(vote.choice.id, count(vote.id)) FROM Vote vote WHERE vote.survey.id = :surveyId GROUP BY vote.choice.id")
    public List<ChoiceVoteCount> countBySurveyIdGroupByChoiceId(@Param("surveyId") Long surveyIds);

    @Query("SELECT vote FROM Vote vote where vote.user.id = :userId and vote.survey.id in :surveyIds")
    public List<Vote> findByUserIdAndSurveyIdIn(@Param("userId") Long userId, @Param("surveyIds") List<Long> surveyIds);

    @Query("SELECT vote FROM Vote vote where vote.user.id = :userId and vote.survey.id = :surveyId")
    public Vote findByUserIdAndSurveyId(@Param("userId") Long userId, @Param("surveyId") Long surveyIds);

    @Query("SELECT COUNT(vote.id) from Vote vote where vote.user.id = :userId")
    public long countByUserId(@Param("userId") Long userId);

    @Query("SELECT vote.survey.id FROM Vote vote WHERE vote.user.id = :userId")
    public Page<Long> findVotedSurveyIdsByUserId(@Param("userId") Long userId, Pageable pageable);

}


