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

    @Override
    public boolean canManageArtifacts() {
        return true;
    }

    @Override
    public boolean canDelete(Comment comment) {
        return comment != null;
    }
}
