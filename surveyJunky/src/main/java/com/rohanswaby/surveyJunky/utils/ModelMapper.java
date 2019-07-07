package com.rohanswaby.surveyJunky.utils;

import com.rohanswaby.surveyJunky.Payload.ChoiceResponse;
import com.rohanswaby.surveyJunky.Payload.SurveyResponse;
import com.rohanswaby.surveyJunky.Payload.UserSummary;
import com.rohanswaby.surveyJunky.models.Survey;
import com.rohanswaby.surveyJunky.models.User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {

    public static SurveyResponse mapSurveyToSurveyResponse(Survey survey, Map<Long,Long> choiceMap, User organizer, Long userVote){
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setId(survey.getId());
        surveyResponse.setQuestion(survey.getQuestion());
        surveyResponse.setCreationDateTime(survey.getCreatedAt());
        surveyResponse.setExpirationDateTime(survey.getExpirationDateTime());

        Instant now = Instant.now();

        surveyResponse.setExpired(survey.getExpirationDateTime().isBefore(now));

        List<ChoiceResponse> choiceResponses = survey.getChoices().stream().map(choice -> {
            ChoiceResponse choiceResponse = new ChoiceResponse();
            choiceResponse.setId(choice.getId());
            choiceResponse.setText(choice.getText());

            if(choiceMap.containsKey(choice.getId())) {
                choiceResponse.setVoteCount(choiceMap.get(choice.getId()));
            } else {
                choiceResponse.setVoteCount(0);
            }
            return choiceResponse;
        }).collect(Collectors.toList());


        surveyResponse.setChoices(choiceResponses);

        UserSummary userSummary = new UserSummary(organizer.getId(),organizer.getUsername(),organizer.getName());

        if(userVote != null) {
            surveyResponse.setSelectedChoice(userVote);
        }

        long totalVotes = surveyResponse.getChoices().stream().mapToLong(ChoiceResponse::getVoteCount).sum();
        surveyResponse.setTotalVotes(totalVotes);

        return surveyResponse;

    }
}
