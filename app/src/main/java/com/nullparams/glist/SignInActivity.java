package com.nullparams.glist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class SignInActivity extends AppCompatActivity {

    private Context context = this;
    private ImageView imageViewHiveLogo;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Window window;
    private View container;
    private ImageView imageViewDarkMode;
    private ImageView imageViewLightMode;
    private TextView textViewRegister;
    private TextView textViewForgotPassword;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mFireBaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(context);
        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);

        Button buttonSignIn = findViewById(R.id.button_sign_in);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        textViewRegister = findViewById(R.id.text_view_go_to_register);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView textViewRegister2 = findViewById(R.id.text_view_go_to_register_2);
        textViewRegister2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        textViewForgotPassword = findViewById(R.id.text_view_go_to_forgot_password);
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ForgotPasswordActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        TextView textViewForgotPassword2 = findViewById(R.id.text_view_go_to_forgot_password_2);
        textViewForgotPassword2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ForgotPasswordActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
        editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        textViewRegister.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        textViewForgotPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
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
        editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        textViewRegister.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewForgotPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void userLogin() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toasty.info(context, "Please enter your password", Toast.LENGTH_LONG, true).show();
            return;
        }

        mProgressDialog.setMessage("Signing in");
        mProgressDialog.show();

        mFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent i = new Intent(context, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                            hideKeyboard(SignInActivity.this);

                        } else {
                            Toasty.error(context, "Sign In Error, please try again. Please ensure that your email address and password are correct.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(context, RegisterActivity.class);
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
