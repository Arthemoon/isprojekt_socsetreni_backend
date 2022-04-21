package com.patrikmaryska.isprojekt.socsetreni.service;

import com.patrikmaryska.isprojekt.socsetreni.model.CaseGroup;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.GroupBody;
import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UnitBody;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import com.patrikmaryska.isprojekt.socsetreni.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserService userService;

    private final int GROUP_MAX_NUMBER = 30;


    public void saveUnit(UnitBody unit, String email){

        User user =  userService.getUserByEmail(email).get();

       /* if(user.getCaseGroups().size()+1 > GROUP_MAX_NUMBER){
            throw new IllegalStateException("You can have only 30 groups. Please remove some of your groups.");
        }*/
        List<User> users = userService.getUsersFromIds(unit.getIds());

        CaseGroup un = new CaseGroup();
        un.setName(unit.getName());
   //     un.setUser(user);
        un.setUsers(users);
        groupRepository.save(un);
    }

    public List<com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.Group> getUsersUnits(String email, int page) {
         return groupRepository.getUsersUnits(email, page);
    }

    public void deleteUnit(long id, String email) throws AuthorizationServiceException {
        CaseGroup CaseGroup = groupRepository.getOne(id);
        User user = userService.getUserByEmail(email).get();

       /* if(user.getCaseGroups().contains(CaseGroup)){*/
            groupRepository.deleteById(id);
    /*    } else {
            throw new AuthorizationServiceException("You do not have permission to delete this CaseGroup.");
        }*/
    }

    public void updateUnit(GroupBody group, String email) throws AuthorizationServiceException {
        User user = userService.getUserByEmail(email).get();
        if(group.getIds().size() == 0){
            deleteUnit(group.getId(), email);
            return;
        }

     //   if(user.getCaseGroups().stream().anyMatch(unit -> unit.getId() == group.getId())){
            CaseGroup unit = new CaseGroup();
            unit.setId(group.getId());
            unit.setName(group.getName());
          //  unit.setUser(user);
            unit.setUsers(userService.getUsersFromIds(group.getIds()));
            groupRepository.updateUnit(unit);
    /*    } else {
            throw new AuthorizationServiceException("You cannot update this group.");
        }*/
    }

    public void removeUserFromGroups(long id) {
        groupRepository.removeUserFromGroups(id);
    }
}
