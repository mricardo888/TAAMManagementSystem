package com.golden.geese.model;

public interface AuthCallBack {
    void onSuccess(User user);
    void onError(String msg);
}
