package com.parse.starter.Utils;

public class UserContacts {

    private String name;
    private String phone;

    public UserContacts(){}

    public UserContacts(String name, String phone){
        this.name = name;
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
