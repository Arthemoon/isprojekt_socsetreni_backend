package com.patrikmaryska.isprojekt.socsetreni.model.RequestBody;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.patrikmaryska.isprojekt.socsetreni.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

public class Group {

    @NotNull
    private long id;

    @NotBlank(message = "Name of group cannot be empty")
    @Size(min = 3, max = 40, message = "Size can be 3-40 chars")
    @Pattern(regexp = "^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s\\,\\(\\)\\\"]+$",  message = "Name of the group can contain only ?!,()")
    private String name;

    private List<User> users;

    @NotNull(message = "Ids must be set")
    @JsonIgnore
    private List<Long> ids;

    public Group(){}

    public Group(long id, String name, List<User> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

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

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
