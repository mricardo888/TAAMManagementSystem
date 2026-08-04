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

    @Override
    public boolean canManageArtifacts() {
        return false;
    }

    @Override
    public boolean canDelete(Comment comment) {
        return comment != null && comment.isAuthoredBy(this);
    }
}
