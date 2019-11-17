package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Context context = this;
    private ImageView imageViewHiveLogo;
    private EditText editTextEmail;
    private Window window;
    private View container;
    private ImageView imageViewDarkMode;
    private ImageView imageViewLightMode;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mFireBaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAuth = FirebaseAuth.getInstance();

        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);
        editTextEmail = findViewById(R.id.edit_text_email);
        Button buttonSendLink = findViewById(R.id.button_send_link);

        buttonSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLink();
            }
        });

        TextView textViewSignIn = findViewById(R.id.text_view_go_to_sign_in);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageViewDarkMode = findViewById(R.id.image_view_dark_mode);
        imageViewLightMode = findViewById(R.id.image_view_light_mode);

        window = this.getWindow();
        container = findViewById(R.id.container2);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        imageViewDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkMode();
                saveDarkModePreference();
            }
        });

        imageViewLightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lightMode();
                saveLightModePreference();
            }
        });
    }

    private void saveLightModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("darkModeOn", false);
        prefsEditor.apply();
    }

    private void saveDarkModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("darkModeOn", true);
        prefsEditor.apply();
    }

    private void lightMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        imageViewLightMode.setVisibility(View.GONE);
        imageViewDarkMode.setVisibility(View.VISIBLE);

        imageViewHiveLogo.setImageResource(R.drawable.logo);

        editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        imageViewDarkMode.setVisibility(View.GONE);
        imageViewLightMode.setVisibility(View.VISIBLE);

        imageViewHiveLogo.setImageResource(R.drawable.logo_light_text);

        editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void sendLink() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your registered email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        mFireBaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toasty.success(context, "Password reset email sent", Toast.LENGTH_LONG, true).show();
                            hideKeyboard(ForgotPasswordActivity.this);
                            onBackPressed();
                        } else {
                            Toasty.error(context, "Error sending password reset email", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(context, SignInActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
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
