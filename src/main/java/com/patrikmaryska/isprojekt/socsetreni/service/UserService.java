package com.patrikmaryska.isprojekt.socsetreni.service;

import com.patrikmaryska.isprojekt.socsetreni.model.RequestBody.UserBearer;
import com.patrikmaryska.isprojekt.socsetreni.model.Role;
import com.patrikmaryska.isprojekt.socsetreni.model.User;
import com.patrikmaryska.isprojekt.socsetreni.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupService groupService;

    
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }


    public User findUserById(long id){

        return userRepository.findById(id).get();
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.getUserByEmail(email);
    }

    public List<Role> getUsersRoles(long id){
        return userRepository.getUsersRoles(id);
    }

    public Role getRoleByName(String name){
        return userRepository.findRoleByName(name);
    }

    public void createUser(User user) {
        user.setPassword(encoder().encode(user.getPassword()));
        userRepository.createUser(user);
    }

    public void updateUser(User user) {

        userRepository.updateUser(user);
    }

    public List<UserBearer> getUsersBySurname(String surname, int page) {
        List<User> users =  userRepository.getUsersBySurname(surname, page);
        List<UserBearer> userBearers = new ArrayList();
        users.forEach(user -> userBearers.add(new UserBearer(user.getId(), user.getFirstName(), user.getSurname(), user.getRoles(), user.getEmail(), user.isActive())));
        return userBearers;
    }

    public List<UserBearer> getAllUsers(int page) {
        List<User> users =  userRepository.getAllUsers(page);
        List<UserBearer> userBearers = new ArrayList();
        users.forEach(user -> userBearers.add(new UserBearer(user.getId(), user.getFirstName(), user.getSurname(), user.getRoles(), user.getEmail(), user.isActive())));
        return userBearers;
    }

    public List<User> getUsersFromIds(List<Long> longs){
        List<User> users = new ArrayList<>();

        longs.forEach(aLong -> {
            User user = userRepository.findById(aLong).get();
            users.add(user);
        });

        return users;
    }

    public List<User> getUsersFromGroupIds(List<Long> longsFromString) {
        return userRepository.getUsersByGroupId(longsFromString);
    }

    public List<User> getUsersForApprovingDocument(long docId){
        return userRepository.getUsersForApprovingDocument(docId);
    }

    public List<User> getUsersForReadingDocument(long docId){
        return userRepository.getUsersForReadingDocument(docId);
    }

    public List<Role> getAllRoles() {
       List<Role> roles = userRepository.getAllRoles();

       return roles;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void test(){
         userRepository.testing();
    }

    public void removeUserFromUsersDocuments(long id) {
        userRepository.removeUserFromUsersDocuments(id);
    }

    public List getActiveUsersBySurname(String name, int page){
        return userRepository.getActiveUsersBySurname(name, page);
    }

    public void deactivateUser(long id) {
        User user = findUserById(id);
        if(user.isActive()){
            groupService.removeUserFromGroups(id);
            removeUserFromUsersDocuments(id);
        }
        user.setActive(!user.isActive());

        updateUser(user);
    }

    public List<User> findByLikeActive(String surname, String email, int page) {
        Optional<User> user = getUserByEmail(email);
        List<User> users = getActiveUsersBySurname(surname, page);
      //  user.ifPresent(users::remove);

        return users;
    }

    public void changePassword(String name, String oldPassword, String newPassword) throws BadCredentialsException {
        User user = userRepository.getUserByEmail(name).get();
       if(encoder().matches(oldPassword, user.getPassword())){
            if(!encoder().matches(newPassword, user.getPassword())){
                userRepository.changePassword(name, encoder().encode((newPassword)));
            } else {
                throw new InternalError("Provided password is the same or too familiar with your current one. Choose different one please");
            }
        } else {
            throw new BadCredentialsException("Provided password is not correct.");
        }
    }
}

