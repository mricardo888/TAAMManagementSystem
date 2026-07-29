package com.golden.geese;

public class Like extends Interaction {

    Likeable likedObject;
    public Like(User author, Likeable likedObject) {
        super(author);
        this.likedObject = likedObject;
    }

}
