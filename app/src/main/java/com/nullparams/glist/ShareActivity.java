package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.email.SendMailShare;
import com.nullparams.glist.models.Item;
import com.nullparams.glist.models.List;
import com.nullparams.glist.models.Notification;
import com.nullparams.glist.models.User;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ShareActivity extends AppCompatActivity {

    private Context context = this;
    private String mCurrentUserId, mSharedUserId, currentUserEmail, sharedUserEmail;
    private FirebaseFirestore mFireBaseFireStore;
    private EditText mSharedUserEmailText;
    private static final int CONTACT_PICKER_RESULT = 2;
    private static final int PERMISSION_READ_CONTACTS_REQUEST = 11;
    private ArrayList<String> itemArrayList = new ArrayList<>();
    private String uniqueId;
    private String collectionId;
    private ImageView imageViewBack;
    private Toolbar toolbar;
    private Window window;
    private View container;
    private ImageView toolbarCheck;
    private ImageView importContactIcon;
    private TextView importContactText;
    private TextView whatsAppText;
    private String listName;
    private TextView textViewActivityTitle;
    private FirebaseAnalytics mFireBaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        window = this.getWindow();
        container = findViewById(R.id.container);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            collectionId = bundle.getString("collectionId");
            uniqueId = bundle.getString("uniqueId");
            listName = bundle.getString("listName");
            itemArrayList = bundle.getStringArrayList("itemArrayList");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            FirebaseUser mUser = mFireBaseAuth.getCurrentUser();
            currentUserEmail = mUser.getEmail();
        }

        mSharedUserEmailText = findViewById(R.id.sharedUserEmail);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewActivityTitle = findViewById(R.id.text_view_fragment_title);
        textViewActivityTitle.setText("Share");

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
                shareList();
                hideKeyboard(ShareActivity.this);
            }
        });

        importContactText = findViewById(R.id.import_text);
        importContactText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        importContactIcon = findViewById(R.id.import_icon);
        importContactIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        whatsAppText = findViewById(R.id.whatsapp_text);
        whatsAppText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whatsAppList();
            }
        });

        ImageView whatsAppIcon = findViewById(R.id.whatsapp_icon);
        whatsAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whatsAppList();
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

        mSharedUserEmailText.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        mSharedUserEmailText.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        DrawableCompat.setTint(mSharedUserEmailText.getBackground(), ContextCompat.getColor(context, R.color.PrimaryDark));
        whatsAppText.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        importContactText.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(importContactIcon, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
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

        mSharedUserEmailText.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        mSharedUserEmailText.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        DrawableCompat.setTint(mSharedUserEmailText.getBackground(), ContextCompat.getColor(context, R.color.PrimaryLight));
        whatsAppText.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        importContactText.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(importContactIcon, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
    }

    public void getPermissionToReadUserContacts() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to access contacts")
                .setMessage("This permission is needed in order to get an email address for a selected contact. Manually enable in Settings > Apps & notifications > gList > Permissions.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS_REQUEST);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_READ_CONTACTS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "Read Contacts permission granted", Toast.LENGTH_LONG, true).show();
                doLaunchContactPicker();
            } else {
                Toasty.error(context, "Read Contacts permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void doLaunchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_RESULT && resultCode == RESULT_OK) {

            String email = "";

            Uri result = data.getData();
            String id = result.getLastPathSegment();

            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);

            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            if (cursor != null) {
                cursor.close();
            }
            if (email.length() == 0) {
                Toasty.info(context, "No email address stored for this contact", Toast.LENGTH_LONG, true).show();
            } else {
                mSharedUserEmailText.setText(email);
            }
        }
    }

    private void shareList() {

        sharedUserEmail = mSharedUserEmailText.getText().toString().trim().toLowerCase();

        if (sharedUserEmail.trim().isEmpty()) {
            Toasty.info(context, "Please enter an email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        if (sharedUserEmail.equals(currentUserEmail)) {

            SendMailShare.sendMail(sharedUserEmail, currentUserEmail, itemArrayList, listName);

            Toasty.success(context, "List emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
            finish();

        } else {

            DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_list").document(sharedUserEmail);
            userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            SendMailShare.sendMail(sharedUserEmail, currentUserEmail, itemArrayList, listName);

                            User user = document.toObject(User.class);
                            mSharedUserId = user.getId();

                            DocumentReference listRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId);
                            listRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {

                                            List list = document.toObject(List.class);

                                            DocumentReference sharedUserListPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Shared_lists").document(uniqueId);
                                            sharedUserListPath.set(new List(uniqueId, list.getTitle(), list.getTimeStamp(), list.getVersion(), "SharedFragment"));

                                            //Write notification for shared user
                                            DocumentReference notificationPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Notifications").document();
                                            notificationPath.set(new Notification(uniqueId, list.getTitle(), list.getVersion()));

                                            //Establish shared list connection
                                            DocumentReference currentUserSharedListPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId);
                                            currentUserSharedListPath.set(new List(uniqueId, list.getTitle(), list.getTimeStamp(), list.getVersion(), "SharedFragment"));
                                        }
                                    }
                                }
                            });

                            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(collectionId).document(uniqueId).collection(uniqueId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    Item item = document.toObject(Item.class);

                                                    DocumentReference sharedUserItemsPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Shared_lists").document(uniqueId).collection(uniqueId).document(item.getId());
                                                    sharedUserItemsPath.set(new Item(item.getId(), item.getAmount(), item.getName(), item.getStrike(), item.getCost()));

                                                    //Establish shared list connection
                                                    DocumentReference currentUserSharedItemsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId).collection(uniqueId).document(item.getId());
                                                    currentUserSharedItemsPath.set(new Item(item.getId(), item.getAmount(), item.getName(), item.getStrike(), item.getCost()));
                                                }
                                            }
                                        }
                                    });

                            //Set participants
                            DocumentReference currentUserParticipantsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId).collection("Participants").document(currentUserEmail);
                            currentUserParticipantsPath.set(new User(mCurrentUserId, currentUserEmail));

                            DocumentReference currentUserParticipantsPath2 = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId).collection("Participants").document(sharedUserEmail);
                            currentUserParticipantsPath2.set(new User(mSharedUserId, sharedUserEmail));

                            java.util.List<User> userList = new ArrayList<>();

                            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(uniqueId).collection("Participants")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {

                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    User user = document.toObject(User.class);
                                                    userList.add(user);

                                                    for (User user1 : userList) {

                                                        for (User user2 : userList) {

                                                            DocumentReference userParticipantsPath = mFireBaseFireStore.collection("Users").document(user1.getId()).collection("Shared_lists").document(uniqueId).collection("Participants").document(user2.getEmailAddress());
                                                            userParticipantsPath.set(new User(user2.getId(), user2.getEmailAddress()));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });

                            Bundle bundle = new Bundle();
                            bundle.putString("list_name", listName);
                            mFireBaseAnalytics.logEvent("share_device", bundle);

                            Toasty.success(context, "List shared with and emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                            finish();

                        } else {

                            SendMailShare.sendMail(sharedUserEmail, currentUserEmail, itemArrayList, listName);

                            Bundle bundle = new Bundle();
                            bundle.putString("list_name", listName);
                            mFireBaseAnalytics.logEvent("share_email", bundle);

                            Toasty.success(context, "List emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                            finish();
                        }

                    } else {
                        Toasty.error(context, "Please ensure that there is an active network connection to share a list", Toast.LENGTH_LONG, true).show();
                    }
                }
            });
        }
    }

    private void whatsAppList() {

        String itemString = listName + "\n\n" + itemArrayList.toString();
        String modItemString = itemString.replaceAll(",", "\n");
        String modItemString2 = modItemString.replace("[", "");
        String modItemString3 = modItemString2.replace("]", "");

        Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
        whatsAppIntent.setType("text/plain"); //html
        whatsAppIntent.setPackage("com.whatsapp");
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.nullparams.glist\n\n" + modItemString3);

        try {
            startActivity(whatsAppIntent);

            Bundle bundle = new Bundle();
            bundle.putString("list_name", listName);
            mFireBaseAnalytics.logEvent("share_whatsapp", bundle);

        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
            Toasty.error(context, "WhatsApp is not installed", Toast.LENGTH_LONG, true).show();
        }
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
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
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
    }
}
