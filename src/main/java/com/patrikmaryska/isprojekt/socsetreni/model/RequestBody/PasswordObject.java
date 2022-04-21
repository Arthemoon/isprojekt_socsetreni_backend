package com.patrikmaryska.isprojekt.socsetreni.model.RequestBody;


import com.patrikmaryska.isprojekt.socsetreni.validation.ValidPassword;

public class PasswordObject {

    @ValidPassword(message = "Password is not ok.")
    private String oldPassword;

    @ValidPassword(message = "Password is not ok.")
    private String newPassword;

    public PasswordObject() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
