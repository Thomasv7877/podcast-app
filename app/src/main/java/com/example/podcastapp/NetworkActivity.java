package com.example.podcastapp;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NetworkActivity extends AppCompatActivity implements EpisodeAdapter.ItemClickListener {

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    private static String URL = "https://rss.art19.com/doughboys";
    //private static final String URL = "http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";
    //private static final String URL = "https://rss.art19.com/hollywood-handbook";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = true;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;
    public static String sPref = "Any";

    private List<String> epNames, epLinks;
    EpisodeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String temp = this.getIntent().getStringExtra("feed");
        if (temp != null) URL = temp;
        epNames = new ArrayList<>();
        epLinks = new ArrayList<>();
        //setContentView(R.layout.activity_network);
        loadPage();
    }

    // Uses AsyncTask to download the XML feed from stackoverflow.com.
    public void loadPage() {

        if ((sPref.equals(ANY)) && (wifiConnected || mobileConnected)) {
            new DownloadXmlTask().execute(URL);
        } else if ((sPref.equals(WIFI)) && (wifiConnected)) {
            new DownloadXmlTask().execute(URL);
        } else {
            // show error
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String epNaam = adapter.getItem(position);
        String epLink = epLinks.get(position);
        Toast.makeText(this, "Downloading " + epNaam + " on row number " + position, Toast.LENGTH_SHORT).show();

        long downloadID;
        epNaam = epNaam.replaceAll("\\.|\\,", "").replaceAll(" ", "_");
        File file=new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PODCASTS),epNaam + ".mp3");

        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(epLink))
                .setTitle(epNaam)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file));// Uri of the destination file

        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setContentView(R.layout.activity_network);
            setupRecyclerView();
            setTitleAndDescription();
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EpisodeAdapter(this, epNames);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        PodXmlParser podXmlParser = new PodXmlParser();
        List<PodXmlParser.Entry> entries = null;

        try {
            stream = downloadUrl(urlString);
            entries = podXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        for (PodXmlParser.Entry entry : entries) {
            epNames.add(entry.title);
            epLinks.add(entry.link);
        }
        return "success";
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        java.net.URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
    private void setTitleAndDescription(){
        String title = this.getIntent().getStringExtra("title");
        TextView txtTitle = (TextView) findViewById(R.id.podOmschrijving);
        txtTitle.setText(title);
        String description = this.getIntent().getStringExtra("description");
        description = description.replaceAll("<p>|</p>", "");
        TextView txtDescription = (TextView) findViewById(R.id.podNaam);
        txtDescription.setText(description);
    }
}
