package com.patrikmaryska.isprojekt.socsetreni.email;

import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.User;

import java.util.Date;
import java.util.Map;

public interface EmailService {
    void sendSimpleMessage(String to,
                           String subject,
                           String text);

    Map<String, String> createMessage(int type, User user, Case aCase);

    String formatTimeMessage(Date date);
}