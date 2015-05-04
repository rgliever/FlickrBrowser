package com.example.ryan.flickrbrowser;

// Ryan Gliever -- 5/3/2015

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    EditText searchQuery;
    Button searchButton;
    TextView jsonResult;
    ListView gallery;
    public ProgressDialog progressDialog;

    // list element for the ListView
    private class ListElement {
        ListElement(){};
        public Bitmap bm;
        public String title;
    }

    // array list of list elements for ListView
    private ArrayList<ListElement> aList;

    // custom adapter
    private class ListAdapter extends ArrayAdapter<ListElement> {
        int resource;
        Context context;

        public ListAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            ListElement w = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view
            ImageView imageView = (ImageView) newView.findViewById(R.id.image);
            imageView.setImageBitmap(w.bm);
            imageView.setMinimumHeight(300);
            TextView textView = (TextView) newView.findViewById(R.id.title);
            textView.setText(w.title);

            // Set a listener for the whole list item.
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*String s = v.getTag().toString();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, s, duration);
                    toast.show();*/

                }
            });

            return newView;
        }

    }

    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchQuery = (EditText)findViewById(R.id.editText);
        searchButton = (Button)findViewById(R.id.button);
        jsonResult = (TextView)findViewById(R.id.jsonresult);
        gallery = (ListView)findViewById(R.id.listView);
        aList = new ArrayList<ListElement>();
        listAdapter = new ListAdapter(this, R.layout.imagelist, aList);
        listAdapter.notifyDataSetChanged();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("LOADING");
        progressDialog.setMessage("Getting your photos...");
        progressDialog.setCancelable(false);
        searchButton.setOnClickListener(searchButtonListener);
    }

    private Button.OnClickListener searchButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog.show();
            final String searchString = searchQuery.getText().toString();
            PostPhotoList ppl = new PostPhotoList(searchString);
            // start AsyncTask
            new QueryFlickr().execute(ppl);
            // close keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    };

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

    // class extends PhotoList and contains searchString to pass to QueryFlickr
    class PostPhotoList extends PhotoList {
        String searchString;
        PostPhotoList (String ss) {
            this.searchString = ss;
        };
        // useResult called from onPostExecute in QueryFlickr
        @Override
        public void useResult(PhotoList pl) {
            aList.clear();
            // build up list
            for (int i = 0; i < pl.photoList.length; i++) {
                ListElement listElement = new ListElement();
                listElement.bm = pl.photoList[i].bitmap;
                listElement.title = pl.photoList[i].title.toString();
                aList.add(listElement);
            }
            listAdapter = new ListAdapter(MainActivity.this, R.layout.imagelist, aList);
            gallery.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
            hideLoading();
        }
    }

    // hide the progress dialog
    public void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

}
