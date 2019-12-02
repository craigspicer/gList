package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.email.RegistrationEmail;
import com.nullparams.glist.models.User;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class UpdateUserDetailsActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    private Context context = this;
    private FirebaseUser firebaseUser;
    private ProgressDialog mProgressDialog;
    private ImageView imageViewHiveLogo;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private String guestEmail;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private Window window;
    private View container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_details);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            mCurrentUserId = firebaseUser.getUid();
            guestEmail = firebaseUser.getEmail();
        }

        mProgressDialog = new ProgressDialog(context);
        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);

        Button buttonConfirm = findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetails();
            }
        });

        window = this.getWindow();
        container = findViewById(R.id.container2);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        reAuthenticateUser();
    }

    private void lightMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        imageViewHiveLogo.setImageResource(R.drawable.logo);

        editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        imageViewHiveLogo.setImageResource(R.drawable.logo_light_text);

        editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void updateDetails() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toasty.info(context, "Please enter a valid email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toasty.info(context, "Please enter a password", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toasty.info(context, "Your password must be at least 8 characters and must contain at least 1 number", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toasty.info(context, "Please enter the same password in the confirm password field", Toast.LENGTH_LONG, true).show();
            return;
        }

        mProgressDialog.setMessage("Updating");
        mProgressDialog.show();

        firebaseUser.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            String guestPassword = md5(guestEmail);

                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(email, guestPassword);

                            firebaseUser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            firebaseUser.updatePassword(password)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                AuthCredential credential = EmailAuthProvider
                                                                        .getCredential(email, password);

                                                                firebaseUser.reauthenticate(credential)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                DocumentReference userDetailsPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId);
                                                                                userDetailsPath.set(new User(mCurrentUserId, email));

                                                                                DocumentReference userListPath = mFireBaseFireStore.collection("User_list").document(email);
                                                                                userListPath.set(new User(mCurrentUserId, email));

                                                                                mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists")
                                                                                        .get()
                                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                if (task.isSuccessful()) {

                                                                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                                        String listId = document.getId();

                                                                                                        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(listId).collection("Participants")
                                                                                                                .get()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                                        if (task.isSuccessful()) {

                                                                                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                                                                DocumentReference particpantsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(listId).collection("Participants").document(document.getId());
                                                                                                                                particpantsRef.update("emailAddress", email);
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        });

                                                                                mProgressDialog.dismiss();

                                                                                RegistrationEmail.sendMail(email);

                                                                                Intent i = new Intent(context, MainActivity.class);
                                                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                startActivity(i);
                                                                                finish();
                                                                                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                                                                                Toasty.success(context, "Details updated", Toast.LENGTH_LONG, true).show();
                                                                            }
                                                                        });

                                                            } else {
                                                                Toasty.error(context, "An error occurred, please try again.", Toast.LENGTH_LONG, true).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                        } else {
                            Toasty.error(context, "An error occurred, please try again.", Toast.LENGTH_LONG, true).show();
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

    private void reAuthenticateUser() {

        String guestPassword = md5(guestEmail);

        AuthCredential credential = EmailAuthProvider
                .getCredential(guestEmail, guestPassword);

        firebaseUser.reauthenticate(credential);
    }
}
