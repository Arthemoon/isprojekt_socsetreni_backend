package com.patrikmaryska.isprojekt.socsetreni.controller;

import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.Approval;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.SimplifiedUser;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UsersApproval;
import com.patrikmaryska.isprojekt.socsetreni.service.CaseService;
import com.patrikmaryska.isprojekt.socsetreni.service.UserService;
import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping("/documents")
public class CaseController {

    @Autowired
    private CaseService caseService;
    @Autowired
    private UserService userService;

    private static Logger docLogger = LoggerFactory.getLogger("doc");

    @GetMapping("")
    @Secured({("ROLE_USER"), ("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public List<Case> getAllDocs(OAuth2Authentication auth, @RequestParam(defaultValue = "1") int page){
        return caseService.getAllUsersDocumentsByEmail(auth.getName(), page);
    }

    @GetMapping(value = "/{id}")
    @Secured({("ROLE_USER"), ("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public void generateReport(OAuth2Authentication auth, HttpServletResponse response, @PathVariable("id") long id) {
        String email = auth.getName();
        User user = userService.getUserByEmail(email).get();
        Optional<Case> doc = caseService.getDocumentById(id);

        if(doc.isPresent() && caseService.hasUserAccessToDocument(doc.get().getId(), user.getId())){
            try {
                caseService.generateReport(doc.get().getResourcePath(), response);
                docLogger.info("User " + email + " has accessed the document id" + id);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | IOException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error while generating document.");
            }
        } else {
            docLogger.warn("User " + email + " has unsuccessfuly accessed the document id" + id + ". User does not have access or document does not exist");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Case was not found or you do not have access to this document,");
        }
    }


    @PostMapping(value="")
    @ResponseStatus(HttpStatus.OK)
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public void getDocumentFromClient(@RequestParam("file") MultipartFile multipartFile, OAuth2Authentication auth,
                                      @RequestParam("name")String name,
                                      @RequestParam("surname") String surname,
                                      @RequestParam("pid") String rc,
                                      @RequestParam("desc")String desc,
                                      @RequestParam("ApprovalGroups")String approvalGroup,
                                      @RequestParam("readers") String readers,
                                      @RequestParam("approvalTime") String approvalTime,
                                      @RequestParam("startOfReading") String startOfReading,
                                      @RequestParam("endOfReading") String endOfReading){

        if(caseService.documentDatesValidity(approvalTime, startOfReading, endOfReading)
                && caseService.checkDocumentValidity(name, surname, rc, desc, approvalGroup, readers)){
            if(caseService.checkFileValidity(multipartFile)){
                try {
                    caseService.createDocument(readers, approvalGroup, auth.getName(), name, surname, rc, desc, approvalTime,
                            startOfReading, endOfReading, multipartFile);
                } catch (ParseException e) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Dates are invalid.");
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error while processing file.");
                } catch (IOException e) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving file.");
                }
            } else {
                docLogger.warn("User " + auth.getName() + " tried to create invalid document.");
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "File is not valid .pdf file.");
            }
        } else {
            docLogger.warn("Creation of document " + " by " + auth.getName() + " has failed due to bad parameters");
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title, description, groups or dates are invalid.");
        }
    }

    @PutMapping(value = "")
    @Secured({("ROLE_USER"),("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public ResponseEntity updateApprovals(OAuth2Authentication auth, @Valid @RequestBody Approval approvals){
        String email = auth.getName();
        long userId = userService.getUserByEmail(email).get().getId();
        int approval = Integer.parseInt(approvals.getApproval());
        long doc_id = approvals.getDoc_id();

        try {
            caseService.updateSharing(userId, doc_id, approval);
            docLogger.info("User " + email + " has voted. Case id" + doc_id);
        } catch (Exception e){
            docLogger.error("User " + email + " has voted unsuccessfuly. Case id" + doc_id);
            System.out.println(e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Your approval could not have been saved.");
        }

        return new ResponseEntity(HttpStatus.OK);
    }


    @GetMapping("/find")
    @Secured({("ROLE_USER"),("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public List<Case> findDocumentByTitle(@RequestParam("name")String name, OAuth2Authentication auth, @RequestParam(defaultValue = "1") int page){
        if(name.trim().length() > 0 && name.length() <= 15 && name.matches("^[a-zá-žA-ZÁ-Ž0-9\\)\\(\\!\\?\\s]+$")){
            try {
                return caseService.findDocumentByTitle(name, auth.getName(), page);
            } catch (NotFoundException e) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User was not found.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Title can contain only letters, digits and some" +
                    " special characters ?, !. Length should be between 3-15 chars.");
        }
    }

    @GetMapping(value = "/owner")
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public List<Case> getUsersCreatedDocs(OAuth2Authentication auth, @RequestParam String year,
                                          @RequestParam String month, @RequestParam(defaultValue = "1") int page){
        User user = userService.getUserByEmail(auth.getName()).get();
        List<Case> caseList;

        if(month.matches("^(1[0-2]|[1-9])$") && year.matches("^\\d{4}$")){
            caseList = caseService.getUsersCreatedDocuments(user.getId(), Integer.parseInt(year), Integer.parseInt(month), page);
            docLogger.info("User " + auth.getName() + " has accessed his documents.");
        } else {
            docLogger.warn("User " + auth.getName() + " has unsuccessfuly accessed his documents.");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Year or month are not in the correct format.");
        }

       return caseList;
    }

    @GetMapping(value = "/owner/{id}")
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public List<UsersApproval> getUsersForDocument(@PathVariable("id") long id, OAuth2Authentication auth){

        Optional<User> user = userService.getUserByEmail(auth.getName());

        List<UsersApproval> users;

        if(user.isPresent()){
            if(caseService.hasUserPermissionToAccessDocument(id, user.get().getId())){
                users= caseService.getUsersForDocument(id, user.get().getId());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You do not have access to this document.");
            }

        } else {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User was not found.");
        }

        return users;
    }


    @GetMapping("/history")
    @Secured({("ROLE_USER"),("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public List<Case> getDocumentsByYearAndByMonth(@RequestParam(name = "year") String year,
                                                   @RequestParam(name = "month") String month,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   OAuth2Authentication auth){
        List<Case> docs;
        if(month.matches("^(1[0-2]|[1-9])$") && year.matches("^\\d{4}$")){
            docs = caseService.getDocumentsByYearAndByMonth(Integer.parseInt(year), Integer.parseInt(month), auth.getName(), page);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Year or month are not in the correct format.");
        }

       return docs;
    }

    @GetMapping("/all")
    @Secured(("ROLE_ADMIN"))
    public List<Case> getAllDocumentsByYearAndMonth(@RequestParam("year") String year, @RequestParam("month") String month,
                                                    @RequestParam(defaultValue = "1") int page, OAuth2Authentication auth){

        List<Case> docs;
        if(month.matches("^(1[0-2]|[1-9])$") && year.matches("^\\d{4}$")){
            docs = caseService.getAllDocumentsByYearAndMonth(Integer.parseInt(year), Integer.parseInt(month), page, auth.getName());
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Year or month are not in the correct format.");
        }

        return docs;
    }


    @DeleteMapping("/{id}")
    @Secured(("ROLE_ADMIN"))
    public void deleteDocument(@PathVariable("id") long id, OAuth2Authentication auth){
        try {
            caseService.deleteDocument(id);
            docLogger.info("User " + auth.getName() + " has deleted document with id " + id);
        } catch (Exception e){
            docLogger.error("User " + auth.getName() + " tried to delete document with id " + id + " unsuccessfuly.");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This document could not have been deleted.");
        }
    }

    @GetMapping("readers")
    public List<SimplifiedUser> readersWithoutRead(@RequestParam(defaultValue = "1") int page, @RequestParam long docId, @RequestParam int st, @RequestParam int approval){

        return caseService.getReaders(page, docId, st, approval);
    }
}
