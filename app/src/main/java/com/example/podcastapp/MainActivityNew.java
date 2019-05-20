package com.example.podcastapp;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivityNew extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ArrayList<Bitmap> podList;
    GridView podGrid;
    AppDatabase db;
    ImageAdapter adapter;
    private static int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // bij sdk versies > 23 moet bovenop de manifest permissie voor write external storage ook manieel goedkeuring gegeven worden
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        setContentView(R.layout.activity_main_new);
        // drawer setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // operaties specifiek voor podcast-app
        podList = new ArrayList<>();
        db = makeDatabase();

        podGrid = (GridView) findViewById(R.id.gridViewNew);
        podGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                System.out.println("Pos = " + ++position);
                Podcast pod = db.podcastDao().get(position);
                openFeed(pod.getFeed(), pod.getTitle(), pod.getDescription());
                Log.i("PODCAST GEKLIKT: ", pod == null ? "niet gevonden" : pod.getTitle());
            }
        });
        adapter = new ImageAdapter(this, podList);
        podGrid.setAdapter(adapter);
        fillGridWithImages();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                showFeedDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_new, menu);
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
    // afhandelen van acties in het "drawer" menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            // naar de activity voor het downloadoverzicht gaan
            Toast.makeText(this, "navigeren naar downloads!", Toast.LENGTH_SHORT).show();
            Intent dwnOverzicht = new Intent(this, DownloadsActivity.class);
            startActivity(dwnOverzicht);
        }else if (id == R.id.nav_share) {
            // TODO: about pagina
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openFeed(String feedUrl, String title, String description){
        Intent feedIntent = new Intent(this, NetworkActivity.class);
        feedIntent.putExtra("feed", feedUrl);
        feedIntent.putExtra("title", title);
        feedIntent.putExtra("description", description);
        Log.i("TITEL: ", title);
        Log.i("DESCRIPTION: ", description);
        startActivity(feedIntent);
    }

    public AppDatabase makeDatabase(){
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").allowMainThreadQueries().build();
        return db;
    }

    public void updateViewWithPodcast(Podcast pod) {
        //adapter.add(pod.getTitle());
        db.podcastDao().insertAll(pod);
    }

    public void setImage(Bitmap bitmap){
        adapter.add(bitmap);
    }
    // De afbeeldingen ophalen die zicht in de interne app storage bevinden om het home scherm te populeren
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
    // maakt en toont de dialog om een podcast toe te voegen adhv de url
    private void showFeedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter feed").setView(R.layout.activity_feed_dialog)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Klasse SubscriptionAdder de xml laten parsen van de url om podcast effectief toe te voegen
                // OPMERKING: we hebben de view nodig uit de dialog, niet uit de parent, hiervoor 2 volgende lijnen
                Dialog actualDialog = (Dialog) dialog;
                EditText feedEditText = (EditText) actualDialog.findViewById(R.id.enterFeed);
                SubscriptionAdder adder = new SubscriptionAdder(MainActivityNew.this);
                adder.addSub(feedEditText.getText().toString());
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
