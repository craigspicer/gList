package com.nullparams.glist.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.R;
import com.nullparams.glist.ShareActivity;
import com.nullparams.glist.models.Item;
import com.nullparams.glist.models.List;
import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class ListsAdapter extends FirestoreRecyclerAdapter<List, ListsAdapter.ListHolder> {

    private OnItemClickListener listener;
    private boolean darkModeOn;
    private Context mContext;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String mCallingFragment;
    private Activity mActivity;

    public ListsAdapter(@NonNull FirestoreRecyclerOptions<List> options, SharedPreferences sharedPreferences, Context context, String callingFragment, Activity activity) {
        super(options);
        darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        mContext = context;
        mCallingFragment = callingFragment;
        mActivity = activity;
    }

    @Override
    protected void onBindViewHolder(@NonNull ListHolder holder, int position, @NonNull List model) {

        if (darkModeOn) {
            holder.textViewTitle.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewList.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewDate.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewFromEmail.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewVersion.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.container.setBackgroundResource(R.color.SecondaryDark);
            holder.container2.setBackgroundResource(R.drawable.rounded_edges_dark);
            ImageViewCompat.setImageTintList(holder.imageViewShare, ContextCompat.getColorStateList(mContext, R.color.PrimaryLight));
        }

        holder.textViewTitle.setText(model.getTitle());
        holder.textViewDate.setText(getDate(model.getTimeStamp(), "dd-MM-yyyy"));
        holder.textViewFromEmail.setText(model.getFromEmailAddress());
        holder.textViewVersion.setText("Version: " + model.getVersion());

        /*db.collection("Users").document(current_user_id).collection(mCallingFragment).document(model.getId()).collection(model.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            ArrayList<String> itemArrayList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Item item = document.toObject(Item.class);
                                String setItem;
                                if (item.getAmount().equals("0")) {
                                    setItem = item.getName();
                                } else {
                                    setItem = item.getAmount() + " " + item.getName();
                                }
                                itemArrayList.add(setItem);
                            }
                            String formattedString = itemArrayList.toString().replace("[", "");
                            String formattedString2 = formattedString.replace("]", "");
                            holder.textViewList.setText(formattedString2);
                        }
                    }
                });*/

        holder.imageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("Users").document(current_user_id).collection(mCallingFragment).document(model.getId()).collection(model.getId())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {

                                    ArrayList<String> itemArrayList = new ArrayList<>();

                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        Item item = document.toObject(Item.class);
                                        String setItem;
                                        if (item.getAmount().equals("0")) {
                                            setItem = item.getName();
                                        } else {
                                            setItem = item.getAmount() + " " + item.getName();
                                        }
                                        itemArrayList.add(setItem);
                                    }
                                    if (itemArrayList.isEmpty()) {
                                        Toasty.info(mContext, "This list is empty", Toast.LENGTH_LONG, true).show();

                                    } else {

                                        String newUniqueId = UUID.randomUUID().toString();

                                        Intent i = new Intent(mContext, ShareActivity.class);
                                        i.putExtra("collectionId", mCallingFragment);
                                        i.putExtra("uniqueId", model.getId());
                                        i.putExtra("listName", model.getTitle());
                                        i.putExtra("itemArrayList", itemArrayList);
                                        i.putExtra("newUniqueId", newUniqueId);
                                        mContext.startActivity(i);
                                        mActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                    }
                                }
                            }
                        });
            }
        });
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,
                parent, false);
        return new ListHolder(v);
    }

    //Delete
    public void deleteItem(int position) {

        String id = getSnapshots().getSnapshot(position).getReference().getId();

        CollectionReference deleteReference = db.collection("Users").document(current_user_id).collection("Bin").document(id).collection(id);
        deleteReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference documentReference = db.collection("Users").document(current_user_id).collection("Bin").document(id).collection(id).document(document.getId());
                                documentReference.delete();
                            }
                        }
                    }
                });

        getSnapshots().getSnapshot(position).getReference().delete();
    }

    //Move - My_lists to Bin
    public void moveItem1(int position) {

        getSnapshots().getSnapshot(position).getReference();

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();

        CollectionReference fromFolder = db.collection("Users").document(current_user_id).collection("My_lists").document(id).collection(id);
        fromFolder.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference fromReference = db.collection("Users").document(current_user_id).collection("My_lists").document(id).collection(id).document(document.getId());
                                DocumentReference toReference = db.collection("Users").document(current_user_id).collection("Bin").document(id).collection(id).document(document.getId());
                                moveFirestoreDocument(fromReference, toReference);
                            }
                        }
                    }
                });

        DocumentReference from = snapshot.getReference();
        DocumentReference to = db.collection("Users").document(current_user_id).collection("Bin").document(id);
        moveFirestoreDocument(from, to);
    }

    //Move - Bin to Notebook
    public void moveItem2(int position) {

        getSnapshots().getSnapshot(position).getReference();

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();

        CollectionReference fromFolder = db.collection("Users").document(current_user_id).collection("Bin").document(id).collection(id);
        fromFolder.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference fromReference = db.collection("Users").document(current_user_id).collection("Bin").document(id).collection(id).document(document.getId());
                                DocumentReference toReference = db.collection("Users").document(current_user_id).collection("My_lists").document(id).collection(id).document(document.getId());
                                moveFirestoreDocument(fromReference, toReference);
                            }
                        }
                    }
                });

        DocumentReference from = db.collection("Users").document(current_user_id).collection("Bin").document(id);
        DocumentReference to = db.collection("Users").document(current_user_id).collection("My_lists").document(id);
        moveFirestoreDocument(from, to);
    }

    //Move - Shared_lists to Bin
    public void moveItem3(int position) {

        getSnapshots().getSnapshot(position).getReference();

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();

        CollectionReference fromFolder = db.collection("Users").document(current_user_id).collection("Shared_lists").document(id).collection(id);
        fromFolder.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference fromReference = db.collection("Users").document(current_user_id).collection("Shared_lists").document(id).collection(id).document(document.getId());
                                DocumentReference toReference = db.collection("Users").document(current_user_id).collection("Bin").document(id).collection(id).document(document.getId());
                                moveFirestoreDocument(fromReference, toReference);
                            }
                        }
                    }
                });

        DocumentReference from = snapshot.getReference();
        DocumentReference to = db.collection("Users").document(current_user_id).collection("Bin").document(id);
        moveFirestoreDocument(from, to);
    }

    class ListHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        TextView textViewList;
        TextView textViewDate;
        TextView textViewFromEmail;
        TextView textViewVersion;
        ImageView imageViewShare;
        ConstraintLayout container;
        ConstraintLayout container2;

        public ListHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewList = itemView.findViewById(R.id.text_view_list);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewFromEmail = itemView.findViewById(R.id.fromUserEmail);
            textViewVersion = itemView.findViewById(R.id.revision);
            imageViewShare = itemView.findViewById(R.id.image_view_share);
            container = itemView.findViewById(R.id.container);
            container2 = itemView.findViewById(R.id.container2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static String getDate(long milliSeconds, String dateFormat) {

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    //Move method
    public void moveFirestoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        toPath.set(document.getData())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        fromPath.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                }
            }
        });
    }
}