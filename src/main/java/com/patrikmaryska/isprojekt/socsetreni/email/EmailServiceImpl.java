package com.patrikmaryska.isprojekt.socsetreni.email;

import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmailServiceImpl implements EmailService {

    @Autowired
    public JavaMailSender emailSender;

    private String address = "https://casemanager.cz:8443";


    @Async
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }


    public Map<String, String> createMessage(int type, User user, Case aCase){
        Map<String, String > map = new HashMap<>();
        String message = "";
        String subject = "";
        switch (type){
            case 1: // DOCUMENT APPROVED
                message = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " bylo schváleno. Šetření bude pro Vás dostupné v aplikaci od: " + formatTimeMessage(aCase.getActiveStartTime()) +
                        " do " + formatTimeMessage(aCase.getActiveEndTime())+ ". Je Vaše povinnost potvrdit seznámení se s tímto sociálním šetřením.\n" + address;
                subject = "Sociální šetření " + aCase.getCsurname() + " " + aCase.getCname() + " je právě dostupné.";
                 break;
            case 2: // DOCUMENT FAILED
                message = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " nebylo schváleno. Uživatel " + user.getFirstName() + " " + user.getSurname() +
                        " nesouhlasí s přijmutím klienta na základě přiloženého sociálního šetření. Sociální šetření je zamítnuto. \n" + address;
                subject = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " nebylo schváleno.";
                break;
            case 3: // DOCUMENT FOR APPROVING
                message = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " , které vytvořil/a: " + aCase.getUser().getFirstName() + " " + aCase.getUser().getSurname() +
                        " bylo zahájeno. Je Vaší povinnosti provést schválení, nebo zamítnutí vytvořeného šetření do " + formatTimeMessage(aCase.getApprovalEndTime())+". Šetření je dostupné" +
                        " v aplikaci momentálně.\n" + address;
                subject = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " vyžaduje schválení";
                break;
            case 4: // DOCUMENT WAS BLOCK DUE TO RUNNING OUT OF TIME
                message = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " nebylo úspěšně schváleno. Sociální šetření nebylo schváleno některými uživateli nebo vypršel čas ke schválení." + " Sociální šetření je momentálně zamítnuto. \n" + address;
                subject = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " bylo zrušeno z důvodu neschválení v časovém limitu";
            break;
            case 5: // APPROVED BY ALL USERS
                message = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " , které vytvořil/a: " + aCase.getUser().getFirstName() + " "  + aCase.getUser().getSurname() + " bylo schváleno všemi zvolenými uživateli. Informace o sociálním šetření budou dostupné vybraným uživatelům od  " + formatTimeMessage(aCase.getActiveStartTime()) + ".\n " + address;
                subject = "Sociální šetření " + aCase.getCname() + " " + aCase.getCsurname() + " bylo schváleno.";
                break;
        }
        map.put("message", message);
        map.put("subject", subject);

        return map;
    }

    @Override
    public String formatTimeMessage(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");

        return format.format(date);
    }
}
