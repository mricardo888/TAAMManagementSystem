package com.golden.geese;

import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.presenter.SignupPresenter;
import com.golden.geese.view.AuthView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SignupPresenterTest {

    @Mock
    AuthView view;

    @Mock
    AuthRepository repo;

    @Mock
    User user;

    SignupPresenter presenter;

    @Before
    public void setUp() {
        presenter = new SignupPresenter(view, repo);
    }

    @Test
    public void signup_withNullEmail_showsError() {
        presenter.signup(null, "goldenGoose", "password123");

        verify(view, times(1)).showError("Email cannot be empty.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_withBlankEmail_showsError() {
        presenter.signup("   ", "goldenGoose", "password123");

        verify(view, times(1)).showError("Email cannot be empty.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_withNullUsername_showsError() {
        presenter.signup("test@example.com", null, "password123");

        verify(view, times(1)).showError("Username cannot be empty.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_withBlankUsername_showsError() {
        presenter.signup("test@example.com", "   ", "password123");

        verify(view, times(1)).showError("Username cannot be empty.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_withNullPassword_showsError() {
        presenter.signup("test@example.com", "goldenGoose", null);

        verify(view, times(1)).showError("Password must be at least 6 characters.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_withShortPassword_showsError() {
        presenter.signup("test@example.com", "goldenGoose", "abc12");

        verify(view, times(1)).showError("Password must be at least 6 characters.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_withValidInput_showsLoadingAndCallsRepo() {
        presenter.signup("test@example.com", "goldenGoose", "password123");

        verify(view, times(1)).showLoading();
        verify(repo, times(1)).signUp(
                eq("test@example.com"), eq("goldenGoose"), eq("password123"), any(AuthCallBack.class));
    }

    @Test
    public void signup_onSuccess_navigatesToHome() {
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.signup("test@example.com", "goldenGoose", "password123");
        verify(repo).signUp(
                eq("test@example.com"), eq("goldenGoose"), eq("password123"), captor.capture());

        captor.getValue().onSuccess(user);

        verify(view, times(1)).goToHome();
    }

    @Test
    public void signup_onError_showsErrorMessage() {
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.signup("test@example.com", "goldenGoose", "password123");
        verify(repo).signUp(
                eq("test@example.com"), eq("goldenGoose"), eq("password123"), captor.capture());

        captor.getValue().onError("Email already in use.");

        verify(view, times(1)).showError("Email already in use.");
    }
}