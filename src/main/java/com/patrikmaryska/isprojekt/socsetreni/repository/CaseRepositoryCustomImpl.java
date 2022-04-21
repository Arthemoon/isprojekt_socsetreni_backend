package com.patrikmaryska.isprojekt.socsetreni.repository;

import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.SimplifiedUser;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UsersApproval;
import com.patrikmaryska.isprojekt.socsetreni.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CaseRepositoryCustomImpl implements CaseRepositoryCustom {

    private final int PAGE_SIZE = 10;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Case> getAllUsersDocuments(long id) {
        TypedQuery<Case> query = entityManager.createQuery("SELECT d FROM Case d join d.documentsForUsers ud WHERE ud.user = :id ORDER BY d.uploadDateTime DESC", Case.class);
        query.setParameter("id", id);

        return query.getResultList();
    }

    @Override
    public List<Case> getAllDocsByEmail(String email, int page) {
        User user = userRepository.getUserByEmail(email).get();

         TypedQuery<Case> q2 = entityManager.createQuery("SELECT d FROM Case d INNER JOIN d.documentsForUsers ud WHERE d.caseState > 0 " +
                 "AND ud.user.id=:id AND ud.approval=2 AND current_timestamp >= ud.applicationStartTime and (d.caseState=1 or d.caseState=2) ORDER BY ud.applicationStartTime DESC", Case.class);


        q2.setParameter("id", user.getId());
        q2.setFirstResult((page - 1 ) * PAGE_SIZE);
        q2.setMaxResults(PAGE_SIZE);

        return updateDocumentList(q2.getResultList(), user.getId());
    }


    @Override
    public void saveDocument(Case aCase) throws PersistenceException {
        entityManager.persist(aCase);
    }

    @Override
    public Case getDocumentByName(String name){
        TypedQuery<Case> query = entityManager.createQuery("SELECT d FROM Case d WHERE d.name=:name", Case.class);
        query.setParameter("name", name);

        return query.getSingleResult();
    }

    @Override
    public void insertSharing(String name, List<User> users, long documentTypeId, boolean emailSent, Date appTime) {
        Case aCase = getDocumentByName(name);

        Query query;
        if(aCase != null){
            for(int i = 0; i < users.size(); i++){
                query = entityManager.createNativeQuery("INSERT INTO users_cases (case_id, user_id, approval, sharing_type_id, email_sent, application_date) VALUES (:value1, :value2, 2, :value3, :value4, :value5)");
                query.setParameter("value1", aCase.getId());
                query.setParameter("value2", users.get(i).getId());
                query.setParameter("value3", documentTypeId);
                query.setParameter("value5", appTime);
                query.setParameter("value4", emailSent);

                query.executeUpdate();
            }
        }
    }

    @Override
    public void updateSharing(long userId, long documentId, int approval) {
        Case aCase = getDocumentById(documentId);
        UserDocumentsId userDocumentsId = new UserDocumentsId(userId, documentId);

        if(aCase.getCaseState().getId() == 2 || aCase.getCaseState().getId() == 3){
            userDocumentsId.setSharingTypeId(1); // approval
        } else if(aCase.getCaseState().getId() == 1){
            // approved, readers
            userDocumentsId.setSharingTypeId(2);
        } else {
            // cancelled do nothing
            return;
        }

        UsersCases usersCases = entityManager.find(UsersCases.class, userDocumentsId);

        usersCases.setApproval(approval);
        entityManager.merge(usersCases);
    }

    public Case getDocumentById(long docId){
       return (Case) entityManager.find(Case.class, docId);
    }

    @Override
    public boolean hasUserAccessToDocument(long documentId, long userId) {
        Case aCase = getDocumentById(documentId);
        String sql = "";

        if(aCase.getUser().getId() == userId){
            return true;
        }

        User user = userRepository.getOne(userId);

        if(user.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ADMIN"))){
            return true;
        }

        if(aCase.getCaseState().getId() == 2 || aCase.getCaseState().getId() == 3){
            // IS PROCESSING, OR CANCELLED
            sql = "SELECT ud FROM UsersCases ud WHERE ud.sharingType.id=1 AND ud.aCase.id=:docId AND ud.user.id=:userId";
        } else if(aCase.getCaseState().getId() == 1) {
            sql = "SELECT ud FROM UsersCases ud WHERE (ud.sharingType.id=1 OR ud.sharingType.id=2) AND ud.aCase.id=:docId AND ud.user.id=:userId";
        } else {
            // nic
            return false;
        }

        TypedQuery<UsersCases> usersDocuments = entityManager.createQuery(sql, UsersCases.class);
        usersDocuments.setParameter("docId", documentId);
        usersDocuments.setParameter("userId", userId);

        if(usersDocuments.getResultList().size() > 0){
            return true;
        }

        return false;
    }

    @Override
    public List<Case> findDocumentByTitle(String surname, long userId, int page) {

        TypedQuery<Case> q2 = entityManager.createQuery("SELECT d FROM Case d " +
                "INNER JOIN d.documentsForUsers ud WHERE (d.caseState.id != 0 AND ud.sharingType = 1 AND" +
                        " ud.user.id=:id AND d.csurname LIKE :value)" +
                        " OR(d.caseState.id = 1 AND ud.sharingType.id=2 AND ud.user.id=:id" +
                        " AND current_timestamp > d.activeStartTime AND " +
                        " d.csurname LIKE :value) ORDER BY d.csurname"
                , Case.class);
        q2.setParameter("id", userId);
        q2.setParameter("value",surname+"%"); //
        q2.setMaxResults(PAGE_SIZE);
        q2.setFirstResult((page - 1 ) * PAGE_SIZE);

        return updateDocumentList(q2.getResultList(), userId);
    }

    private List<Case> updateDocumentList(List<Case> list, long userId){
        List<Case> docs = list.stream().map(document -> {
            UsersCases ud = document.getDocumentsForUsers().stream().filter(usersDocuments -> usersDocuments.getUser().getId() == userId
            && usersDocuments.getaCase().getId() == document.getId()).findFirst().get();
            document.setApproval(ud.getApproval());
     //       document.setApplicationStartTime(ud.getApplicationStartTime());
            return document;
        }).collect(Collectors.toList());

        return docs;
    }


    public Date minmaxDate(int year, int month, final String type){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        if(type.equals("MIN")){
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
        }

        int intDate = type.equals("MAX") ? calendar.getActualMaximum(Calendar.DATE) : calendar.getActualMinimum(Calendar.DATE);
        calendar.set(Calendar.DATE, intDate);
        return calendar.getTime();
    }

    @Override
    public List<Case> getUsersCreatedDocuments(long id, int year, int month, int pageNumber) {

        Date minDate = minmaxDate(year, month, "MIN");
        Date maxDate = minmaxDate(year, month, "MAX");

        TypedQuery<Case> query = entityManager.createQuery("SELECT d FROM Case d JOIN d.user u " +
                " WHERE d.uploadDatetime BETWEEN :minDate AND :maxDate and d.user.id=:value " +
                "ORDER BY d.uploadDatetime DESC", Case.class);

        query.setParameter("value", id);
        query.setParameter("minDate", minDate);
        query.setParameter("maxDate", maxDate);
        query.setFirstResult((pageNumber - 1 ) * PAGE_SIZE);
        query.setMaxResults(PAGE_SIZE);


     //   return updateDocumentList(query.getResultList());
        return query.getResultList();
    }

    @Override
    public List<UsersApproval> getUsersForDocument(long docId, long userId) {

        Query query = entityManager.createNativeQuery("SELECT u.first_name, u.surname, ud.approval FROM user u JOIN users_cases ud ON u.id = ud.user_id " +
                "WHERE ud.case_id = :x");

        query.setParameter("x", docId);
        List<UsersApproval> usersApprovals = new ArrayList<>();

        List<Object[]> results = query.getResultList();

        results.forEach(object -> {
            String firstName = (String) object[0];
            String surname = (String) object[1];
            int approval = (Integer) object[2];


            usersApprovals.add(new UsersApproval(firstName, surname, approval));
        });

        return usersApprovals;
    }

    @Override
    public SharingType getDocumentType(long id){
        TypedQuery<SharingType> documentTypeTypedQuery = entityManager.createQuery("SELECT d FROM CaseType d WHERE d.id=:id", SharingType.class);
        documentTypeTypedQuery.setParameter("id", id);

        return documentTypeTypedQuery.getSingleResult();
    }

    @Override
    public boolean isDocumentApproved(long documentId){
        Query query = entityManager.createQuery("");

        return true;
    }

    @Override
    public long getCountOfApprovals(long docId) {
        Query query = entityManager.createQuery("SELECT count(d.id) from Case d INNER JOIN d.documentsForUsers ud " +
                "INNER JOIN ud.sharingType dt WHERE dt.id = :val1 AND ud.aCase.id = :val2 AND ud.approval = :val3");

        query.setParameter("val1", 1L);
        query.setParameter("val2", docId);
        query.setParameter("val3", 1);

        return (long) query.getSingleResult();
    }

    @Override
    public long getExpectedCountOfApprovals(long documentId){
        Query query = entityManager.createQuery("SELECT count(d.id) from Case d INNER JOIN d.documentsForUsers ud " +
                "INNER JOIN ud.sharingType dt WHERE dt.id = :val1 AND ud.aCase.id = :val2");
        query.setParameter("val1", 1L);
        query.setParameter("val2", documentId);

        return (long) query.getSingleResult();
    }

    @Override
    public CaseState getDocumentState(long id){
        TypedQuery<CaseState> query = entityManager.createQuery("SELECT st FROM CaseState st WHERE st.id=:id", CaseState.class);
        query.setParameter("id", id);

        return query.getSingleResult();
    }

    @Override
    public void updateDocument(Case aCase){
        entityManager.merge(aCase);
    }

    @Override
    public List<Case> getAllPassedDocuments() {
        TypedQuery<Case> documents = entityManager.createQuery("SELECT d FROM Case d " +
                "WHERE d.approvalEndTime < CURRENT_TIMESTAMP and d.caseState.id=2 ", Case.class);

        return documents.getResultList();
    }


    @Override
    public void blockDocument(Case aCase) {
        CaseState caseState = getDocumentState(3);
        aCase.setCaseState(caseState);

        entityManager.merge(aCase);
    }

    @Override
    public List<Case> getDocumentsByYearAndMonth(int year, int month, String email, int page) {

        Date minDate = minmaxDate(year, month, "MIN");
        Date maxDate = minmaxDate(year, month, "MAX");
        User user = userRepository.getUserByEmail(email).get();

        TypedQuery<Case> q3 = entityManager.createQuery("SELECT d FROM Case d " +
                "INNER JOIN d.documentsForUsers ud WHERE (" +
                " ud.sharingType = 1" +
                " AND ud.user.id=:id AND ud.approval != 2 AND" +
                " d.uploadDatetime between :minDate and :maxDate)" +
                " OR(d.caseState.id = 1 AND ud.sharingType.id=2" +
                " AND ud.user.id=:id" +
                " AND current_timestamp > d.activeStartTime AND ud.approval != 2" +
                 " AND d.activeStartTime between :minDate AND :maxDate) ORDER BY ud.applicationStartTime DESC", Case.class);

        q3.setParameter("id", user.getId());
        q3.setParameter("minDate", minDate);
        q3.setParameter("maxDate", maxDate);
        q3.setFirstResult((page - 1) * PAGE_SIZE);
        q3.setMaxResults(PAGE_SIZE);

        Optional<Case> doc = q3.getResultList().stream().filter(document -> document.getId() == 280319L).findFirst();

        System.out.println("DOC " + doc.isPresent());

        return updateDocumentList(q3.getResultList(), user.getId());

    //    return q3.getResultList();
    }

    @Override
    public List<UsersCases> getNewActiveDocuments() {
        TypedQuery<UsersCases> documentTypedQuery = entityManager.createQuery("SELECT ud FROM Case d" +
                " JOIN d.documentsForUsers ud WHERE d.activeStartTime < current_timestamp " +
                "AND ud.emailSent=0 AND d.caseState.id=1", UsersCases.class);

        return documentTypedQuery.getResultList();
    }

    @Override
    public void updateEmailSent(UsersCases usersCases){
        entityManager.merge(usersCases);
    }


    @Override
    public boolean deleteDocument(long id){
        Query query = entityManager.createNativeQuery("DELETE FROM scase WHERE id=:id");
        query.setParameter("id", id);

        return query.executeUpdate() > 0;
    }

    @Override
    public boolean deleteAllUsersDocumentsByDocumentId(long id){
        Query query = entityManager.createNativeQuery("DELETE FROM users_cases WHERE case_id=:id");
        query.setParameter("id", id);

        return query.executeUpdate() > 0;
    }

    @Override
    public List<Case> getAllDocumentsByYearAndMonth(int year, int month, int page){

        Date minDate = minmaxDate(year, month, "MIN");
        Date maxDate = minmaxDate(year, month, "MAX");

        TypedQuery<Case> documentTypedQuery = entityManager.createQuery("SELECT d FROM Case d " +
                "WHERE d.uploadDatetime BETWEEN :minDate AND :maxDate " +
                "ORDER BY d.uploadDatetime DESC", Case.class);

        documentTypedQuery.setParameter("minDate", minDate);
        documentTypedQuery.setParameter("maxDate", maxDate);
        documentTypedQuery.setFirstResult((page - 1 ) * PAGE_SIZE);
        documentTypedQuery.setMaxResults(PAGE_SIZE);

        return documentTypedQuery.getResultList();
    }

    @Override
    public boolean hasUserPermissionToAccessDocument(long documentId, long userId) {
        User user = userRepository.getOne(userId);
        Case aCase = getDocumentById(documentId);

        if(user.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ADMIN"))){
            return true;
        }

        if(aCase.getUser().getId() == userId){
            return true;
        }

        return false;
    }

    public List<SimplifiedUser> getReaders (int page, long docId, int sharingType, int approval){
        Query query = entityManager.createNativeQuery("select u.first_name, u.surname FROM User u INNER JOIN users_documents ud " +
                "ON ud.user_id=u.id WHERE ud.document_id=? AND ud.sharing_type_id=? AND ud.approval=? ORDER BY u.surname");

        query.setParameter(1, docId);
        query.setParameter(2, sharingType);
        query.setParameter(3, approval);
        query.setMaxResults(PAGE_SIZE);
        query.setFirstResult((page-1) * PAGE_SIZE);

        List<Object[]> users = query.getResultList();
        List<SimplifiedUser> usersFinal = new ArrayList<>();

        for(Object[] o : users){
            SimplifiedUser u = new SimplifiedUser();
            u.setFirstName(o[0].toString());
            u.setSurname(o[1].toString());
            usersFinal.add(u);
        }
        return usersFinal;
    }

    public List<SimplifiedUser> getReadersWhoRead (int page, long docId){
        Query query = entityManager.createNativeQuery("select u.first_name, u.surname FROM User u INNER JOIN users_documents ud " +
                "ON ud.user_id=u.id WHERE ud.document_id=? AND ud.sharing_type_id=2 AND ud.approval=2");

        query.setParameter(1, docId);
        query.setMaxResults(PAGE_SIZE);
        query.setFirstResult((page-1) * PAGE_SIZE);

        List<Object[]> users = query.getResultList();
        List<SimplifiedUser> usersFinal = new ArrayList<>();

        for(Object[] o : users){
            SimplifiedUser u = new SimplifiedUser();
            u.setFirstName(o[0].toString());
            u.setSurname(o[1].toString());
            usersFinal.add(u);
        }
        return usersFinal;
    }

    @Override
    public UsersCases getUsersDocuments(long userId, long docId, long sharingTypeId){
        UserDocumentsId userDocumentsId = new UserDocumentsId(userId, docId, sharingTypeId);
        try {
            return entityManager.find(UsersCases.class, userDocumentsId);
        } catch (Exception e){
            return null;
        }
    }

}
