package com.example.ryan.flickrbrowser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    EditText searchQuery;
    Button searchButton;
    TextView jsonResult;
    ImageView imageView;
    String searchResult = null;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchQuery = (EditText)findViewById(R.id.editText);
        searchButton = (Button)findViewById(R.id.button);
        jsonResult = (TextView)findViewById(R.id.jsonresult);
        imageView = (ImageView)findViewById(R.id.image);
        searchButton.setOnClickListener(searchButtonListener);
    }

    private Button.OnClickListener searchButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String searchString = searchQuery.getText().toString();
            new QueryFlickr().execute(searchString);
        }
    };

    private String ParseJSON (String json) {
        String jResult = null;
        bitmap = null;

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject json_photos = jsonObject.getJSONObject("photos");
            JSONArray json_photo_array = json_photos.getJSONArray("photo");

            JSONObject FlickrPhoto = json_photo_array.getJSONObject(0);

            String ID = FlickrPhoto.getString("id");
            String OWNER = FlickrPhoto.getString("owner");
            String SECRET = FlickrPhoto.getString("secret");
            String SERVER = FlickrPhoto.getString("server");
            String FARM = FlickrPhoto.getString("farm");
            String TITLE = FlickrPhoto.getString("title");

            jResult = "\nid: " + ID + "\n"
                    + "owner: " + OWNER + "\n"
                    + "secret: " + SECRET + "\n"
                    + "server: " + SERVER + "\n"
                    + "farm: " + FARM + "\n"
                    + "title: " + TITLE + "\n";

            String loadPhotoParams[] = {ID, OWNER, SECRET, SERVER, FARM, TITLE};
            new LoadPhoto().execute(loadPhotoParams);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jResult;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadPhoto extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... params) {
            String id = params[0];
            String owner = params[1];
            String secret = params[2];
            String server = params[3];
            String farm = params[4];
            String title = params[5];

            Bitmap bm = null;
            String FlickrPhotoPath =
                    "http://farm" + farm + ".static.flickr.com/"
                    + server + "/" + id + "_" + secret + "_m.jpg";

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

            return bm;
        }

        protected void onPostExecute(Bitmap bm) {
            imageView.setImageBitmap(bm);
        }
    }

    private class QueryFlickr extends AsyncTask<String, Void, String> {

        private static final String URL_PREFIX = "https://api.flickr.com/services/rest/?";
        private static final String URL_SEARCH = "method=flickr.photos.search";
        private static final String URL_PERPAGE = "&per_page=1";
        private static final String URL_NOJSON = "&nojsoncallback=1";
        private static final String URL_FORMAT = "&format=json";
        private static final String URL_TAGS = "&tags=";
        private static final String URL_KEY = "&api_key=";
        private static final String API_KEY = "1073716bf6315ed00fa4da7e7b7d589e";
        private static final String API_SECRET = "7204de4d5f4702db";
        private Exception exception;

        protected String doInBackground(String... searchString) {
            String result = null;
            String queryString = URL_PREFIX + URL_SEARCH
                    + URL_PERPAGE + URL_NOJSON
                    + URL_FORMAT
                    + URL_TAGS + searchString[0]
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

                    while ((stringReadLine = bufferedReader.readLine()) != null){
                        stringBuilder.append(stringReadLine + "\n");
                    }

                    result = stringBuilder.toString();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            String parsed = ParseJSON(result);
            jsonResult.setText(parsed);
        }
    }
}
