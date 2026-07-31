package com.golden.geese;

public class RegularUser extends User {

    public RegularUser() {
        super();
    }

    public RegularUser(String username, String pfp) {
        super(username, pfp);
    }

    @Override
    public boolean isAdmin() {
        return false;
    }
}
