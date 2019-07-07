package com.rohanswaby.surveyJunky.services;

import com.rohanswaby.surveyJunky.Payload.PagedResponse;
import com.rohanswaby.surveyJunky.Payload.SurveyRequest;
import com.rohanswaby.surveyJunky.Payload.SurveyResponse;
import com.rohanswaby.surveyJunky.Payload.VoteRequest;
import com.rohanswaby.surveyJunky.exception.BadRequestException;
import com.rohanswaby.surveyJunky.exception.ResourceNotFoundException;
import com.rohanswaby.surveyJunky.models.*;
import com.rohanswaby.surveyJunky.repository.SurveyRepository;
import com.rohanswaby.surveyJunky.repository.UserRepository;
import com.rohanswaby.surveyJunky.repository.VoteRepository;
import com.rohanswaby.surveyJunky.security.UserPrincipal;
import com.rohanswaby.surveyJunky.utils.Constants;
import com.rohanswaby.surveyJunky.utils.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SurveyService {
    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private VoteRepository voteRepository;


    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(SurveyService.class);


    public PagedResponse<SurveyResponse> getAllSurveys(UserPrincipal currentUser, int page, int size) {

        validatePageNumberAndSize(page,size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Survey> surveys = surveyRepository.findAll(pageable);


        if(surveys.getNumberOfElements() == 0){
            return new PagedResponse<>(Collections.emptyList(),surveys.getNumber(),surveys.getSize(),surveys.getTotalElements(),
                    surveys.getTotalPages(),surveys.isLast());
        }

        List<Long> surveyIds = surveys.map(Survey::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(surveyIds);
        Map<Long, Long> surveyUserVoteMap = getSurveyUserVoteMap(currentUser, surveyIds);
        Map<Long, User> creatorMap = getSurveyCreatorMap(surveys.getContent());

        List<SurveyResponse> pollResponses = surveys.map(survey -> {
            return ModelMapper.mapSurveyToSurveyResponse(survey,
                    choiceVoteCountMap,
                    creatorMap.get(survey.getCreatedBy()),
                    surveyUserVoteMap == null ? null : surveyUserVoteMap.getOrDefault(survey.getId(), null));
        }).getContent();

        return new PagedResponse<>(pollResponses, surveys.getNumber(),
                surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
    }

    public PagedResponse<SurveyResponse> getPollsCreatedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all polls created by the given username
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Survey> surveys = surveyRepository.findByCreatedBy(user.getId(), pageable);

        if (surveys.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), surveys.getNumber(),
                    surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
        }

        // Map Polls to PollResponses containing vote counts and poll creator details
        List<Long> surveyIds = surveys.map(Survey::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(surveyIds);
        Map<Long, Long> surveyUserVoteMap = getSurveyUserVoteMap(currentUser, surveyIds);

        List<SurveyResponse> surveyResponses = surveys.map(survey -> {
            return ModelMapper.mapSurveyToSurveyResponse(survey,
                    choiceVoteCountMap,
                    user,
                    surveyUserVoteMap == null ? null : surveyUserVoteMap.getOrDefault(survey.getId(), null));
        }).getContent();

        return new PagedResponse<>(surveyResponses, surveys.getNumber(),
                surveys.getSize(), surveys.getTotalElements(), surveys.getTotalPages(), surveys.isLast());
    }

    public PagedResponse<SurveyResponse> getSurveysVotedBy(String username, UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Retrieve all pollIds in which the given username has voted
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedSurveyIds = voteRepository.findVotedSurveyIdsByUserId(user.getId(), pageable);

        if (userVotedSurveyIds.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), userVotedSurveyIds.getNumber(),
                    userVotedSurveyIds.getSize(), userVotedSurveyIds.getTotalElements(),
                    userVotedSurveyIds.getTotalPages(), userVotedSurveyIds.isLast());
        }

        // Retrieve all poll details from the voted pollIds.
        List<Long> surveyIds = userVotedSurveyIds.getContent();

        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");
        List<Survey> surveys = surveyRepository.findByIdIn(surveyIds, sort);

        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(surveyIds);
        Map<Long, Long> surveyUserVoteMap = getSurveyUserVoteMap(currentUser, surveyIds);
        Map<Long, User> creatorMap = getSurveyCreatorMap(surveys);

        List<SurveyResponse> surveyResponses = surveys.stream().map(survey -> {
            return ModelMapper.mapSurveyToSurveyResponse(survey,
                    choiceVoteCountMap,
                    creatorMap.get(survey.getCreatedBy()),
                    surveyUserVoteMap == null ? null : surveyUserVoteMap.getOrDefault(survey.getId(), null));
        }).collect(Collectors.toList());

        return new PagedResponse<>(surveyResponses, userVotedSurveyIds.getNumber(), userVotedSurveyIds.getSize(), userVotedSurveyIds.getTotalElements(), userVotedSurveyIds.getTotalPages(), userVotedSurveyIds.isLast());
    }


    public Survey createSurvey(SurveyRequest surveyRequest) {
        Survey survey = new Survey();
        survey.setQuestion(surveyRequest.getQuestion());

        surveyRequest.getChoices().forEach(choiceRequest -> {
            survey.addChoice(new Choice(choiceRequest.getText()));
        });

        Instant now = Instant.now();
        Instant expirationDateTime = now.plus(Duration.ofDays(surveyRequest.getSurveyLength().getDays()))
                .plus(Duration.ofHours(surveyRequest.getSurveyLength().getHours()));

        survey.setExpirationDateTime(expirationDateTime);

        return surveyRepository.save(survey);
    }

    public SurveyResponse getSurveyById(Long surveyId, UserPrincipal currentUser) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                () -> new ResourceNotFoundException("survey", "id", surveyId));

        // Retrieve Vote Counts of every choice belonging to the current poll
        List<ChoiceVoteCount> votes = voteRepository.countBySurveyIdGroupByChoiceId(surveyId);

        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        // Retrieve poll creator details
        User creator = userRepository.findById(survey.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", survey.getCreatedBy()));

        // Retrieve vote done by logged in user
        Vote userVote = null;
        if(currentUser != null) {
            userVote = voteRepository.findByUserIdAndSurveyId(currentUser.getId(), surveyId);
        }

        return ModelMapper.mapSurveyToSurveyResponse(survey, choiceVotesMap,
                creator, userVote != null ? userVote.getChoice().getId(): null);
    }

    public SurveyResponse castVoteAndGetUpdatedSurvey(Long surveyId, VoteRequest voteRequest, UserPrincipal currentUser) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("survey", "id", surveyId));

        if(survey.getExpirationDateTime().isBefore(Instant.now())) {
            throw new BadRequestException("Sorry! This survey has already expired");
        }

        User user = userRepository.getOne(currentUser.getId());

        Choice selectedChoice = survey.getChoices().stream()
                .filter(choice -> choice.getId().equals(voteRequest.getChoiceId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));

        Vote vote = new Vote();
        vote.setSurvey(survey);
        vote.setUser(user);
        vote.setChoice(selectedChoice);

        try {
            vote = voteRepository.save(vote);
        } catch (DataIntegrityViolationException ex) {
            logger.info("User {} has already voted in Poll {}", currentUser.getId(), surveyId);
            throw new BadRequestException("Sorry! You have already cast your vote in this poll");
        }

        //-- Vote Saved, Return the updated Poll Response now --

        // Retrieve Vote Counts of every choice belonging to the current poll
        List<ChoiceVoteCount> votes = voteRepository.countBySurveyIdGroupByChoiceId(surveyId);

        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        // Retrieve poll creator details
        User creator = userRepository.findById(survey.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", survey.getCreatedBy()));

        return ModelMapper.mapSurveyToSurveyResponse(survey, choiceVotesMap, creator, vote.getChoice().getId());
    }


    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if(size > Constants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + Constants.MAX_PAGE_SIZE);
        }
    }

    private Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds) {
        // Retrieve Vote Counts of every Choice belonging to the given pollIds
        List<ChoiceVoteCount> votes = voteRepository.countBySurveyIdInGroupByChoiceId(pollIds);

        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        return choiceVotesMap;
    }

    private Map<Long, Long> getSurveyUserVoteMap(UserPrincipal currentUser, List<Long> surveyIds) {
        // Retrieve Votes done by the logged in user to the given pollIds
        Map<Long, Long> surveyUserVoteMap = null;
        if(currentUser != null) {
            List<Vote> userVotes = voteRepository.findByUserIdAndSurveyIdIn(currentUser.getId(), surveyIds);

            surveyUserVoteMap = userVotes.stream()
                    .collect(Collectors.toMap(vote -> vote.getSurvey().getId(), vote -> vote.getChoice().getId()));
        }
        return surveyUserVoteMap;
    }

    Map<Long, User> getSurveyCreatorMap(List<Survey> surveys) {
        // Get Survey Creator details of the given list of surveys
        List<Long> creatorIds = surveys.stream()
                .map(Survey::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());

        List<User> creators = userRepository.findByIdIn(creatorIds);
        Map<Long, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return creatorMap;
    }


}
