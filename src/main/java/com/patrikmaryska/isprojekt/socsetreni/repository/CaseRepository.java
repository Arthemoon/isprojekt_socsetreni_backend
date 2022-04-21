package com.patrikmaryska.isprojekt.socsetreni.repository;

import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<Case, Long>, CaseRepositoryCustom {
}
