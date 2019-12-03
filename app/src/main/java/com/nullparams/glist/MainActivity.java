package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nullparams.glist.database.ListEntity;
import com.nullparams.glist.database.RecentSearchesEntity;
import com.nullparams.glist.fragments.BinFragment;
import com.nullparams.glist.fragments.ListsFragment;
import com.nullparams.glist.fragments.ProfileFragment;
import com.nullparams.glist.fragments.SharedFragment;
import com.nullparams.glist.models.List;
import com.nullparams.glist.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private Window window;
    private View container;
    private TextView textViewFragmentTitle;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private Toolbar toolbar;
    private AutoCompleteTextView searchField;
    private BottomNavigationView bottomNav;
    private ImageView imageViewToolbarAdd;
    private Repository repository;
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private java.util.List<RecentSearchesEntity> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.navigation_my_lists:
                            selectedFragment = new ListsFragment();
                            textViewFragmentTitle.setText("My Lists");
                            break;
                        case R.id.navigation_shared:
                            selectedFragment = new SharedFragment();
                            textViewFragmentTitle.setText("Shared");
                            break;
                        case R.id.navigation_bin:
                            selectedFragment = new BinFragment();
                            textViewFragmentTitle.setText("Bin");
                            break;
                        case R.id.navigation_profile:
                            selectedFragment = new ProfileFragment();
                            textViewFragmentTitle.setText("Profile");
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new Repository(getApplication());

        AppRate.with(this)
                .setInstallDays(7)
                .setLaunchTimes(5)
                .setRemindInterval(2)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_frame, new ListsFragment());
            fragmentTransaction.commit();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        window = this.getWindow();
        container = findViewById(R.id.container);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewFragmentTitle = findViewById(R.id.text_view_fragment_title);
        textViewFragmentTitle.setText("My Lists");

        imageViewToolbarAdd = findViewById(R.id.toolbar_add);
        imageViewToolbarAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uniqueId = UUID.randomUUID().toString();
                Intent i = new Intent(context, ListActivity.class);
                i.putExtra("version", "0");
                i.putExtra("collectionId", "My_lists");
                i.putExtra("uniqueId", uniqueId);
                i.putExtra("callingFragment", "ListsFragment");
                i.putExtra("fromNotification", false);
                startActivity(i);
            }
        });

        searchField = findViewById(R.id.searchField);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, searchSuggestions);
        searchField.setAdapter(adapter);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        registerToken();
        setupSearchSuggestions();
    }

    private void lightMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector));

        ImageViewCompat.setImageTintList(imageViewToolbarAdd, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector_light));

        ImageViewCompat.setImageTintList(imageViewToolbarAdd, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
    }

    private void registerToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();

                Map<String, Object> userToken = new HashMap<>();
                userToken.put("User_Token_ID", deviceToken);

                DocumentReference userTokenPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Tokens").document("User_Token");
                userTokenPath.set(userToken);
            }
        });
    }

    private void setupSearchSuggestions() {

        CollectionReference myListsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists");
        myListsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            repository.deleteAllLists();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                List list = document.toObject(List.class);

                                if (!searchSuggestions.contains(list.getTitle())) {
                                    searchSuggestions.add(list.getTitle());
                                }
                            }
                        }
                    }
                });

        CollectionReference sharedListsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists");
        sharedListsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                List list = document.toObject(List.class);

                                if (!searchSuggestions.contains(list.getTitle())) {
                                    searchSuggestions.add(list.getTitle());
                                }
                            }
                        }
                    }
                });

        CollectionReference binRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin");
        binRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                List list = document.toObject(List.class);

                                if (!searchSuggestions.contains(list.getTitle())) {
                                    searchSuggestions.add(list.getTitle());
                                }
                            }
                        }
                    }
                });
    }

    private void search() {

        repository.deleteAllLists();

        CollectionReference myListsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists");
        myListsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                List list = document.toObject(List.class);

                                ListEntity listEntity = new ListEntity(list.getId(), list.getTitle(), list.getTimeStamp(), list.getVersion(), "My_lists", list.getCreatingFragment());
                                repository.insert(listEntity);
                            }
                        }
                    }
                });

        CollectionReference sharedListsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists");
        sharedListsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                List list = document.toObject(List.class);

                                ListEntity listEntity = new ListEntity(list.getId(), list.getTitle(), list.getTimeStamp(), list.getVersion(), "Shared_lists", list.getCreatingFragment());
                                repository.insert(listEntity);
                            }
                        }
                    }
                });

        CollectionReference binRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin");
        binRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                List list = document.toObject(List.class);

                                ListEntity listEntity = new ListEntity(list.getId(), list.getTitle(), list.getTimeStamp(), list.getVersion(), "Bin", list.getCreatingFragment());
                                repository.insert(listEntity);
                            }

                            String searchTerm = searchField.getText().toString().trim().toLowerCase();

                            recentSearchesList.clear();
                            recentSearchesStringArrayList.clear();

                            recentSearchesList = repository.getRecentSearches();

                            for (RecentSearchesEntity recentSearches : recentSearchesList) {
                                String recentSearchesListString = recentSearches.getSearchTerm();
                                recentSearchesStringArrayList.add(recentSearchesListString);
                            }

                            if (!recentSearchesStringArrayList.contains(searchTerm) && !searchTerm.equals("")) {
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

                            Intent i = new Intent(context, SearchActivity.class);
                            i.putExtra("searchTerm", searchTerm);
                            i.putExtra("searchSuggestions", searchSuggestions);
                            startActivity(i);
                        }
                    }
                });
    }
}
