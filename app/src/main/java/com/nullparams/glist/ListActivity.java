package com.nullparams.glist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.adapters.ItemAdapter;
import com.nullparams.glist.models.Item;
import com.nullparams.glist.models.List;
import com.nullparams.glist.models.Notification;
import com.nullparams.glist.models.User;
import com.nullparams.glist.util.GrocerySuggestions;

import java.util.ArrayList;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class ListActivity extends AppCompatActivity {

    private Context context = this;
    private AutoCompleteTextView mEditTextName;
    private TextView mTextViewAmount;
    private int mAmount = 0;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private ItemAdapter adapter;
    private ArrayList<String> itemArrayList = new ArrayList<>();
    private String uniqueId;
    private Window window;
    private View container;
    private EditText editTextListName;
    private ImageView imageViewBack;
    private String collectionId;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;
    private TextView toolbarShare;
    private int updatedVersionInt;
    private String callingFragment;
    private String mCurrentUserEmail;
    private String listName;
    private boolean fromNotification;
    private EditText editTextCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        window = this.getWindow();
        container = findViewById(R.id.container);

        String listName = "";

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            String versionString = bundle.getString("version");
            int versionInt = Integer.parseInt(versionString);
            updatedVersionInt = versionInt + 1;

            collectionId = bundle.getString("collectionId");
            uniqueId = bundle.getString("uniqueId");
            listName = bundle.getString("listName");
            callingFragment = bundle.getString("callingFragment");

            fromNotification = bundle.getBoolean("fromNotification");
        }

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            FirebaseUser firebaseUser = mFireBaseAuth.getCurrentUser();
            mCurrentUserEmail = firebaseUser.getEmail();
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        editTextListName = findViewById(R.id.toolbar_list_title);
        editTextListName.setText(listName);

        imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbarShare = toolbar.findViewById(R.id.toolbar_share);
        toolbarShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.info(context, "Swipe left to strike an item\nSwipe right to un-strike an item\nTap an item to edit it", Toast.LENGTH_LONG, true).show();
            }
        });

        mEditTextName = findViewById(R.id.searchField);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, GrocerySuggestions.getGrocerySuggestions(context));
        mEditTextName.setAdapter(adapter);

        mTextViewAmount = findViewById(R.id.grocery_amount);
        editTextCost = findViewById(R.id.edit_text_cost);

        final Button buttonIncrease = findViewById(R.id.button_increase);
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increase();
            }
        });

        final Button buttonDecrease = findViewById(R.id.button_decrease);
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrease();
            }
        });

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        setUpRecyclerView();
    }

    private void lightMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
        editTextListName.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextListName.setHintTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        toolbarShare.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        mTextViewAmount.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextCost.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextCost.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        DrawableCompat.setTint(editTextCost.getBackground(), ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
        editTextListName.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextListName.setHintTextColor(ContextCompat.getColor(context, R.color.SecondaryLight));
        toolbarShare.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        mTextViewAmount.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextCost.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextCost.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        DrawableCompat.setTint(editTextCost.getBackground(), ContextCompat.getColor(context, R.color.SecondaryDark));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateListName();

        if (fromNotification) {

            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            hideKeyboard(this);
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);

        } else {

            finish();
            hideKeyboard(this);
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }
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

    private void increase() {
        mAmount++;
        mTextViewAmount.setText(String.valueOf(mAmount));
    }

    private void decrease() {
        if (mAmount > 0) {
            mAmount--;
            mTextViewAmount.setText(String.valueOf(mAmount));
        }
    }

    private void addItem() {

        String name = mEditTextName.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toasty.info(context, "Please enter an item name", Toast.LENGTH_LONG, true).show();
            return;
        }

        String amount = mTextViewAmount.getText().toString();
        String cost = editTextCost.getText().toString();

        listName = editTextListName.getText().toString().trim();
        if (listName.equals("")) {
            listName = "My List";
        }

        long timeStamp = System.currentTimeMillis();

        String randomItemId = UUID.randomUUID().toString();

        if (callingFragment.equals("ListsFragment")) {

            DocumentReference listPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId);
            listPath.set(new List(uniqueId, listName, timeStamp, Integer.toString(updatedVersionInt), "ListsFragment"));

            DocumentReference itemPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId).collection(uniqueId).document(randomItemId);
            itemPath.set(new Item(randomItemId, amount, name, false, cost));

            //Set participants
            DocumentReference currentUserParticipantsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(uniqueId).collection("Participants").document(mCurrentUserEmail);
            currentUserParticipantsPath.set(new User(mCurrentUserId, mCurrentUserEmail));

        } else if (callingFragment.equals("SharedFragment")) {

            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId).collection("Participants")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    User user = document.toObject(User.class);
                                    String sharedUserId = user.getId();

                                    if (!sharedUserId.equals(mCurrentUserId)) {

                                        //Write notification for shared user
                                        DocumentReference notificationPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Notifications").document();
                                        notificationPath.set(new Notification(uniqueId, listName, Integer.toString(updatedVersionInt)));
                                    }

                                    DocumentReference listPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(uniqueId);
                                    listPath.set(new List(uniqueId, listName, timeStamp, Integer.toString(updatedVersionInt), "SharedFragment"));

                                    DocumentReference itemPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(uniqueId).collection(uniqueId).document(randomItemId);
                                    itemPath.set(new Item(randomItemId, amount, name, false, cost));
                                }

                            } else {
                                Toasty.error(context, "Please ensure that there is an active network connection to share a list", Toast.LENGTH_LONG, true).show();
                            }
                        }
                    });
        }

        mTextViewAmount.setText("0");
        mEditTextName.getText().clear();
        editTextCost.getText().clear();
    }

    private void setUpRecyclerView() {

        final CollectionReference listRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId).collection(uniqueId);
        Query query = listRef.orderBy("strike", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Item> options = new FirestoreRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        adapter = new ItemAdapter(options, sharedPreferences, context, callingFragment, uniqueId);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.strikeItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.unStrikeItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new ItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                Item item = documentSnapshot.toObject(Item.class);

                Intent i = new Intent(context, ItemActivity.class);
                i.putExtra("itemId", item.getId());
                i.putExtra("uniqueId", uniqueId);
                i.putExtra("collectionId", collectionId);
                i.putExtra("itemName", item.getName());
                i.putExtra("itemAmount", item.getAmount());
                i.putExtra("itemCost", item.getCost());
                i.putExtra("strike", item.getStrike());
                startActivity(i);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void share() {

        listName = editTextListName.getText().toString().trim();
        if (listName.equals("")) {
            listName = "My List";
        }

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId).collection(uniqueId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            itemArrayList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Item item = document.toObject(Item.class);

                                if (!item.getStrike()) {
                                    String setItem;
                                    if (item.getAmount().equals("0")) {
                                        setItem = item.getName();
                                    } else {
                                        setItem = item.getAmount() + " " + item.getName();
                                    }
                                    itemArrayList.add(setItem);
                                }
                            }

                            if (itemArrayList.isEmpty()) {
                                Toasty.info(context, "This list is empty", Toast.LENGTH_LONG, true).show();

                            } else {

                                Intent i = new Intent(context, ShareActivity.class);
                                i.putExtra("collectionId", collectionId);
                                i.putExtra("uniqueId", uniqueId);
                                i.putExtra("listName", listName);
                                i.putExtra("itemArrayList", itemArrayList);
                                startActivity(i);
                                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            }
                        }
                    }
                });
    }

    private void updateListName() {

        String listName = editTextListName.getText().toString().trim();
        if (listName.equals("")) {
            listName = "My List";
        }

        DocumentReference listPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId);
        listPath.update("title", listName);
    }
}
