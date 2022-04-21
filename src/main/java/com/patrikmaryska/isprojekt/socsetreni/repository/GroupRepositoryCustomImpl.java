package com.patrikmaryska.isprojekt.socsetreni.repository;

import com.patrikmaryska.isprojekt.socsetreni.model.CaseGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GroupRepositoryCustomImpl implements GroupRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    private final int PAGE_SIZE = 10;

    @Override
    public List<com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.Group> getUsersUnits(String email, int page) {
        TypedQuery<CaseGroup> unitTypedQuery = entityManager.createQuery("SELECT u FROM CaseGroup u ORDER BY u.name", CaseGroup.class);
        if(page != 9999){
            unitTypedQuery.setFirstResult((page-1) * PAGE_SIZE);
            unitTypedQuery.setMaxResults(PAGE_SIZE);
        }

        List<CaseGroup> caseGroups = unitTypedQuery.getResultList();

        return caseGroups.stream()
                .map(unit -> new com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.Group(unit.getId(), unit.getName(), unit.getUsers()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void updateUnit(CaseGroup CaseGroup) {

        entityManager.merge(CaseGroup);
    }

    @Override
    public void removeUserFromGroups(long id) {
        Query query = entityManager.createNativeQuery("delete from users_groups WHERE user_id=:id");
        Query docsQuery = entityManager.createNativeQuery("delete from users_documents WHERE user_id=:id");

        query.setParameter("id", id);
        docsQuery.setParameter("id", id);

        query.executeUpdate();
        docsQuery.executeUpdate();
    }
}
