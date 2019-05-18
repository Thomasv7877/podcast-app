package com.example.podcastapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity {

    ArrayList<Bitmap> podList;
    ArrayAdapter<Bitmap> adapterOld;
    //RecyclerView.Adapter rAdapter;
    GridView podGrid;
    AppDatabase db;
    RecyclerView.LayoutManager layoutManger;
    ImageAdapter adapter;
    // url pod: https://rss.art19.com/hollywood-handbook

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        podList = new ArrayList<>();
        //Podcast podDough = new Podcast("Doughboys", "Its good");
        //podList.add(podDough.getTitle());

        //Podcast podHolly = new Podcast("Comedy Bang Bang", "Its good");
        db = makeDatabase();
        //db.podcastDao().insertAll(podHolly);
        //String title = db.podcastDao().findByName("Comedy Bang Bang").getTitle();
        //podList.add(title);

        podGrid = (GridView) findViewById(R.id.subscriptionGrid);
        podGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                System.out.println("Pos = " + ++position);
                Podcast pod = db.podcastDao().get(position);
                openFeed(pod.getFeed());
                Log.i("PODCAST GEKLIKT: ", pod == null ? "niet gevonden" : pod.getTitle());
                /*List<Podcast> temp = db.podcastDao().getAll();
                String string = "";
                for (Podcast pd : temp){
                    string += pd.toString();
                }
                System.out.print(string);*/
            }
        });
        //podGrid.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        //adapter = new ArrayAdapter<ImageView>(this, android.R.layout.simple_list_item_1, podList);
        adapter = new ImageAdapter(this, podList);
        adapterOld = new ArrayAdapter<Bitmap>(this, android.R.layout.simple_list_item_1, podList);
        podGrid.setAdapter(adapter);

        fillGridWithImages();
    }

    public void readFeed(View view){
        Intent feedIntent = new Intent(this, NetworkActivity.class);
        startActivity(feedIntent);
    }

    public void openFeed(String feedUrl){
        Intent feedIntent = new Intent(this, NetworkActivity.class);
        feedIntent.putExtra("feed", feedUrl);
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
        //adapter.add(pod.getTitle());
        db.podcastDao().insertAll(pod);
    }
    public void setImage(Bitmap bitmap){
        //ImageView imageView = (ImageView) findViewById(R.id.podImage);
        //imageView.setImageBitmap(bitmap);
        //ImageView imageViewInLijst = new ImageView(this);
        //imageViewInLijst.setImageBitmap(bitmap);
        //podList.add(imageViewInLijst);
        //adapter.add(bitmap);
        //podGrid.setAdapter(adapter);
        //adapterOld.add(bitmap);
        adapter.add(bitmap);
    }

    public void fillGridWithImages(){
        List<Podcast> lijst = db.podcastDao().getAll();
        String name;
        FileInputStream fileInputStream;
        Bitmap bitmap;
        try{
            for (Podcast pod : lijst){
                name = pod.getTitle() + ".png";
                fileInputStream = this.openFileInput(name);
                bitmap = BitmapFactory.decodeStream(fileInputStream);
                fileInputStream.close();
                this.setImage(bitmap);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}