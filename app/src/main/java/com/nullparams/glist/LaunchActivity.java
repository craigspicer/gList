package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nullparams.glist.models.List;
import com.nullparams.glist.models.User;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class LaunchActivity extends AppCompatActivity {

    private Context context = this;
    private ImageView imageViewHiveLogo;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private Window window;
    private View container;
    private String mCurrentUserEmail;
    private FirebaseUser mCurrentUser;
    private String uniqueId = UUID.randomUUID().toString();
    private String uniqueId2 = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();

            Intent i = new Intent(context, MainActivity.class);
            startActivity(i);
            finish();

        } else {
            guestMode();
        }

        window = this.getWindow();
        container = findViewById(R.id.container);

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

        imageViewHiveLogo.setImageResource(R.drawable.logo);
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        imageViewHiveLogo.setImageResource(R.drawable.logo_light_text);
    }

    private void guestMode() {

        Random rand = new Random();
        int num = rand.nextInt(9000000) + 1000000;
        String randomNumber = Integer.toString(num);

        String guestEmail = "guest" + randomNumber + "@nullparams.com";
        String guestPassword = md5(guestEmail);

        mFireBaseAuth.createUserWithEmailAndPassword(guestEmail, guestPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (mFireBaseAuth.getCurrentUser() != null) {
                                mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
                                mCurrentUser = mFireBaseAuth.getCurrentUser();
                                mCurrentUserEmail = mCurrentUser.getEmail();
                            }

                            long timeStamp = System.currentTimeMillis();

                            DocumentReference userDetailsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId);
                            userDetailsPath.set(new User(mCurrentUserId, mCurrentUserEmail));

                            DocumentReference userListPath = mFireBaseFireStore.collection("User_list").document(mCurrentUserEmail);
                            userListPath.set(new User(mCurrentUserId, mCurrentUserEmail));

                            DocumentReference myGroceryListPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(uniqueId);
                            myGroceryListPath.set(new List(uniqueId, "Grocery List", timeStamp, "1", "ListsFragment"));

                            DocumentReference myToDoListPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(uniqueId2);
                            myToDoListPath.set(new List(uniqueId2, "To-Do List", timeStamp, "1", "ListsFragment"));

                            //Set participants
                            DocumentReference groceryListParticipantsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(uniqueId).collection("Participants").document(mCurrentUserEmail);
                            groceryListParticipantsPath.set(new User(mCurrentUserId, mCurrentUserEmail));

                            //Set participants
                            DocumentReference toDoListParticipantsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(uniqueId2).collection("Participants").document(mCurrentUserEmail);
                            toDoListParticipantsPath.set(new User(mCurrentUserId, mCurrentUserEmail));

                            registerToken();

                            Intent i = new Intent(context, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                        } else {
                            Toasty.error(context, "Error, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
    }

    public static String md5(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")), 0, s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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
}
