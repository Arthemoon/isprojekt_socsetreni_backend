package com.patrikmaryska.isprojekt.socsetreni.model.RequestBody;

import com.patrikmaryska.isprojekt.socsetreni.model.Case;

import java.util.List;

public class DocumentBearer {

    private List<Case> caseList;
    private long countOfRows;

    public DocumentBearer(){

    }

    public List<Case> getCaseList() {
        return caseList;
    }

    public void setCaseList(List<Case> caseList) {
        this.caseList = caseList;
    }

    public long getCountOfRows() {
        return countOfRows;
    }

    public void setCountOfRows(long countOfRows) {
        this.countOfRows = countOfRows;
    }
}
