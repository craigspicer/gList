package com.nullparams.glist.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nullparams.glist.R;
import com.nullparams.glist.SignInActivity;
import com.nullparams.glist.UpdateUserDetailsActivity;
import com.nullparams.glist.database.GlistDatabase;
import com.nullparams.glist.repository.Repository;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private Context context;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private FirebaseAuth mFireBaseAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST = 13;
    private ImageView imageViewProfilePic;
    private TextView textViewUsername;
    private SharedPreferences sharedPreferences;
    private ConstraintLayout layout;
    private TextView textViewDarkMode;
    private TextView textViewSearchHistory;
    private TextView textViewLogout;
    private ImageView imageViewCameraIcon;
    private Toolbar toolbar;
    private TextView textViewFragmentTitle;
    private Window window;
    private BottomNavigationView bottomNav;
    private ImageView imageViewToolbarAdd;
    private ImageView clearSearchHistory;
    private String mCurrentUserEmail;
    private FirebaseUser firebaseUser;
    private TextView textViewUpdateDetails;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        context = getActivity();

        Repository repository = new Repository(getActivity().getApplication());

        AutoCompleteTextView searchField = getActivity().findViewById(R.id.searchField);
        searchField.setVisibility(View.GONE);

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            firebaseUser = mFireBaseAuth.getCurrentUser();
            mCurrentUserEmail = firebaseUser.getEmail();
        }

        layout = view.findViewById(R.id.container2);
        textViewDarkMode = view.findViewById(R.id.text_view_dark_mode);

        imageViewProfilePic = view.findViewById(R.id.image_view_profile_pic);

        textViewUsername = view.findViewById(R.id.text_view_username);
        textViewUsername.setText(mCurrentUserEmail);

        imageViewCameraIcon = view.findViewById(R.id.image_view_camera_icon);
        imageViewCameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else {
                    openFileChooser();
                }
            }
        });

        imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else {
                    openFileChooser();
                }
            }
        });

        clearSearchHistory = view.findViewById(R.id.image_view_bin_icon);

        ImageView imageViewClearSearchHistoryIcon = view.findViewById(R.id.image_view_search_history_icon);
        imageViewClearSearchHistoryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                repository.deleteAllRecentSearches();
                Toasty.info(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

        textViewSearchHistory = view.findViewById(R.id.text_view_search_history);
        textViewSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                repository.deleteAllRecentSearches();
                Toasty.info(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

        clearSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                repository.deleteAllRecentSearches();
                Toasty.info(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

        textViewLogout = view.findViewById(R.id.text_view_logout);
        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogOut();
            }
        });

        ImageView imageViewLogout = view.findViewById(R.id.image_view_logout);
        imageViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogOut();
            }
        });

        ImageView imageViewUpdateDetails = view.findViewById(R.id.image_view_update_details);
        textViewUpdateDetails = view.findViewById(R.id.text_view_update_details);

        String emailAddressSubString = mCurrentUserEmail.substring(0, 5);
        if (emailAddressSubString.equals("guest")) {

            imageViewUpdateDetails.setVisibility(View.VISIBLE);
            textViewUpdateDetails.setVisibility(View.VISIBLE);
        }

        imageViewUpdateDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reauthenticateUser();
            }
        });

        textViewUpdateDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reauthenticateUser();
            }
        });

        window = getActivity().getWindow();

        toolbar = getActivity().findViewById(R.id.toolbar);
        textViewFragmentTitle = getActivity().findViewById(R.id.text_view_fragment_title);
        imageViewToolbarAdd = getActivity().findViewById(R.id.toolbar_add);
        bottomNav = getActivity().findViewById(R.id.bottom_nav);

        Switch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(darkModeOn);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    darkMode();
                    saveDarkModePreference();
                } else {
                    lightMode();
                    saveLightModePreference();
                }
            }
        });

        loadLocalProfilePic();

        return view;
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

        View container = getActivity().findViewById(R.id.container);

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewToolbarAdd, ContextCompat.getColorStateList(context, R.color.PrimaryDark));

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector));

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewDarkMode.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewSearchHistory.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewLogout.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewUpdateDetails.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        imageViewCameraIcon.setImageResource(R.drawable.camera_icon);
        ImageViewCompat.setImageTintList(clearSearchHistory, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        View container = getActivity().findViewById(R.id.container);

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewToolbarAdd, ContextCompat.getColorStateList(context, R.color.PrimaryLight));

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector_light));

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));

        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewDarkMode.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewSearchHistory.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewLogout.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewUpdateDetails.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        imageViewCameraIcon.setImageResource(R.drawable.camera_icon_dark);
        ImageViewCompat.setImageTintList(clearSearchHistory, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
    }

    private void startLogOut() {

        new AlertDialog.Builder(context)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logOut();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void logOut() {

        DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Tokens").document("User_Token");
        userTokenDocumentPath.delete();

        mFireBaseAuth.signOut();

        GlistDatabase glistDatabase = GlistDatabase.getInstance(context);
        glistDatabase.clearAllTables();

        Intent i = new Intent(context, SignInActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String folder = Environment.getExternalStorageDirectory() + File.separator + "gList/Images/";
            File directory = new File(folder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            try {
                FileOutputStream out = new FileOutputStream(folder + "profile_pic" + ".jpg");
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                }
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            String filePath = Environment.getExternalStorageDirectory() + File.separator + "gList/Images/" + "profile_pic" + ".jpg";

            File file = new File(filePath);
            if (file.exists()) {
                Picasso.get().load(file).into(imageViewProfilePic);
            }
        }
    }

    private void loadLocalProfilePic() {

        String filePath = Environment.getExternalStorageDirectory() + File.separator + "gList/Images/" + "profile_pic" + ".jpg";

        File file = new File(filePath);
        if (file.exists()) {
            Picasso.get().load(file).into(imageViewProfilePic);
        }
    }

    private void getPermissionToWriteStorage() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to Write to External Storage")
                .setMessage("This permission is needed in order save images taken with the camera when accessed by the App. Manually enable in Settings > Apps & notifications > gList > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "External storage permission granted", Toast.LENGTH_LONG, true).show();
                openFileChooser();
            } else {
                Toasty.error(context, "External storage permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void reauthenticateUser() {

        String guestPassword = md5(mCurrentUserEmail);

        AuthCredential credential = EmailAuthProvider
                .getCredential(mCurrentUserEmail, guestPassword);

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Intent i = new Intent(context, UpdateUserDetailsActivity.class);
                        i.putExtra("guestEmail", mCurrentUserEmail);
                        startActivity(i);
                        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
}
