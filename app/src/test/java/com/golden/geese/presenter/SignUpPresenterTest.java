package com.golden.geese.presenter;

import com.golden.geese.User;
import com.golden.geese.model.AuthCallBack;
import com.golden.geese.model.AuthRepository;
import com.golden.geese.view.AuthView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SignUpPresenterTest {

    @Mock
    AuthView view;

    @Mock
    AuthRepository repo;

    @Mock
    User user;

    SignUpPresenter presenter;

    @Before
    public void setUp() {
        presenter = SignUpPresenter.getSignUpPresenter();
        presenter.setView(view);
        presenter.setRepo(repo);
    }

    @After
    public void tearDown() {
        presenter.completeProcess();
    }

    @Test
    public void validateName_withNullUsername_showsError() {
        presenter.validateName(null);

        verify(view, times(1)).showError("Username cannot be empty.");
        verify(view, never()).nextStep();
    }

    @Test
    public void validateName_withBlankUsername_showsError() {
        presenter.validateName("   ");

        verify(view, times(1)).showError("Username cannot be empty.");
        verify(view, never()).nextStep();
    }

    @Test
    public void validateName_withValidUsername_storesNameAndAdvances() {
        presenter.validateName("goldenGoose");

        assertEquals("goldenGoose", presenter.getUsername());
        verify(view, times(1)).nextStep();
        verify(view, never()).showError(anyString());
    }

    @Test
    public void validateName_afterViewCleared_isNoOp() {
        presenter.onDestroy(view);

        presenter.validateName("goldenGoose");

        verifyNoMoreInteractions(view);
    }

    @Test
    public void validateEmail_withNullEmail_showsError() {
        presenter.validateEmail(null);

        verify(view, times(1)).showError("Email cannot be empty.");
        verify(view, never()).nextStep();
    }

    @Test
    public void validateEmail_withBlankEmail_showsError() {
        presenter.validateEmail("   ");

        verify(view, times(1)).showError("Email cannot be empty.");
        verify(view, never()).nextStep();
    }

    @Test
    public void validateEmail_withValidEmail_storesEmailAndAdvances() {
        presenter.validateEmail("test@example.com");

        assertEquals("test@example.com", presenter.getEmail());
        verify(view, times(1)).nextStep();
        verify(view, never()).showError(anyString());
    }

    @Test
    public void validateEmail_afterViewCleared_isNoOp() {
        presenter.onDestroy(view);

        presenter.validateEmail("test@example.com");

        verifyNoMoreInteractions(view);
    }

    @Test
    public void validatePassword_withNullPassword_showsError() {
        presenter.validatePassword(null, null);

        verify(view, times(1)).showError("Password must be at least 6 characters.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void validatePassword_withShortPassword_showsError() {
        presenter.validatePassword("abc12", "abc12");

        verify(view, times(1)).showError("Password must be at least 6 characters.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void validatePassword_withMismatchedConfirmation_showsError() {
        presenter.validatePassword("password123", "doesNotMatch");

        verify(view, times(1)).showError("Passwords do not match.");
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void validatePassword_withValidPassword_storesPasswordAndTriggersSignup() {
        presenter.setUsername("goldenGoose");
        presenter.setEmail("test@example.com");

        presenter.validatePassword("password123", "password123");

        assertEquals("password123", presenter.getPassword());
        verify(view, times(1)).showLoading();
        verify(repo, times(1)).signUp(
                eq("test@example.com"), eq("goldenGoose"), eq("password123"), any(AuthCallBack.class));
    }

    @Test
    public void validatePassword_afterViewCleared_isNoOp() {
        presenter.onDestroy(view);

        presenter.validatePassword("password123", "password123");

        verifyNoMoreInteractions(view);
        verify(repo, never()).signUp(anyString(), anyString(), anyString(), any(AuthCallBack.class));
    }

    @Test
    public void signup_onSuccess_advancesAndCompletesProcess() {
        presenter.setUsername("goldenGoose");
        presenter.setEmail("test@example.com");
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.validatePassword("password123", "password123");
        verify(repo).signUp(
                eq("test@example.com"), eq("goldenGoose"), eq("password123"), captor.capture());

        captor.getValue().onSuccess(user);

        verify(view, times(1)).nextStep();

        SignUpPresenter afterCompletion = SignUpPresenter.getSignUpPresenter();
        assertNotSame(presenter, afterCompletion);
    }

    @Test
    public void signup_onError_showsErrorMessage() {
        presenter.setUsername("goldenGoose");
        presenter.setEmail("test@example.com");
        ArgumentCaptor<AuthCallBack> captor = ArgumentCaptor.forClass(AuthCallBack.class);

        presenter.validatePassword("password123", "password123");
        verify(repo).signUp(
                eq("test@example.com"), eq("goldenGoose"), eq("password123"), captor.capture());

        captor.getValue().onError("Email already in use.");

        verify(view, times(1)).showError("Email already in use.");
        verify(view, never()).nextStep();
    }

    @Test
    public void onDestroy_withMatchingCaller_clearsView() {
        presenter.onDestroy(view);

        presenter.validateName("goldenGoose");

        verifyNoMoreInteractions(view);
    }

    @Test
    public void onDestroy_withNonMatchingCaller_keepsView() {
        AuthView otherView = mock(AuthView.class);

        presenter.onDestroy(otherView);
        presenter.validateName("goldenGoose");

        verify(view, times(1)).nextStep();
    }
}