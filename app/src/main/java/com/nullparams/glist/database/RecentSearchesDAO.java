package com.nullparams.glist.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecentSearchesDAO {

    @Insert
    void insert(RecentSearchesEntity recentSearchesEntity);

    @Update
    void update(RecentSearchesEntity recentSearchesEntity);

    @Delete
    void delete(RecentSearchesEntity recentSearchesEntity);

    @Query("SELECT * FROM recent_searches_table ORDER BY timeStamp DESC")
    List<RecentSearchesEntity> getAll();

    @Query("DELETE FROM recent_searches_table")
    void deleteAll();

    @Query("SELECT * FROM recent_searches_table WHERE recent_searches_table.searchTerm LIKE :term COLLATE NOCASE")
    long getTimeStamp(String term);
}
