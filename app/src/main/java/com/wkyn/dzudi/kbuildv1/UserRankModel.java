package com.wkyn.dzudi.kbuildv1;

/**
 * Created by Dzudi on 6/24/2017.
 */

public class UserRankModel {
    private String email;
    private String name;
    private long rank;

    public UserRankModel(String email, String name){
        this.email = email;
        this.name = name;
        this.rank=0;
    }
    public UserRankModel(){
        this.email="";
        this.name="";
        this.rank=0;
    }
    public void incrementRank(){
        rank++;
    }
    public long getRank(){
        return rank;
    }
    public String getEmail(){
        return email;
    }
    public String getName(){
        return name;
    }
    @Override
    public String toString(){
        return  name + " (" + email + ") [added objects: " + rank+"]";
    }
}
