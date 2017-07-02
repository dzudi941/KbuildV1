package com.wkyn.dzudi.kbuildv1;

import java.util.ArrayList;

/**
 * Created by Dzudi on 6/30/2017.
 */

public class FriendsModel {
    String email;
    private ArrayList<String> friends;
    public FriendsModel(String eml){
        this.email = eml;
        friends=new ArrayList<String>();
    }
    public FriendsModel(){
        this.email = "";
        friends=new ArrayList<String>();
    }

    public boolean addFriend(String email){
        if(!friends.contains(email)){
            friends.add(email);
            return true;
        } else{
            return false;
        }

    }
    public void updateFriendsList(ArrayList<String> currentState){
        friends = currentState;
    }
    public ArrayList<String> getFriends(){
        return friends;
    }
}
