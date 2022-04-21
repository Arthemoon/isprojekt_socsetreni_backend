package com.patrikmaryska.isprojekt.socsetreni.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="users_cases")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UsersCases {

    @EmbeddedId
    @JsonIgnore
    private UserDocumentsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("user")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("aCase")
    @JoinColumn(name = "case_id", nullable = false)
    @JsonIgnore
    private Case aCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sharingType")
    @JoinColumn(name = "sharing_type_id", nullable = false)
    private SharingType sharingType;

    @Column(name = "application_date", columnDefinition = "DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Active time is mandatory")
    private Date applicationStartTime;

    @Column(name="approval", nullable = false)
    @NotNull
    private int approval = 2;

    @Column(name = "email_sent", nullable = false)
    @JsonIgnore
    private boolean emailSent;

    public UsersCases(){
    }

    public UsersCases(User user, Case aCase) {
        this.user = user;
        this.aCase = aCase;
        this.id = new UserDocumentsId(user.getId(), aCase.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsersCases)) return false;
        UsersCases that = (UsersCases) o;
        return getUser().equals(that.getUser()) &&
                getaCase().equals(that.getaCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getaCase());
    }

    public UserDocumentsId getId() {
        return id;
    }

    public void setId(UserDocumentsId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Case getaCase() {
        return aCase;
    }

    public void setaCase(Case aCase) {
        this.aCase = aCase;
    }

    public int getApproval() {
        return approval;
    }

    public void setApproval(int approval) {
        this.approval = approval;
    }

    public SharingType getSharingType() {
        return sharingType;
    }

    public void setSharingType(SharingType sharingType) {
        this.sharingType = sharingType;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    @Override
    public String toString() {
        return "UsersCases{" +
                "id=" + id +
                ", user=" + user +
                ", sharingType=" + sharingType +
                ", approval=" + approval +
                '}';
    }

    public Date getApplicationStartTime() {
        return applicationStartTime;
    }

    public void setApplicationStartTime(Date applicationStartTime) {
        this.applicationStartTime = applicationStartTime;
    }
}
