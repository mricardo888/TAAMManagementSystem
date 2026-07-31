package com.golden.geese.model;

import com.golden.geese.User;

public interface AuthCallBack {
    void onSuccess(User user);
    void onError(String msg);
}
