package com.golden.geese;

import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.presenter.LoginPresenter;
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
public class LoginPresenterTest {

    @Mock
    AuthView view;

    @Mock
    AuthRepository repo;

    @Mock
    User user;

    LoginPresenter presenter;

    @Before
    public void setUp() {
        presenter = new LoginPresenter(view, repo);
    }

    @Test
    public void login_withNullEmail_showsError() {
        presenter.login(null, "password123");

        verify(view, times(1)).showError("Email cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withBlankEmail_showsError() {
        presenter.login("   ", "password123");

        verify(view, times(1)).showError("Email cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withNullPassword_showsError() {
        presenter.login("test@example.com", null);

        verify(view, times(1)).showError("Password cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withEmptyPassword_showsError() {
        presenter.login("test@example.com", "");

        verify(view, times(1)).showError("Password cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withValidCredentials_showsLoadingAndCallsRepo() {
        presenter.login("test@example.com", "password123");

        verify(view, times(1)).showLoading();
        verify(repo, times(1)).signIn(eq("test@example.com"), eq("password123"), any(AuthCallBack.class));
    }

    @Test
    public void login_onSuccess_navigatesToHome() {
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.login("test@example.com", "password123");
        verify(repo).signIn(eq("test@example.com"), eq("password123"), captor.capture());

        captor.getValue().onSuccess(user);

        verify(view, times(1)).goToHome();
    }

    @Test
    public void login_onError_showsErrorMessage() {
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.login("test@example.com", "password123");
        verify(repo).signIn(eq("test@example.com"), eq("password123"), captor.capture());

        captor.getValue().onError("Invalid credentials.");

        verify(view, times(1)).showError("Invalid credentials.");
    }
}