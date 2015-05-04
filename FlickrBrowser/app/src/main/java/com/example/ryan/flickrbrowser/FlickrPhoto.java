package com.example.ryan.flickrbrowser;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Ryan on 5/3/2015.
 */
// class which contains json response fields for photos and a bitmap of the photo
public class FlickrPhoto {

    FlickrPhoto() {};

    public void useResult(FlickrPhoto fp) {}

    String id;
    String owner;
    String secret;
    String server;
    String farm;
    String title;
    Bitmap bitmap;

}
