package com.rohanswaby.surveyJunky.Payload;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class SurveyRequest {

    @NotBlank
    @Size(max = 140)
    private String question;


    @NotBlank
    @Size(min = 2, max = 6)
    @Valid
    private List<ChoiceRequest> choices;


    @NotBlank
    @Valid
    private SurveyLength surveyLength;

    public String getQuestion(){
        return question;
    }


    public void setQuestion(String question){
        this.question = question;
    }

    public List<ChoiceRequest> getChoices(){
        return choices;
    }

    public void setChoices(List<ChoiceRequest> choices){
        this.choices = choices;
    }

    public SurveyLength getSurveyLength() {
        return surveyLength;
    }

    public void setSurveyLength(SurveyLength surveyLength) {
        this.surveyLength = surveyLength ;
    }


}
