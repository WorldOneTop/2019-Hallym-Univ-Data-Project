package com.example.myapplication;

import android.graphics.drawable.Drawable;

public class MyItem {//MyAdapter에서 쓰이는 객체들 정의하는 클래스

    private Drawable icon;
    private String name;
    private String contents;
    private int isRead;
    private String userid;
    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContents() {
        return contents;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setuserid(String userid) {
        this.userid = userid;
    }

    public String getuserid() {
        return userid;
    }


}