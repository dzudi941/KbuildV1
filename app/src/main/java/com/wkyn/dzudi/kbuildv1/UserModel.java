package com.wkyn.dzudi.kbuildv1;

/**
 * Created by Dzudi on 7/1/2017.
 */

public class UserModel {
    String email;
    String name;
    public UserModel(String email, String name){
        this.email=email;
        this.name=name;
    }
    public  UserModel(){
        this.email="";
        this.name="";
    }
    public String getEmail(){
        return email;
    }
    public String getName(){
        return name;
    }
}
