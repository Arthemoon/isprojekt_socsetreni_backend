package com.patrikmaryska.isprojekt.socsetreni.service;

import com.patrikmaryska.isprojekt.socsetreni.email.EmailService;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.SimplifiedUser;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UsersApproval;
import com.patrikmaryska.isprojekt.socsetreni.utils.EncrypterDecrypter;
import com.patrikmaryska.isprojekt.socsetreni.repository.CaseRepository;
import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.CaseState;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import com.patrikmaryska.isprojekt.socsetreni.model.UsersCases;
import javassist.NotFoundException;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class CaseService {
    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @Value("${pdf.key}")
    private String key;

    @Transactional(noRollbackFor = TransactionSystemException.class)
    public void blockDocument(Case aCase){
        caseRepository.blockDocument(aCase);
    }

    public List<Case> getAllDocuments() {

        return caseRepository.findAll();
    }

    public List<Case> getAllUsersDocuments(long id) {
        return caseRepository.getAllUsersDocuments(id);
    }

    public Optional<Case> getDocumentById(long id) {
        return caseRepository.findById(id);
    }

    public List<Case> getAllUsersDocumentsByEmail(String email, int page) {
        List<Case> dcs = caseRepository.getAllDocsByEmail(email, page);
        return dcs;
    }



    public String write(MultipartFile file, String fileType, Case aCase) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        createDirectory(date);
        String folder = System.getProperty("user.home");
        String generatedName = generateName();

        String encPath = folder + "\\pdf\\" + date + "/" + generatedName + ".pdf"; // .pdf

        encryptFile(file, encPath);

        aCase.setResourcePath(Paths.get(encPath).toString());
        aCase.setName(generatedName);

        return generatedName;
    }


    public Case getDocumentByName(String name) {
        return caseRepository.getDocumentByName(name);
    }

    private boolean createDirectory(String date) {
        boolean success = new java.io.File(System.getProperty("user.home"), "pdf\\" + date).mkdirs();

        return success;
    }

    public void saveDocument(Case aCase) throws PersistenceException {
        caseRepository.saveDocument(aCase);
    }

    public String generateName() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public void insertSharing(String name, List<User> users, long documentTypeId, boolean emailSent, Date appTime) {
        caseRepository.insertSharing(name, users, documentTypeId, emailSent, appTime);
    }

    public void updateSharing(long userId, long documentId, int approval) {
        Optional<Case> optionalDocument = caseRepository.findById(documentId);
        User user = userService.findUserById(userId);

        if(optionalDocument.isPresent()){
            Case aCase = optionalDocument.get();

            if(aCase.getCaseState().getId() == 2){
                if(caseRepository.getUsersDocuments(userId, documentId, 1) == null){
                    return;
                }

                caseRepository.updateSharing(userId, documentId, approval);

                if(approval == 0){
                    List<User> users = userService.getUsersForApprovingDocument(documentId);
                    Map<String, String> map = emailService.createMessage(2, user, aCase);

                    if(aCase.getUser().isActive()){
                        users.add(aCase.getUser());
                    }

                    setDocumentType(documentId, 3);

                    sendEmail(users, map.get("subject"), map.get("message"));

                } else if(isDocumentApproved(documentId)){
                    setDocumentType(documentId, 1);

                    List<User> users = userService.getUsersForApprovingDocument(documentId);
                    if(aCase.getUser().isActive()){
                        users.add(aCase.getUser());
                    }

                    Map<String, String> map = emailService.createMessage(5, user, aCase);
                    sendEmail(users, map.get("subject"), map.get("message"));
                }
            } else if(aCase.getCaseState().getId() == 1){
                if(caseRepository.getUsersDocuments(userId, documentId, 2) == null){
                    return;
                }
                caseRepository.updateSharing(userId, documentId, approval);
            } else {
                return;
            }
        }
    }



    private void encryptFile(MultipartFile file, String name) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
            SecretKey secretKey = EncrypterDecrypter.getSecretKey(key);
            EncrypterDecrypter encrypterDecrypter
                    = new EncrypterDecrypter(secretKey, "AES/CBC/PKCS5Padding");
            encrypterDecrypter.encrypt(name, file);

    }

    public byte[] decryptFile(String name) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        SecretKey secretKey = EncrypterDecrypter.getSecretKey(key);
        EncrypterDecrypter encrypterDecrypter
                = new EncrypterDecrypter(secretKey, "AES/CBC/PKCS5Padding");

        return encrypterDecrypter.decrypt(name);
    }

    private void setDocumentType(long docId, long documentTypeId){
        CaseState caseState = caseRepository.getDocumentState(documentTypeId);
        Optional<Case> updatedDocument = caseRepository.findById(docId);
        if(updatedDocument.isPresent()){
            Case aCase = updatedDocument.get();
            aCase.setCaseState(caseState);
            caseRepository.updateDocument(aCase);
        }
    }

    public void sendEmail(List<User> users, String subject, String message){
        users.forEach(user -> {
            emailService.sendSimpleMessage(user.getEmail(), subject, message);
        });
    }

    public boolean hasUserAccessToDocument(long documentId, long userId){
        return caseRepository.hasUserAccessToDocument(documentId, userId);
    }

    public List<Case> findDocumentByTitle(String name, String email, int page) throws NotFoundException {
        List<Case> aCases = new ArrayList<>();
        if(name.trim().length() > 0 && name.length() <= 12){
            Optional<User> opt = userService.getUserByEmail(email);
            if(opt.isPresent()){
                long id = opt.get().getId();
                aCases = caseRepository.findDocumentByTitle(name, id, page);
            } else {
                throw new NotFoundException("User was not found.");
            }
        }
        return aCases;
    }

    public List<Case> getUsersCreatedDocuments(long id, int year, int month, int pageNumber) {
        List<Case> aCases = caseRepository.getUsersCreatedDocuments(id, year, month, pageNumber);

        return aCases;
    }

    public List<UsersApproval> getUsersForDocument(long docId, long userId) {
        return caseRepository.getUsersForDocument(docId, userId);
    }


    public boolean isDocumentApproved(long docId){
        if(caseRepository.getExpectedCountOfApprovals(docId) == caseRepository.getCountOfApprovals(docId)){
            return true;
        }
        return false;
    }

    public CaseState getDocumentState(long id){
        return caseRepository.getDocumentState(id);
    }

    public List<Case> getAllPassedDocuments() {
        return caseRepository.getAllPassedDocuments();
    }

    public List<Case> getDocumentsByYearAndByMonth(int year, int month, String email, int page) {

        List<Case> docs =  caseRepository.getDocumentsByYearAndMonth(year, month, email, page);
        return docs;
    }

    public List<UsersCases> getNewActiveDocuments() {
        return caseRepository.getNewActiveDocuments();
    }

    public void updateEmailSent(UsersCases usersCases){
        caseRepository.updateEmailSent(usersCases);
    }


    public boolean documentDatesValidity(String approvalTime, String startOfReading, String endOfReading) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            if(formatter.parse(approvalTime).after(formatter.parse(formatter.format(new Date()))) ||
                    formatter.parse(approvalTime).equals(formatter.parse(formatter.format(new Date())))){
                if(formatter.parse(startOfReading).after(formatter.parse(formatter.format(new Date())))
                        && formatter.parse(endOfReading).after(formatter.parse(formatter.format(new Date()))))
                    if(formatter.parse(endOfReading).after(formatter.parse(startOfReading))){
                        return true;
                    }
            }
        }catch (ParseException e){
            return false;
        }
        return false;
    }

    private boolean checkIfArraysAreTheSame(String[] array1, String[] array2){
        return Arrays.equals(array1, array2);
    }

    public boolean checkDocumentValidity(String name, String surname, String rc, String desc, String approvalGroup, String readers) {
        if(name.length() < 1 || name.length() > 50 || name.trim().length() == 0 && !name.matches("^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s]+$") &&
                surname.length() < 1 || surname.length() > 50 || surname.trim().length() == 0 && !surname.matches("^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s]+$") &&
                desc.length() <= 1 || desc.length() > 255 || desc.trim().length() == 0 &&
                 approvalGroup.trim().length() == 0 && !approvalGroup.matches("\\d,|\\d") && readers.trim().length() == 0 && !readers.matches("\\d,|\\d")
                && readers.equals(approvalGroup) && checkIfArraysAreTheSame(approvalGroup.split(","), readers.split(","))){
            return false;
        }
        return true;
    }

    public boolean checkFileValidity(MultipartFile multipartFile) {
        try {
            if(!multipartFile.isEmpty())
            {
                Tika tika = new Tika();
                String detectedType = tika.detect(multipartFile.getBytes());
                if (detectedType.equals("application/pdf")) {
                    return true;
                }
            }
        } catch (IOException e){
            return false;
        }
        return false;
    }

    public boolean deleteDocument(long id){
        boolean x = false;
        Case aCase = caseRepository.getOne(id);
        if(deleteDocumentFromDisk(aCase.getResourcePath())){
            caseRepository.deleteAllUsersDocumentsByDocumentId(id);
            x =  caseRepository.deleteDocument(id);
        }
        return x;
    }

    private boolean deleteDocumentFromDisk(String path){
        File file = new File(path);
        return file.delete();
    }


    public List<Case> getAllDocumentsByYearAndMonth(int year, int month, int page, String email){
        List<Case> aCases = caseRepository.getAllDocumentsByYearAndMonth(year, month, page);

        return aCases;

    }

    public List<Long> getLongsFromString(String value){
        String[] values = value.split(",");

        List<Long> longs = new ArrayList<>();

        for(int i = 0; i < values.length; i++){
            longs.add(Long.parseLong(values[i]));
        }

        return longs;
    }

    public void createDocument(String readers, String approvalGroup, String userEmail, String name, String surname, String pid, String desc,
                               String approvalTime, String startOfReading, String endOfReading, MultipartFile file) throws ParseException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {

        List<User> approvalUsers = userService.getUsersFromGroupIds(getLongsFromString(approvalGroup));
        List<User> usersForReading;

        List<Long> ids  = getLongsFromString(readers);
        if(ids.contains(0L)){
            usersForReading = userService.getAllUsers();
        } else {
            usersForReading = userService.getUsersFromGroupIds(ids);
        }

        usersForReading.removeAll(approvalUsers);

        Optional<User> user = userService.getUserByEmail(userEmail);
        Case aCase = new Case(name, surname, pid, desc);
        aCase.setUploadDatetime(new Date());
        aCase.setCaseState(getDocumentState(2));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        aCase.setApprovalEndTime(formatter.parse(approvalTime));
        aCase.setActiveStartTime(formatter.parse(startOfReading));
        aCase.setActiveEndTime(formatter.parse(endOfReading));

        String docName = "";

        if(user.isPresent()){
                aCase.setUser(user.get());
                try {
                    docName = write(file, file.getName() + ".pdf", aCase);
                } catch(IOException e){
                    throw new IOException("Case error while saving on the disk...");
                }

                try {
                    saveDocument(aCase);
                }catch (PersistenceException e){
                    deleteDocumentFromDisk(aCase.getResourcePath());
                    return;
                }
        }

        Case finDoc = getDocumentByName(aCase.getName());

        insertSharing(docName, approvalUsers, 1, true, aCase.getUploadDatetime());
        insertSharing(docName, usersForReading, 2, false, aCase.getActiveStartTime());

        Case case2 =getDocumentByName(docName);
        Map<String,String> map = emailService.createMessage(3, case2.getUser(), case2);
        sendEmail( userService.getUsersForApprovingDocument(case2.getId()), map.get("subject"), map.get("message"));
    }

    public List<SimplifiedUser> getReaders(int page, long docId, int st, int approval){
        return caseRepository.getReaders(page, docId, st, approval);
    }

    public void generateReport(String resourcePath, HttpServletResponse response) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        byte[] fileContent =  decryptFile(resourcePath);
        streamReport(response, fileContent, resourcePath);
    }

    public void streamReport(HttpServletResponse response, byte[] data, String name)
            throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=" + name);
        response.setContentLength(data.length);

        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    public boolean hasUserPermissionToAccessDocument(long documentId, long userId) {
        return caseRepository.hasUserPermissionToAccessDocument(documentId, userId);
    }
}
