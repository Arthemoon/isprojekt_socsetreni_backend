package com.patrikmaryska.isprojekt.socsetreni.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
public class CaseGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Name of group is mandatory")
    @Column(length = 40, nullable = false)
    @Size(min = 3, max = 40,  message = "Size can be 3-40 chars.")
    @Pattern(regexp = "^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s\\,\\(\\)\\\"]+$", message = "Name of the group can contain only ?!,()")
    private String name;

    public CaseGroup(){}

    @JsonIgnore
    @ManyToMany()
    @JoinTable(
            name = "users_groups",
            joinColumns = @JoinColumn(
                    name = "group_id", referencedColumnName = "id", nullable = false),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id", nullable = false))
    private List<User> users;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
