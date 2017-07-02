package com.wkyn.dzudi.kbuildv1;

/**
 * Created by Dzudi on 6/9/2017.
 */

public class Place {
    private String name;
    private String description;
    private String longitude;
    private String latitude;
    private String type;
    private String imageUrl;
    private String myId;

    public Place(String nme) {
        //this(nme, "");
        this.name = nme;
        description="";
        longitude="";
        latitude="";
        type="";
        imageUrl="";
        myId="";
    }
    public Place(){
        name="";
        description="";
        longitude="";
        latitude="";
        type="";
        imageUrl="";
        myId="";
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public void setName(String nme){
        this.name=nme;
    }
    public  void setDescription(String desc){
        this.description = desc;
    }
    public void setType(String typ){this.type=typ;}
    public String getType(){return  type;}
    public void setImageUrl(String imgUrl){this.imageUrl = imgUrl;}
    public String getImageUrl(){return imageUrl;}

    public String getLongitude(){
        return longitude;
    }
    public String getLatitude(){
        return latitude;
    }
    public void setLongitude(String longitude){
        this.longitude = longitude;
    }
    public void setLatitude(String latitude){
        this.latitude = latitude;
    }
    public String getMyId(){
        return myId;
    }
    public void setMyId(String myId){
        this.myId = myId;
    }

    @Override
    public String toString(){
        return this.name + "    ["+"type: "+ this.type +"]";
    }
}
