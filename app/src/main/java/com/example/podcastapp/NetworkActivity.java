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

    //private List<PodXmlParser.Entry> entries = null;
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    //private static final String URL = temp == null? "https://rss.art19.com/doughboys" : temp;
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

    /*private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String[] epLinks, epNames;*/
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
        //new DownloadEpisodeTask().execute(link);



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
            //setupRecyclerView();
            setContentView(R.layout.activity_network);
            // Displays the HTML string in the UI via a WebView
            //WebView myWebView = (WebView) findViewById(R.id.webview);
            //myWebView.loadData(result, "text/html", null);
            //ListView podLijst = (ListView) findViewById(R.id.podLijst);
            //ArrayAdapter adapter = new ArrayAdapter<PodXmlParser.Entry>(this, R.layout.activity_network, entries);
            //podLijst.setAdapter(adapter);
            setupRecyclerView();
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
        String title = null;
        String url = null;
        String summary = null;
        Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

        // Checks whether the user set the preference to include summary text
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref = sharedPrefs.getBoolean("summaryPref", false);

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
                formatter.format(rightNow.getTime()) + "</em>");

        try {
            stream = downloadUrl(urlString);
            entries = podXmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
        // Each Entry object represents a single post in the XML feed.
        // This section processes the entries list to combine each entry with HTML markup.
        // Each entry is displayed in the UI as a link that optionally includes
        // a text summary.

        for (PodXmlParser.Entry entry : entries) {
            htmlString.append("<p><a href='");
            htmlString.append(entry.link);
            htmlString.append("'>" + entry.title + "</a></p>");
            // If the user set the preference to include summary text,
            // adds it to the display.
            if (pref) {
                htmlString.append(entry.summary);
            }
            epNames.add(entry.title);
            epLinks.add(entry.link);
        }
        /*
        epNames = new String[entries.size()];
        epLinks = new String[entries.size()];
        for(int i = 0; i < entries.size(); i++){
            epNames[i] = entries.get(i).title;
            epLinks[i] = entries.get(i).link;
        }*/

        //htmlString.append("<h3>" + entries.isEmpty() + "</h3>");
        return htmlString.toString();
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

}
