package com.example.podcastapp;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Podcast {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "feed")
    public String feed;

    @Ignore
    public Podcast(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Podcast(String title, String description, String feed) {
        this.title = title;
        this.description = description;
        this.feed = feed;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s%n%s%n%s%n", this.title, this.description, this.feed);
    }
}
