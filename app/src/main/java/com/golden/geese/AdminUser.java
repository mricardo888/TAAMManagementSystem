package com.golden.geese;

public class AdminUser extends User {

    public AdminUser() {
        super();
    }

    public AdminUser(String username, String pfp) {
        super(username, pfp);
    }







    @Override
    public boolean isAdmin() {
        return true;
    }
}
