package com.patrikmaryska.isprojekt.socsetreni.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserDocumentsId implements Serializable {

    @Column(name="user_id", nullable = false)
    private long user;

    @Column(name="case_id", nullable = false)
    private long aCase;

    @Column(name="sharing_type_id", nullable = false)
    private long sharingTypeId;

    public UserDocumentsId(){}

    public UserDocumentsId(long user, long caseId, long sharingTypeId) {
        this.user = user;
        this.aCase = caseId;
        this.sharingTypeId = sharingTypeId;
    }

    public UserDocumentsId(long user, long caseId) {
        this.user = user;
        this.aCase = caseId;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getSharingTypeId() {
        return sharingTypeId;
    }

    public void setSharingTypeId(long sharingTypeId) {
        this.sharingTypeId = sharingTypeId;
    }

    public long getaCase() {
        return aCase;
    }

    public void setaCase(long aCase) {
        this.aCase = aCase;
    }
}
