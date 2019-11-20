package com.nullparams.glist.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ListEntity.class, RecentSearchesEntity.class}, version = 5, exportSchema = false)

public abstract class GlistDatabase extends RoomDatabase {

    private static  GlistDatabase instance;
    public abstract ListDAO listDAO();
    public abstract RecentSearchesDAO recentSearchesDAO();

    public static synchronized GlistDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), GlistDatabase.class, "glist_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
