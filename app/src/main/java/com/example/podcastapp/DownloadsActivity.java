package com.example.podcastapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadsActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private ListView dwnList;
    private List<String> dirContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        dwnList = (ListView) findViewById(R.id.dwnListView);
        dirContent = new ArrayList<>();

        getFolderContent();
        Log.i("DIR: ", dirContent.toString());

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dirContent);
        dwnList.setAdapter(adapter);
        dwnList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "item " + position + " geklikt!", Toast.LENGTH_SHORT).show();
                Intent epPlayer = new Intent(getApplicationContext(), EpisodePlayer.class);
                epPlayer.putExtra("epFileName", dirContent.get(position));
                startActivity(epPlayer);
            }
        });
    }

    private void getFolderContent() {
        File directory =new File(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PODCASTS)));

        File[] files = directory.listFiles();
        for(File file : files){
            dirContent.add(file.getName());
        }
    }
}
