package com.golden.geese.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.golden.geese.HomeFragment;
import com.golden.geese.R;
import com.golden.geese.SessionManager;
import com.golden.geese.User;

import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.model.FirebaseAuthRepository;

/**
 * Main Activity view to contain different screen fragments
 *   which is connected to the repository to verify if the user is logged in in order to
 *   display correct screen on app open.
 *  */
public class MainActivity extends AppCompatActivity {

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        authRepository = new FirebaseAuthRepository();
        authRepository.getCurrentUser(new AuthCallBack() {
            @Override
            public void onSuccess(User user) {
                SessionManager.getInstance().setCurrentUser(user);
                if (savedInstanceState == null) {
                    loadFragment(new HomeFragment());
                }
            }

            @Override
            public void onError(String msg) {
                if (savedInstanceState == null) {
                    loadFragment(new WelcomeFragment());
                }
            }
        });
    }

    /**
     * Loads a screen fragment into the fragment container
     * @param fragment the fragment to be loaded
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}