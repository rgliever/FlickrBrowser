package com.example.ryan.flickrbrowser;

import java.util.ArrayList;

/**
 * Created by Ryan on 5/3/2015.
 */
public class PhotoList {

    public FlickrPhoto[] photoList;
    PhotoList (){}
    public void setSize(int size) {
        photoList = new FlickrPhoto[size];
    }
    public void useResult(PhotoList pl){}

}
