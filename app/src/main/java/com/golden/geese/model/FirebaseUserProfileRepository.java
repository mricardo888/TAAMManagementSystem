package com.golden.geese.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/** UserProfileRepository backed by the Firebase Realtime Database /users node */
public class FirebaseUserProfileRepository implements UserProfileRepository {
    private final DatabaseReference usersRef;

    /** Binds to the /users node of the default FirebaseDatabase instance */
    public FirebaseUserProfileRepository() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    public void createUserProfile(String uid, String username, String email, RepositoryCallback<Void> callback) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("email", email);
        profile.put("isAdmin", false);
        usersRef.child(uid).setValue(profile)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void isAdmin(String uid, RepositoryCallback<Boolean> callback) {
        usersRef.child(uid).child("isAdmin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onSuccess(Boolean.TRUE.equals(snapshot.getValue(Boolean.class)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    @Override
    public void updateUsername(String uid, String username, RepositoryCallback<Void> callback) {
        usersRef.child(uid).child("username").setValue(username)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getUsername(String uid, RepositoryCallback<String> callback) {
        usersRef.child(uid).child("username")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onSuccess(snapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}
