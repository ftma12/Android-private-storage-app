package com.example.privatefilestorageapp;

public class ListItemModel {
    private String title;
    private int iconResId;

    public ListItemModel(String title, int iconResId){
     this.title=title;
     this.iconResId=iconResId;
    }

    public String getTitle(){
     return title;
    }
    public int getIconResId(){
return iconResId;
    }
}
