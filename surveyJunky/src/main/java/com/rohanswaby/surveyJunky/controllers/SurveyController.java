package com.rohanswaby.surveyJunky.controllers;

import com.rohanswaby.surveyJunky.Payload.*;
import com.rohanswaby.surveyJunky.models.Survey;
import com.rohanswaby.surveyJunky.repository.SurveyRepository;
import com.rohanswaby.surveyJunky.repository.UserRepository;
import com.rohanswaby.surveyJunky.repository.VoteRepository;
import com.rohanswaby.surveyJunky.security.CurrentUser;
import com.rohanswaby.surveyJunky.security.UserPrincipal;
import com.rohanswaby.surveyJunky.services.SurveyService;
import com.rohanswaby.surveyJunky.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("api/survey")
public class SurveyController {

    @Autowired
    SurveyRepository surveyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SurveyService surveyService;

    @Autowired
    VoteRepository voteRepository;


    public static Logger logger = LoggerFactory.getLogger(SurveyController.class);

    @GetMapping
    public PagedResponse<SurveyResponse> getSurveys(@CurrentUser UserPrincipal currentUser,
                                                    @RequestParam(value = "page", defaultValue = Constants.INIT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = Constants.INIT_PAGE_SIZE) int size){

        return surveyService.getAllSurveys(currentUser,page,size);

    }


    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSurvey(@Valid @RequestBody SurveyRequest surveyRequest) {
        Survey survey = surveyService.createSurvey(surveyRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{surveyId}")
                .buildAndExpand(survey.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "survey Created Successfully"));
    }

    @GetMapping("/{surveyId}")
    public SurveyResponse getSurveyById(@CurrentUser UserPrincipal currentUser,
                                    @PathVariable Long surveyId) {
        SurveyResponse surveyById = surveyService.getSurveyById(surveyId, currentUser);
        return surveyById;
    }

    @PostMapping("/{surveyId}/votes")
    @PreAuthorize("hasRole('USER')")
    public SurveyResponse castVote(@CurrentUser UserPrincipal currentUser,
                                 @PathVariable Long surveyId,
                                 @Valid @RequestBody VoteRequest voteRequest) {
        return surveyService.castVoteAndGetUpdatedSurvey(surveyId, voteRequest, currentUser);
    }



}
