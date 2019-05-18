package com.example.podcastapp;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface PodcastDao {
    @Query("SELECT * FROM podcast")
    List<Podcast> getAll();

    @Query("SELECT * FROM podcast WHERE id IN (:userIds)")
    List<Podcast> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM podcast WHERE title LIKE :first LIMIT 1")
    Podcast findByName(String first);

    @Query("SELECT * FROM podcast WHERE id = :userId")
    Podcast get(int userId);

    @Insert
    void insertAll(Podcast... users);

    @Delete
    void delete(Podcast user);
}
