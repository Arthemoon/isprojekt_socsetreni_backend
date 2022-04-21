package com.patrikmaryska.isprojekt.socsetreni.repository;

import com.patrikmaryska.isprojekt.socsetreni.model.CaseGroup;

import java.util.List;

public interface GroupRepositoryCustom {

    List<com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.Group> getUsersUnits(String email, int page);

    void updateUnit(CaseGroup CaseGroup);

    void removeUserFromGroups(long id);
}
