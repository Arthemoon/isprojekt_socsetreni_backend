package com.patrikmaryska.isprojekt.socsetreni.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "case_state")
public class CaseState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "State is mandatory")
    @Size(min=1, max=60, message = "Size can be 1-60 chars.")
    @Column(length = 60, nullable = false)
    private String state;

    @JsonIgnore
    @OneToMany(mappedBy = "caseState", cascade= CascadeType.ALL)
    private List<Case> aCases;

    public CaseState(){

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Case> getaCases() {
        return aCases;
    }

    public void setaCases(List<Case> aCases) {
        this.aCases = aCases;
    }
}
