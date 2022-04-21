package com.patrikmaryska.isprojekt.socsetreni.controller;

import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.Group;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.GroupBody;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UnitBody;
import com.patrikmaryska.isprojekt.socsetreni.service.GroupService;
import com.patrikmaryska.isprojekt.socsetreni.service.UserService;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/groups")
public class GroupController {

    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;

    private Logger logger = LoggerFactory.getLogger("user");

    @PostMapping("")
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public void createGroup(@Valid @RequestBody UnitBody unit, OAuth2Authentication auth){
        try {
            groupService.saveUnit(unit, auth.getName());
            logger.info("CaseGroup " + unit.getName() + " has been created by " + auth.getName());
        } catch (IllegalStateException error){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, error.getMessage());
        }
        catch (Exception e){
            logger.error("CaseGroup " + unit.getName() + " could not have been created by " + auth.getName() + ". Internal server error.");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "CaseGroup could not have been created.");
        }
    }

    @GetMapping("")
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public List<Group> getUsersUnits(OAuth2Authentication auth, @RequestParam(defaultValue = "1") int page){
        String email = auth.getName();

        return groupService.getUsersUnits(email, page);
    }

    @DeleteMapping("")
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public void deleteUserGroup(OAuth2Authentication auth, @RequestBody Group group){
        try {
            System.out.println("UNIT ID " + group.getId());
            groupService.deleteUnit(group.getId(), auth.getName());
            logger.info("CaseGroup with id " + group.getId() + " has been deleted by " + auth.getName());
        } catch (JDBCException e){
            logger.error("CaseGroup with id " + group.getId() + " could not have been deleted by " + auth.getName() + ". Internal server error");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CaseGroup could not have been deleted. This group was not found.");
        } catch (AuthorizationServiceException e){
            logger.error("CaseGroup with id " + group.getId() + " could not have been deleted by " + auth.getName() + ". User does not have access to this group.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CaseGroup could not have been deleted. You dont have access to this group.");
        }
    }


    @PutMapping("")
    @Secured({("ROLE_CASE_CREATOR"), ("ROLE_ADMIN")})
    public void updateUnit(@Valid @RequestBody GroupBody group, OAuth2Authentication auth){
        try {
            groupService.updateUnit(group, auth.getName());
            logger.info("CaseGroup " + group.getName() + " has been updated by " + auth.getName());
        } catch (Exception e){
            logger.error("CaseGroup " + group.getName() + " could not have been deleted by " + auth.getName() + ". Internal server error");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "CaseGroup could not have been updated. Check whether information are correct.");
        }
    }
}

