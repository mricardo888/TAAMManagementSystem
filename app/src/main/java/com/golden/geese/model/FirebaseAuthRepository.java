package com.golden.geese.model;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.golden.geese.AdminUser;
import com.golden.geese.RegularUser;
import com.golden.geese.User;

public class FirebaseAuthRepository implements AuthRepository {
    private final FirebaseAuth mAuth;
    private final UserProfileRepository userProfileRepository;

    public FirebaseAuthRepository() {
        this(FirebaseAuth.getInstance(), new FirebaseUserProfileRepository());
    }

    public FirebaseAuthRepository(FirebaseAuth mAuth, UserProfileRepository userProfileRepository) {
        this.mAuth = mAuth;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void signIn(String email, String pwd, AuthCallBack callback) {
        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            userProfileRepository.isAdmin(firebaseUser.getUid(), new RepositoryCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean isAdmin) {
                                    User user = isAdmin ? new AdminUser() : new RegularUser();
                                    user.setUid(firebaseUser.getUid());
                                    user.setUsername(firebaseUser.getEmail());
                                    user.setEmail(firebaseUser.getEmail());
                                    callback.onSuccess(user);
                                }

                                @Override
                                public void onError(String message) {
                                    callback.onError(message);
                                }
                            });
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

    @Override
    public void signUp(String email, String username, String pwd, AuthCallBack callback) {
        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            userProfileRepository.createUserProfile(firebaseUser.getUid(), username, email,
                                    new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            RegularUser user = new RegularUser();
                                            user.setUid(firebaseUser.getUid());
                                            user.setUsername(username);
                                            user.setEmail(email);
                                            callback.onSuccess(user);
                                        }

                                        @Override
                                        public void onError(String message) {
                                            callback.onError(message);
                                        }
                                    });
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

    @Override
    public User getCurrentUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            return null;
        }
        RegularUser user = new RegularUser();
        user.setUid(firebaseUser.getUid());
        user.setUsername(firebaseUser.getEmail());
        user.setEmail(firebaseUser.getEmail());
        return user;
    }

    @Override
    public void signOut() {
        mAuth.signOut();
    }

    @Override
    public void reauthenticate(String currentPassword, RepositoryCallback<Void> callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            callback.onError("No signed-in user.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);
        firebaseUser.reauthenticate(credential)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateEmail(String newEmail, RepositoryCallback<Void> callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("No signed-in user.");
            return;
        }

        firebaseUser.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updatePassword(String newPassword, RepositoryCallback<Void> callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("No signed-in user.");
            return;
        }

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
