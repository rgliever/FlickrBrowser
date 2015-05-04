package com.example.ryan.flickrbrowser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ryan on 5/3/2015.
 */
public class QueryFlickr extends AsyncTask<MainActivity.PostPhotoList, Void, PhotoList> {

    private static final int NUM_RESULTS = 10;

    private static final String URL_PREFIX = "https://api.flickr.com/services/rest/?";
    private static final String URL_SEARCH = "method=flickr.photos.search";
    private static final String URL_PERPAGE = "&per_page=";
    private static final String URL_NOJSON = "&nojsoncallback=1";
    private static final String URL_FORMAT = "&format=json";
    private static final String URL_TAGS = "&tags=";
    private static final String URL_KEY = "&api_key=";
    private static final String API_KEY = "1073716bf6315ed00fa4da7e7b7d589e";
    private static final String API_SECRET = "7204de4d5f4702db";

    // we will build up PhotoList and return the final result
    PhotoList pl;

    private String ParseJSON (String json, int j) {
        Log.i("ParseJSON", Integer.toString(j));
        String jResult = null;

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject json_photos = jsonObject.getJSONObject("photos");
            JSONArray json_photo_array = json_photos.getJSONArray("photo");

            JSONObject FImage = json_photo_array.getJSONObject(j);

            String ID = FImage.getString("id");
            String OWNER = FImage.getString("owner");
            String SECRET = FImage.getString("secret");
            String SERVER = FImage.getString("server");
            String FARM = FImage.getString("farm");
            String TITLE = FImage.getString("title");

            jResult = "\nid: " + ID + "\n"
                    + "owner: " + OWNER + "\n"
                    + "secret: " + SECRET + "\n"
                    + "server: " + SERVER + "\n"
                    + "farm: " + FARM + "\n"
                    + "title: " + TITLE + "\n";

            // fill out photo fields besides bitmap
            pl.photoList[j] = new FlickrPhoto();
            pl.photoList[j].id = ID;
            pl.photoList[j].owner = OWNER;
            pl.photoList[j].secret = SECRET;
            pl.photoList[j].server = SERVER;
            pl.photoList[j].farm = FARM;
            pl.photoList[j].title = TITLE;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jResult;
    }

    protected PhotoList doInBackground(MainActivity.PostPhotoList... ppl) {
        pl = ppl[0];
        pl.setSize(NUM_RESULTS);

        // first get json from server
        for (int i = 0; i < NUM_RESULTS; i++) {
            String result = null;
            String queryString = URL_PREFIX + URL_SEARCH
                    + URL_PERPAGE + NUM_RESULTS
                    + URL_NOJSON + URL_FORMAT
                    + URL_TAGS + ppl[0].searchString
                    + URL_KEY + API_KEY;

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(queryString);
            try {
                HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();
                if (httpEntity != null) {
                    InputStream inputStream = httpEntity.getContent();
                    Reader in = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(in);
                    StringBuilder stringBuilder = new StringBuilder();

                    String stringReadLine = null;

                    while ((stringReadLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(stringReadLine + "\n");
                    }

                    result = stringBuilder.toString();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // parse json and set photo fields (except for bitmap)
            String parsed_json = ParseJSON(result, i);

            // set photo bitmap field
            Bitmap bm = null;
            String FlickrPhotoPath =
                    "http://farm" + pl.photoList[i].farm + ".static.flickr.com/"
                            + pl.photoList[i].server + "/" + pl.photoList[i].id + "_"
                            + pl.photoList[i].secret + "_m.jpg";
            URL PhotoURL = null;
            try {
                PhotoURL = new URL(FlickrPhotoPath);
                HttpURLConnection httpURLConnection = (HttpURLConnection) PhotoURL.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                bm = BitmapFactory.decodeStream(inputStream);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pl.photoList[i].bitmap = bm;
        }
        return pl;
    }

    protected void onPostExecute(PhotoList pl) {
        // calls extended class function in UI thread
        pl.useResult(pl);
    }

}
