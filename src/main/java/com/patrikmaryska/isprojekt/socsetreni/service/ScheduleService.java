package com.patrikmaryska.isprojekt.socsetreni.service;

import com.patrikmaryska.isprojekt.socsetreni.email.EmailService;
import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import com.patrikmaryska.isprojekt.socsetreni.model.UsersCases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ScheduleService {

    @Autowired
    private CaseService caseService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;


  //  @Scheduled(cron = "0 00 23 * * ?")
    @Scheduled(cron = "0 0/15 * * * ?")
    public void blockPassedDocuments() {
        List<Case> passedCases = caseService.getAllPassedDocuments();

        if (passedCases.size() > 0) {
            passedCases.forEach(document -> {
                caseService.blockDocument(document);
                List<User> users = userService.getUsersForApprovingDocument(document.getId());

            /*    if(document.getUser().isActive()){
                    users.add(document.getUser());
                }*/

                Map<String, String> map = emailService.createMessage(4, document.getUser(), document);
                caseService.sendEmail(users, map.get("subject"), map.get("message"));
            });
        }
    }

    @Transactional
    @Scheduled(cron = "0 0/20 * * * ?")
    public void sendEmailAboutReleasingNewDocumentToReaders(){
        List<UsersCases> releasingDocuments = caseService.getNewActiveDocuments();

        releasingDocuments.forEach(usersCases -> {
            Map<String, String> map = emailService.createMessage(1, usersCases.getUser(), usersCases.getaCase());
            emailService.sendSimpleMessage(usersCases.getUser().getEmail(), map.get("subject"), map.get("message"));
            usersCases.setEmailSent(true);
            caseService.updateEmailSent(usersCases);
        });

        /*
       if(releasingDocuments.size() > 0){
            releasingDocuments.forEach(document -> {
                List<User> users = userService.getUsersForReadingDocument(document.getaCase().getId());

                if(document.getUser().isActive()){
                    users.add(document.getUser());
                }

                System.out.println("USERS");
                users.forEach(user -> {
                    System.out.println("user " + user.toString());
                });

                Map<String, String> map = emailService.createMessage(1, document.getUser(), document.getaCase());
                caseService.sendEmail(users, map.get("subject"), map.get("message"));
                document.setEmailSent(true);
                caseService.updateEmailSent(document);
            });*/
        }
    }