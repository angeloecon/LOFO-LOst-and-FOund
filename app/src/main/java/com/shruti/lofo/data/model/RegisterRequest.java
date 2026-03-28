package com.shruti.lofo.data.model;

public class RegisterRequest {

    private String email;
    private String password;
    private String full_name;
    private String phone;

    public RegisterRequest(String email, String password, String full_name, String phone){
        this.email = email;
        this.password = password;
        this.full_name = full_name;
        this.phone = phone;
    }

}
