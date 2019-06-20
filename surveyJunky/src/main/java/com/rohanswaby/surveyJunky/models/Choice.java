package com.rohanswaby.surveyJunky.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name="choices")
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Size(max = 40)
    private String text;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;


    public Choice(){

    }

    public Choice (String text){
        this.text=text;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Survey getSurvey() {
        return this.survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    @Override
    public boolean equals(Object object){
        if(this == object) return true;
        if(object == null || this.getClass() != object.getClass()) return false;

        Choice choice = (Choice) object;
        return Objects.equals(this.id,choice.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }


}
