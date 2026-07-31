package com.golden.geese;

public abstract class ArtifactManager {
    protected final Session session;

    protected ArtifactManager(Session session) {
        this.session = session;
    }

    protected User getCurrentUser() {
        User user = session.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user is logged in.");
        }
        return user;
    }
}
