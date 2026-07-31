package com.golden.geese;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class InteractionManager<T extends Interaction> {
    protected List<T> interactions;

    public InteractionManager() {
        interactions = new ArrayList<T>();
    }

    public int getNumInteractions() {
        return interactions.size();
    }

    public List<T> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    public abstract void add(User user, T interaction);

    public abstract void delete(User user, T interaction);
}
