package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.models.Item;
import com.nullparams.glist.models.User;
import com.nullparams.glist.util.GrocerySuggestions;

import es.dmoral.toasty.Toasty;

public class ItemActivity extends AppCompatActivity {

    private Context context = this;
    private ImageView imageViewBack;
    private Toolbar toolbar;
    private Window window;
    private View container;
    private ImageView toolbarCheck;
    private TextView textViewActivityTitle;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private String uniqueId;
    private String collectionId;
    private String itemId;
    private String itemName;
    private String itemAmount;
    private String itemCost;
    private boolean strike;
    private AutoCompleteTextView mEditTextName;
    private TextView mTextViewAmount;
    private int mAmount = 0;
    private EditText editTextCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        window = this.getWindow();
        container = findViewById(R.id.container);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            uniqueId = bundle.getString("uniqueId");
            collectionId = bundle.getString("collectionId");
            itemId = bundle.getString("itemId");
            itemName = bundle.getString("itemName");
            itemAmount = bundle.getString("itemAmount");
            itemCost = bundle.getString("itemCost");
            strike = bundle.getBoolean("strike");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewActivityTitle = findViewById(R.id.text_view_fragment_title);
        textViewActivityTitle.setText("Edit Item");

        imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        toolbarCheck = toolbar.findViewById(R.id.toolbar_check);
        toolbarCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateItem();
                hideKeyboard(ItemActivity.this);
            }
        });

        mEditTextName = findViewById(R.id.searchField);
        mEditTextName.setText(itemName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, GrocerySuggestions.getGrocerySuggestions(context));
        mEditTextName.setAdapter(adapter);

        mTextViewAmount = findViewById(R.id.grocery_amount);
        mTextViewAmount.setText(itemAmount);
        editTextCost = findViewById(R.id.edit_text_cost);
        editTextCost.setText(itemCost);

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

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
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
        ImageViewCompat.setImageTintList(toolbarCheck, ContextCompat.getColorStateList(context, R.color.PrimaryDark));

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
        textViewActivityTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(toolbarCheck, ContextCompat.getColorStateList(context, R.color.PrimaryLight));

        mTextViewAmount.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextCost.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextCost.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        DrawableCompat.setTint(editTextCost.getBackground(), ContextCompat.getColor(context, R.color.SecondaryDark));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        hideKeyboard(this);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void updateItem() {

        String name = mEditTextName.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toasty.info(context, "Please enter an item name", Toast.LENGTH_LONG, true).show();
            return;
        }

        String amount = mTextViewAmount.getText().toString();
        String cost = editTextCost.getText().toString();

        if (collectionId.equals("My_lists") || collectionId.equals("Bin")) {

            DocumentReference itemPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId).collection(uniqueId).document(itemId);
            itemPath.set(new Item(itemId, amount, name, strike, cost));

            long timeStamp = System.currentTimeMillis();

            DocumentReference listRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(uniqueId);
            listRef.update("timeStamp", timeStamp);

        } else if (collectionId.equals("Shared_lists")) {

            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId).collection("Participants")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    User user = document.toObject(User.class);
                                    String userId = user.getId();

                                    mFireBaseFireStore.collection("Users").document(userId).collection("Shared_lists").document(uniqueId).collection("Participants")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {

                                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                                            User user = document.toObject(User.class);

                                                            DocumentReference itemPath = mFireBaseFireStore.collection("Users").document(user.getId()).collection("Shared_lists").document(uniqueId).collection(uniqueId).document(itemId);
                                                            itemPath.set(new Item(itemId, amount, name, strike, cost));

                                                            long timeStamp = System.currentTimeMillis();

                                                            DocumentReference listRef = mFireBaseFireStore.collection("Users").document(user.getId()).collection("Shared_lists").document(uniqueId);
                                                            listRef.update("timeStamp", timeStamp);
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    });
        }

        Toasty.info(context, "Item updated", Toast.LENGTH_LONG, true).show();
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
