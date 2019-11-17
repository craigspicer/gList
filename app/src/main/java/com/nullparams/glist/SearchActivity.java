package com.nullparams.glist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import com.nullparams.glist.adapters.RecentSearchesAdapter;
import com.nullparams.glist.adapters.SearchAdapter;
import com.nullparams.glist.database.ListEntity;
import com.nullparams.glist.database.RecentSearchesEntity;
import com.nullparams.glist.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private Context context = this;
    private AutoCompleteTextView searchField;
    private Window window;
    private View container;
    private Toolbar toolbar;
    private ImageView imageViewBack;
    private RecyclerView mRecentSearchesRecyclerView;
    private RecyclerView recyclerView;
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private List<RecentSearchesEntity> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();
    private List<ListEntity> mLists = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private Repository repository;
    private TextView textViewActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        repository = new Repository(getApplication());

        window = this.getWindow();
        container = findViewById(R.id.container);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewActivityTitle = findViewById(R.id.text_view_fragment_title);
        textViewActivityTitle.setText("Search");

        ImageView toolbarCheck = findViewById(R.id.toolbar_check);
        toolbarCheck.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        mRecentSearchesRecyclerView = findViewById(R.id.recent_searches_recycler);
        mRecentSearchesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecentSearchesRecyclerView.setNestedScrollingEnabled(false);

        imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setVisibility(View.VISIBLE);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String searchTerm = bundle.getString("searchTerm");
            performSearch(searchTerm);

            searchSuggestions = bundle.getStringArrayList("searchSuggestions");
        }

        searchField = findViewById(R.id.searchField);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setupSearch();
                    return true;
                }
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, searchSuggestions);
        searchField.setAdapter(adapter);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }
    }

    private void lightMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
        textViewActivityTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
        textViewActivityTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void setupSearch() {

        String searchTerm = searchField.getText().toString().trim().toLowerCase();

        recentSearchesList.clear();
        recentSearchesStringArrayList.clear();

        recentSearchesList = repository.getRecentSearches();

        for (RecentSearchesEntity recentSearches : recentSearchesList) {
            String recentSearchesListString = recentSearches.getSearchTerm();
            recentSearchesStringArrayList.add(recentSearchesListString);
        }

        if (!recentSearchesStringArrayList.contains(searchTerm) && !searchTerm.equals("")) {
            mRecentSearchesRecyclerView.setVisibility(View.VISIBLE);
            long timeStamp = System.currentTimeMillis();
            RecentSearchesEntity recentSearches = new RecentSearchesEntity(timeStamp, searchTerm);
            repository.insert(recentSearches);

        } else if (recentSearchesStringArrayList.contains(searchTerm)) {
            long timeStampQuery = repository.getTimeStamp(searchTerm);
            RecentSearchesEntity recentSearchesOld = new RecentSearchesEntity(timeStampQuery, searchTerm);
            repository.delete(recentSearchesOld);

            long timeStamp = System.currentTimeMillis();
            RecentSearchesEntity recentSearchesNew = new RecentSearchesEntity(timeStamp, searchTerm);
            repository.insert(recentSearchesNew);
        }
        performSearch(searchTerm);
    }

    private void performSearch(String searchTerm) {

        List<ListEntity> listEntityList = repository.searchAllLists(searchTerm);

        mLists.clear();

        for (ListEntity listEntity : listEntityList) {

            ListEntity listEntityItem = new ListEntity(listEntity.getId(), listEntity.getTitle(), listEntity.getTimeStamp(), listEntity.getFromEmailAddress(), listEntity.getVersion(), listEntity.getCallingFragment());
            mLists.add(listEntityItem);
        }

        SearchAdapter searchAdapter = new SearchAdapter(context, mLists, sharedPreferences, this);
        recyclerView.setAdapter(searchAdapter);

        recentSearchesList = repository.getRecentSearches();

        if (recentSearchesList.isEmpty()) {
            mRecentSearchesRecyclerView.setVisibility(View.GONE);
        }

        mRecentSearchesRecyclerView.setAdapter(new RecentSearchesAdapter(recentSearchesList, sharedPreferences, context, new RecentSearchesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecentSearchesEntity item) {
                searchField.setText(item.getSearchTerm());
                String searchTerm = searchField.getText().toString().trim().toLowerCase();
                performSearch(searchTerm);
            }
        }));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        hideKeyboard(this);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
