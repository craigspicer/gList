package com.nullparams.glist.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ListDAO {

    @Insert
    void insert(ListEntity listEntity);

    @Update
    void update(ListEntity listEntity);

    @Delete
    void delete(ListEntity listEntity);

    @Query("SELECT * FROM lists_table")
    List<ListEntity> getAll();

    @Query("DELETE FROM lists_table")
    void deleteAll();

    @Query("SELECT * FROM lists_table WHERE lists_table.title LIKE '%' || :term || '%' COLLATE NOCASE")
    List<ListEntity> search(String term);

    //@Query("SELECT * FROM lists_table WHERE lists_table.title LIKE '%' || :term || '%' OR lists_table.fromEmailAddress LIKE '%' || :term || '%' COLLATE NOCASE")
    //List<ListEntity> search(String term);
}
