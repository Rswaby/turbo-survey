package com.rohanswaby.surveyJunky.models;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "surveys")
public class Survey extends UserDateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Size(max = 140)
    private String question;

    @OneToMany(
            mappedBy = "survey",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Size(min = 2, max = 6)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30)
    private List<Choice> choiceList = new ArrayList<>();



    @NotNull
    private Instant expirationDateTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Choice> getChoices() {
        return choiceList;
    }

    public void setChoices(List<Choice> choices) {
        this.choiceList = choiceList;
    }

    public Instant getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(Instant expirationDateTime){
        this.expirationDateTime = expirationDateTime;
    }

    public void addChoice(Choice choice){
        choice.add(choice);
        choice.setSurvey(this);
    }


    public void removeChoice(Choice choice){
        choice.remove(choice);
        choice.setSurvey(null);
    }


}
