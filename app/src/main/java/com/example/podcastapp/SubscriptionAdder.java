package com.example.podcastapp;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.webkit.WebView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class SubscriptionAdder {

    public static String ns = null;
    private String feedUrl;
    private String title;
    private String description;
    private String imgUrl;
    private MainActivity main;

    public SubscriptionAdder(MainActivity mainActivity) {
        this.main = mainActivity;
    }

    public void addSub(String URL){
        this.feedUrl = URL;
        new addSubIBackground().execute(URL);
    }

    private class addSubIBackground extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                parseXml(urls[0]);
            } catch (IOException e) {
                //return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                //return getResources().getString(R.string.xml_error);
            }
            return "succes";
        }

        @Override
        protected void onPostExecute(String result) {
            //setContentView(R.layout.activity_network);
            // Displays the HTML string in the UI via a WebView
            //WebView myWebView = (WebView) findViewById(R.id.webview);
            //myWebView.loadData(result, "text/html", null);
            //ListView podLijst = (ListView) findViewById(R.id.podLijst);
            //ArrayAdapter adapter = new ArrayAdapter<PodXmlParser.Entry>(this, R.layout.activity_network, entries);
            //podLijst.setAdapter(adapter);
            System.out.println("Volledige output: " + title + " " + description + " " + imgUrl);
            main.updateViewWithPodcast(new Podcast(title, description));
            updateDbAndStorage();
        }
    }

    public void parseXml(String urlString)throws XmlPullParserException, IOException {
        InputStream stream = null;
        try {
            stream = downloadUrl(urlString);
            XmlPullParser parser = setupParser(stream);
            readFeed2(parser);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
    public XmlPullParser setupParser(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return parser;
        } finally {
            in.close();
        }
    }

    /*public void readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "channel");

        // oldwilecheck: parser.next() != XmlPullParser.END_TAG
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            System.out.println(name);
            // Starts by looking for the entry tag
            if (name.equals("title")) {
                this.title = readTitle(parser);
            } else if (name.equals("description")) {
                this.description = readSummary(parser);
            } else if (name.equals("itunes:image")) {
                //System.out.println("image gevonden!!");
                //parser.nextTag();
                this.imgUrl = readUrl(parser);
            }else {
                skip(parser);
            }

        }
    }*/

    public void readFeed2(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();
        //parser.nextTag();
        boolean itemTag = false;
        int eventType = parser.getEventType();

        // oldwilecheck: parser.next() != XmlPullParser.END_TAG
        while (!itemTag) {
            String tagname = parser.getName();
            if (eventType == XmlPullParser.START_TAG){
                switch (tagname.toLowerCase()){
                    case "title":
                        this.title = readTitle(parser);
                        Log.i("Tag gevonden: ", tagname); break;
                    case "description":
                        this.description = readSummary(parser);
                        Log.i("Tag gevonden: ", tagname); break;
                    case "itunes:image":
                        this.imgUrl = readUrl(parser);
                        Log.i("Tag gevonden: ", tagname); break;
                    case "item":
                        itemTag = true;
                        Log.i("Tag gevonden: ", tagname); break;
                    default: break;
                }
            }
            eventType = parser.next();
        }
    }

    public void updateDbAndStorage(){

    }
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
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }


    private String readUrl(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "itunes:image");
        String url = parser.getAttributeValue(ns, "href");
        //System.out.println("Attrubuut naam: " + parser.getAttributeName(0) + ", waarde: " + url);
        //parser.require(XmlPullParser.END_TAG, ns, "itunes:image");
        return url;
    }

    // Processes summary tags in the feed.
    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return summary;
    }

    // TODO: readTag mathode waar je tagnaam kan meegeven

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
