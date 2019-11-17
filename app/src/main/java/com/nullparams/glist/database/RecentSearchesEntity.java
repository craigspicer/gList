package com.nullparams.glist.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recent_searches_table")
public class RecentSearchesEntity {

    @PrimaryKey
    @NonNull
    private long timeStamp;

    private String searchTerm;

    public RecentSearchesEntity (long timeStamp, String searchTerm) {

        this.timeStamp = timeStamp;
        this.searchTerm = searchTerm;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getSearchTerm() {
        return searchTerm;
    }
}
