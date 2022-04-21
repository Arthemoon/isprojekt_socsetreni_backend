package com.patrikmaryska.isprojekt.socsetreni.controller;

import com.patrikmaryska.isprojekt.socsetreni.model.Case;
import com.patrikmaryska.isprojekt.socsetreni.model.CaseState;
import com.patrikmaryska.isprojekt.socsetreni.model.Role;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import com.patrikmaryska.isprojekt.socsetreni.service.CaseService;
import com.patrikmaryska.isprojekt.socsetreni.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class OauthController {

    @Autowired
    private TokenStore tokenStore;
    @Autowired
    private CaseService caseService;
    @Autowired
    private UserService userService;

    private static Logger logger = LoggerFactory.getLogger("login");

    @PostMapping(value = "/oauth/revoke-token")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, OAuth2Authentication auth) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            String tokenValue = authHeader.replace("Bearer", "").trim();
            OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
            tokenStore.removeAccessToken(accessToken);
            logger.info("User " + auth.getName() + " has logged out.");
        }
    }

    @GetMapping(value = "oauth/valid")
    @ResponseStatus(HttpStatus.OK)
    public String isTokenValid(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            String tokenValue = authHeader.replace("Bearer", "").trim();
            OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);

            if(accessToken != null){
                return "OK";
            }
        }
        return "NOT OK";
    }

    private String generateTitle(int len){
         final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
         SecureRandom rnd = new SecureRandom();

            StringBuilder sb = new StringBuilder( len );
            for( int i = 0; i < len; i++ )
                sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
            return sb.toString();
    }

    @GetMapping(value = "test2")
    public void test2(){
        for(int i = 0; i < 8; i++){
            int jmeno = i;
            Thread thread = new Thread(() -> {
                for(int j = 0; j < 1000; j++){
                    User user = new User();
                    String x = generateTitle(5);
                    user.setFirstName("Petr");
                    user.setSurname("Mil" + x);
                    user.setEmail(x+"@fakeemail.cz");
                    user.setPassword("515EA6ir$");
                    user.setActive(true);
                    List<Role> roles = List.of(userService.getRoleByName("USER"));
                    user.setRoles(roles);

                    System.out.println("Vlakno " + jmeno + "Cyklus: " + j);

                    userService.createUser(user);
                }
            });
            thread.start();
        }
        }

    @GetMapping(value = "test")
    public void test() throws ParseException {

        for(int j = 1; j < 21; j++){
            int f = j;
            Thread t1 = new Thread(() -> {
                String jmeno = "Vlakno " + f;
                for(int i = 0; i < 1000; i++){
                    Case aCase = new Case();
                    CaseState caseState = caseService.getDocumentState(2);
                    aCase.setCaseState(caseState);
                    String x = generateTitle(6);
                    aCase.setDescription("Popis testu");
                    System.out.println("TITLE " + x + " I " + i + " VLAKNO:: " + jmeno);
                    aCase.setUser(userService.findUserById(4));
                    aCase.setUploadDatetime(new Date());
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    String name = caseService.generateName();

                    try {
                        aCase.setApprovalEndTime(formatter.parse("2020-01-30 00:00"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        aCase.setActiveStartTime(formatter.parse("2020-02-01 00:00)"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        aCase.setActiveEndTime(formatter.parse("2020-02-20 00:00"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    aCase.setResourcePath("C:\\Users\\patri\\pdf\\25-09-2019\\2DTR8JP3A1WND3AS2O.pdf");
                    aCase.setName(name);

                    List<User> approvalls = userService.getUsersFromGroupIds(caseService.getLongsFromString("23126"));
                    List<User> readerrs = userService.getUsersFromGroupIds(caseService.getLongsFromString("23127"));

                    caseService.saveDocument(aCase);



                    caseService.insertSharing(name, approvalls, 1, true, aCase.getUploadDatetime());
                    caseService.insertSharing(name, readerrs, 2, false, aCase.getActiveStartTime());

                }
    });
            t1.start();
}}}
