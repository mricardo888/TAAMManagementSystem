package com.golden.geese.presenter;

import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.LoginView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock
    LoginView view;

    @Mock
    AuthRepository repo;

    @Mock
    User user;

    LoginPresenter presenter;

    @Before
    public void setUp() {
        presenter = new LoginPresenter();
        presenter.setView(view);
        presenter.setRepo(repo);
    }

    @Test
    public void login_withNullEmail_showsEmailError() {
        presenter.login(null, "password123");

        verify(view, times(1)).showEmailError("Email cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withBlankEmail_showsEmailError() {
        presenter.login("   ", "password123");

        verify(view, times(1)).showEmailError("Email cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withNullPassword_showsPasswordError() {
        presenter.login("test@example.com", null);

        verify(view, times(1)).showPasswordError("Password cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withEmptyPassword_showsPasswordError() {
        presenter.login("test@example.com", "");

        verify(view, times(1)).showPasswordError("Password cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withWhitespacePassword_showsPasswordError() {
        presenter.login("test@example.com", "   ");

        verify(view, times(1)).showPasswordError("Password cannot be empty.");
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void login_withValidCredentials_callsRepo() {
        presenter.login("test@example.com", "password123");

        verify(repo, times(1)).signIn(eq("test@example.com"), eq("password123"), any(AuthCallBack.class));
        verify(view, never()).showEmailError(anyString());
        verify(view, never()).showPasswordError(anyString());
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
    public void login_onError_showsPasswordError() {
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.login("test@example.com", "password123");
        verify(repo).signIn(eq("test@example.com"), eq("password123"), captor.capture());

        captor.getValue().onError("Invalid credentials.");

        verify(view, times(1)).showPasswordError("Invalid credentials.");
    }

    @Test
    public void login_afterViewCleared_isNoOp() {
        presenter.onDestroy(view);

        presenter.login("test@example.com", "password123");

        verifyNoMoreInteractions(view);
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void onDestroy_withMatchingCaller_clearsViewAndRepo() {
        presenter.onDestroy(view);

        presenter.login("test@example.com", "password123");

        verifyNoMoreInteractions(view);
        verify(repo, never()).signIn(anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void onDestroy_withNonMatchingCaller_keepsViewAndRepo() {
        LoginView otherView = mock(LoginView.class);

        presenter.onDestroy(otherView);
        presenter.login("test@example.com", "password123");

        verify(repo, times(1)).signIn(eq("test@example.com"), eq("password123"), any(AuthCallBack.class));
    }
}