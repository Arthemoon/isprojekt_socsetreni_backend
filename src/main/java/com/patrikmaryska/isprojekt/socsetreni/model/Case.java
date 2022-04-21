package com.patrikmaryska.isprojekt.socsetreni.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Table(name = "scase")
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true, length = 60, nullable = false)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 1, max = 50, message = "Size can be 1-50 chars.")
    @Column(length = 50, nullable = false)
    @Pattern(regexp = "^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s]+$",  message = "Name cannot have alphanumeric characters and ? ! and whitespace chars")
    private String cname;

    @NotBlank(message = "Surname is mandatory")
    @Size(min = 1, max = 50, message = "Size can be 1-50 chars.")
    @Column(length = 50, nullable = false)
    @Pattern(regexp = "^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s]+$",  message = "Title can have alphanumeric characters and ? ! and whitespace chars")
    private String csurname;

    @NotBlank(message = "RC is mandatory")
    @Column(nullable = false)
    private String pid;

    @NotBlank(message = "Description is mandatory")
    @Size(min = 3, max = 255,  message = "Size can be 3-255 chars.")
    @Column(length = 255, nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "case_state_id", nullable = false)
    private CaseState caseState;

    @JsonIgnore
    @Column(name = "resource_path", length = 255, nullable = false)
    private String resourcePath;

    @Column(name = "upload_datetime", columnDefinition = "DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "UploadDateTime is mandatory")
    private Date uploadDatetime;

    @Column(name = "approval_end_time", columnDefinition = "DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Approval end time is mandatory")
    private Date approvalEndTime;

    @Column(name = "active_start_time", columnDefinition = "DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Active start time is mandatory")
    private Date activeStartTime;

    @Column(name = "active_end_time", columnDefinition = "DATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Active end time is mandatory")
    private Date activeEndTime;

    @OneToMany(mappedBy = "aCase", cascade= CascadeType.ALL, orphanRemoval = true)
    private Set<UsersCases> documentsForUsers = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Transient
    @JsonInclude
    private int approval;

    @Transient
    @JsonInclude
    private Date applicationStartTime;


    public Case() {
    }


    public Case(String cname, String surname, String pid, String description){
        this.cname = cname;
        this.pid = pid;
        this.description = description;
        this.csurname = surname;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getCsurname() {
        return csurname;
    }

    public void setCsurname(String csurname) {
        this.csurname = csurname;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Case)) return false;
        Case aCase = (Case) o;
        return getId() == aCase.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Date getUploadDatetime() {
        return uploadDatetime;
    }

    public void setUploadDatetime(Date uploadDatetime) {
        this.uploadDatetime = uploadDatetime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<UsersCases> getDocumentsForUsers() {
        return documentsForUsers;
    }

    public void setDocumentsForUsers(Set<UsersCases> documentsForUsers) {
        this.documentsForUsers = documentsForUsers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getApprovalEndTime() {
        return approvalEndTime;
    }

    public void setApprovalEndTime(Date approvalEndTime) {
        this.approvalEndTime = approvalEndTime;
    }

    public Date getActiveStartTime() {
        return activeStartTime;
    }

    public void setActiveStartTime(Date activeStartTime) {
        this.activeStartTime = activeStartTime;
    }

    public Date getActiveEndTime() {
        return activeEndTime;
    }

    public void setActiveEndTime(Date activeEndTime) {
        this.activeEndTime = activeEndTime;
    }

    public CaseState getCaseState() {
        return caseState;
    }

    public void setCaseState(CaseState caseState) {
        this.caseState = caseState;
    }

    @Override
    public String toString() {
        return "Case{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", caseState=" + caseState +
                ", resourcePath='" + resourcePath + '\'' +
                ", uploadDatetime=" + uploadDatetime +
                ", approvalEndTime=" + approvalEndTime +
                ", activeStartTime=" + activeStartTime +
                ", activeEndTime=" + activeEndTime +
                ", documentsForUsers=" + documentsForUsers +
                ", user=" + user +
                '}';
    }

    public int getApproval() {
        return approval;
    }

    public void setApproval(int approval) {
        this.approval = approval;
    }

    public Date getApplicationStartTime() {
        return applicationStartTime;
    }

    public void setApplicationStartTime(Date applicationStartTime) {
        this.applicationStartTime = applicationStartTime;
    }
}

