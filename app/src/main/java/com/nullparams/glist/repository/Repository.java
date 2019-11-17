package com.nullparams.glist.repository;

import android.app.Application;

import com.nullparams.glist.database.GlistDatabase;
import com.nullparams.glist.database.ListDAO;
import com.nullparams.glist.database.ListEntity;
import com.nullparams.glist.database.RecentSearchesDAO;
import com.nullparams.glist.database.RecentSearchesEntity;

import java.util.List;

public class Repository {

    private ListDAO listDAO;
    private RecentSearchesDAO recentSearchesDAO;

    public Repository(Application application) {

        GlistDatabase glistDatabase = GlistDatabase.getInstance(application);
        listDAO = glistDatabase.listDAO();
        recentSearchesDAO = glistDatabase.recentSearchesDAO();
    }

    public void insert(ListEntity listEntity) {
        listDAO.insert(listEntity);
    }

    public void update(ListEntity listEntity) {
        listDAO.update(listEntity);
    }

    public void delete(ListEntity listEntity) {
        listDAO.delete(listEntity);
    }

    public void deleteAllLists() {
        listDAO.deleteAll();
    }

    public List<ListEntity> getAllLists() {
        return listDAO.getAll();
    }

    public List<ListEntity> searchAllLists(String term) {
        return listDAO.search(term);
    }

    public void insert(RecentSearchesEntity recentSearchesEntity) {
        recentSearchesDAO.insert(recentSearchesEntity);
    }

    public void update(RecentSearchesEntity recentSearchesEntity) {
        recentSearchesDAO.update(recentSearchesEntity);
    }

    public void delete(RecentSearchesEntity recentSearchesEntity) {
        recentSearchesDAO.delete(recentSearchesEntity);
    }

    public void deleteAllRecentSearches() {
        recentSearchesDAO.deleteAll();
    }

    public List<RecentSearchesEntity> getRecentSearches() {
        return recentSearchesDAO.getAll();
    }

    public long getTimeStamp(String term) {
        return recentSearchesDAO.getTimeStamp(term);
    }
}
