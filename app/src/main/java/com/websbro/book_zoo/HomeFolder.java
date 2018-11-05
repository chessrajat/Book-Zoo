package com.websbro.book_zoo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class HomeFolder {
    private Drawable image=null;
    private String name;
    private String path;
    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Drawable getDrawable() {
        return image;
    }

    public void setDrawable(Drawable drawable) {
        this.image = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
