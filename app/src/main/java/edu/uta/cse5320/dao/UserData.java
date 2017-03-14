package edu.uta.cse5320.dao;

/**
 * Created by Akshay on 3/1/2017.
 */

public class UserData {


    private String fullName;
    private String age;
    private String phoneNumber;
    private String email;

    public UserData(){
    }

    public UserData(String fullName, String age, String phoneNumber, String email) {
        this.fullName = fullName;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
