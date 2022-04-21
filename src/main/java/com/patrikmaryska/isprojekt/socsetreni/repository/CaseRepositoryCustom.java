package com.patrikmaryska.isprojekt.socsetreni.repository;

import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.SimplifiedUser;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UsersApproval;
import com.patrikmaryska.isprojekt.socsetreni.model.*;

import java.util.Date;
import java.util.List;

public interface CaseRepositoryCustom {
   List<Case> getAllUsersDocuments(long id);
   List<Case> getAllDocsByEmail(String email, int page);
   void saveDocument(Case aCase);
   Case getDocumentByName(String name);

    List<SimplifiedUser> getReaders(int page, long docId, int sharingType, int approval);

   void insertSharing(String name, List<User> users, long documentTypeId, boolean emailSent, Date appTime);
   void updateSharing(long userId, long documentId, int approval);

    boolean hasUserAccessToDocument(long documentId, long userId);

    List<Case> findDocumentByTitle(String name, long userId, int page);

    List<Case> getUsersCreatedDocuments(long id, int year, int month, int pageNumber);

    List<UsersApproval> getUsersForDocument(long docId, long userId);
    SharingType getDocumentType(long id);

    boolean isDocumentApproved(long docId);
    long getCountOfApprovals(long docId);
    long getExpectedCountOfApprovals(long docId);

    CaseState getDocumentState(long id);

    void updateDocument(Case updatedCase);

    List<Case> getAllPassedDocuments();

    void blockDocument(Case aCase);

    List<Case> getDocumentsByYearAndMonth(int year, int month, String email, int page);

    List<UsersCases> getNewActiveDocuments();

    void updateEmailSent(UsersCases usersCases);

    boolean deleteDocument(long id);

    boolean deleteAllUsersDocumentsByDocumentId(long id);

    List<Case> getAllDocumentsByYearAndMonth(int year, int month, int page);

    boolean hasUserPermissionToAccessDocument(long documentId, long userId);

    UsersCases getUsersDocuments(long userId, long docId, long sharingTypeId);
}
