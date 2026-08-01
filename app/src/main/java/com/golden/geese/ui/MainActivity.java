package com.golden.geese.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.golden.geese.R;


import com.golden.geese.SessionManager;
import com.golden.geese.User;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.model.FirebaseAuthRepository;

public class MainActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_fragment_container), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        authRepository = new FirebaseAuthRepository();
        User currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            SessionManager.getInstance().setCurrentUser(currentUser);
        }

        if (savedInstanceState == null) {
            if (userIsLoggedIn()) {
//                loadFragment(new HomeFragment());
            } else {
                loadFragment(new WelcomeFragment());
            }
        }

    }

    private boolean userIsLoggedIn () {
        // TODO: Make the new class the that authenticates user and return if they are logged
        return false;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}