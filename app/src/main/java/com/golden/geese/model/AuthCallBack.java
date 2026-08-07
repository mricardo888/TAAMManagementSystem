package com.golden.geese.model;

import com.golden.geese.User;

/** Completion callback for AuthRepository operations that resolve to a signed-in User */
public interface AuthCallBack {
    /** Success call once authentication succeeds */
    void onSuccess(User user);

    /** Error message if authentication fails */
    void onError(String msg);
}
