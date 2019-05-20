package com.example.podcastapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.webkit.WebView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private MainActivityNew main;
    //private MainActivityNew mainNew;

    // OPMERKING: indien terug MainActivity nodig "MainActivityNew" vervangen door "MainActivity"
    // + elders vervangen indien zichtbare fouten
    public SubscriptionAdder(MainActivityNew mainActivityNew) {
        this.main = mainActivityNew;
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
            getAndStoreImage();

            return "succes";
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(title + "\n" + description + "\n" + feedUrl + "\n" + imgUrl);
            main.updateViewWithPodcast(new Podcast(title, description, feedUrl));
            //updateDbAndStorage();
            showImage();
        }
    }

    public void parseXml(String urlString)throws XmlPullParserException, IOException {
        InputStream stream = null;
        try {
            stream = downloadUrl(urlString);
            XmlPullParser parser = setupParser(stream);
            readFeed2(parser);
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

    public void readFeed2(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();
        //parser.nextTag();
        boolean itemTag = false;
        int eventType = parser.getEventType();

        // old whilecheck: parser.next() != XmlPullParser.END_TAG
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
    // podcast afbeelding downloaden en opslaan
    public void getAndStoreImage(){
        FileOutputStream outStream = null;
        try {
            InputStream inStream = (InputStream) new URL(imgUrl).getContent();
            Bitmap d = BitmapFactory.decodeStream(inStream);
            inStream.close();
            String imgNaam = title + ".png";
            File file = new File(main.getFilesDir(), imgNaam);
            System.out.println("De app file dir is: " + main.getFilesDir().toString());
            outStream = new FileOutputStream(file);
            d.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showImage(){
        String name = title + ".png";
        FileInputStream fileInputStream;
        Bitmap bitmap = null;
        try{
            fileInputStream = main.openFileInput(name);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
            main.setImage(bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public String toString() {
        return String.format("%s: %s%n%s: %s%n%s: %s%n%s: %s%n",
                "title", title,
                "description", description,
                "feed", feedUrl,
                "image", imgUrl);
    }
}
