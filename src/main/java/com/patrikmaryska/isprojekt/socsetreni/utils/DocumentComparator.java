package com.patrikmaryska.isprojekt.socsetreni.utils;

import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import com.patrikmaryska.isprojekt.socsetreni.model.UsersCases;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

public class DocumentComparator implements Comparator<Case> {
    private User user;

    public DocumentComparator(User user){
        this.user = user;
    }

    @Override
    public int compare(Case d1, Case d2) {
        Date date1 = new Date();
        Date date2 = new Date();

        Optional<UsersCases> ud1 = d1.getDocumentsForUsers().stream().
                filter(usersDocuments -> usersDocuments.getUser().getId() == user.getId()).findFirst();
        Optional<UsersCases> ud2 = d2.getDocumentsForUsers().stream().
                filter(usersDocuments -> usersDocuments.getUser().getId() == user.getId()).findFirst();

        if(ud1.get().getSharingType().getId() == 1){
            date1 = d1.getUploadDatetime();
        }  else {
            date1 = d1.getActiveStartTime();
        }

        if(ud2.get().getSharingType().getId() == 1){
            date2 = d2.getUploadDatetime();
        }  else{
            date2 = d2.getActiveStartTime();
        }

        return date1.compareTo(date2);
    }
}
