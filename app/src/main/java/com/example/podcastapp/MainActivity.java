package com.example.podcastapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.room.Room;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> podList;
    ArrayAdapter<String> adapter;
    GridView podGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        podList = new ArrayList<>();
        Podcast podDough = new Podcast("Doughboys", "Its good");
        podList.add(podDough.getTitle());

        Podcast podHolly = new Podcast("Comedy Bang Bang", "Its good");
        AppDatabase db = makeDatabase();
        db.podcastDao().insertAll(podHolly);
        String title = db.podcastDao().findByName("Comedy Bang Bang").getTitle();
        podList.add(title);

        podGrid = (GridView) findViewById(R.id.subscriptionGrid);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, podList);
        podGrid.setAdapter(adapter);
    }

    public void readFeed(View view){
        Intent feedIntent = new Intent(this, NetworkActivity.class);
        startActivity(feedIntent);
    }
    public AppDatabase makeDatabase(){
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").allowMainThreadQueries().build();
        return db;
    }
    public void AddSubClicked(View view){
        EditText feedEditText = (EditText) findViewById(R.id.txtFeed);
        SubscriptionAdder adder = new SubscriptionAdder(this);
        adder.addSub(feedEditText.getText().toString());
    }
    private void updateGrid(){

    }
    public void updateViewWithPodcast(Podcast pod) {
        adapter.add(pod.getTitle());
    }
    public void setImage(Bitmap bitmap){
        ImageView imageView = (ImageView) findViewById(R.id.podImage);
        imageView.setImageBitmap(bitmap);
    }
}