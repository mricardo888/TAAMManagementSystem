package com.golden.geese.ui;

import androidx.lifecycle.ViewModel;
public class SignUpViewModel extends ViewModel {
    private String email = "";
    private String name = "";
    private String password = "";


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName (String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public String getEmail () {
        return email;
    }
}
