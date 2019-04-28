package com.example.podcastapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Podcast.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PodcastDao podcastDao();
}
