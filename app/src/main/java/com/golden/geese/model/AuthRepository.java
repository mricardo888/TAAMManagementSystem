package com.golden.geese.model;

import com.golden.geese.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    private final FirebaseAuth mAuth;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void signIn(String email, String pwd, AuthCallBack callback) {
        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
//                            Customer user = new Customer();
//                            user.setUsername(firebaseUser.getEmail());
//                            callback.onSuccess(user);
                        } else {
                            callback.onError("Sign in succeeded but no user found.");
                        }
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign in failed.";
                        callback.onError(errorMsg);
                    }
                });
    }

    public void signUp(String email, String pwd, AuthCallBack callback) {
        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
//                            Customer user = new Customer();
//                            user.setUsername(firebaseUser.getEmail());
//                            callback.onSuccess(user);
                        } else {
                            callback.onError("Sign up succeeded but no user found.");
                        }
                    } else {
                        String errorMsg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Sign up failed.";
                        callback.onError(errorMsg);
                    }
                });
    }

//    public User getCurrentUser() {
//        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//        if (firebaseUser == null) {
//            return null;
//        }
//        Customer user = new Customer();
//        user.setUsername(firebaseUser.getEmail());
//        return user;
//    }
}
