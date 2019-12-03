package com.nullparams.glist.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.ListActivity;
import com.nullparams.glist.R;
import com.nullparams.glist.ShareActivity;
import com.nullparams.glist.database.ListEntity;
import com.nullparams.glist.models.Item;
import com.nullparams.glist.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context mContext;
    private List<ListEntity> mList;
    private boolean darkModeOn;
    private FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        TextView textViewDate;
        TextView textViewFromEmail;
        TextView textViewVersion;
        ImageView imageViewShare;
        ConstraintLayout container;
        ConstraintLayout container2;
        View parentView;

        SearchViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewFromEmail = itemView.findViewById(R.id.fromUserEmail);
            textViewVersion = itemView.findViewById(R.id.revision);
            imageViewShare = itemView.findViewById(R.id.image_view_share);
            container = itemView.findViewById(R.id.container);
            container2 = itemView.findViewById(R.id.container2);
            parentView = itemView;
        }
    }

    public SearchAdapter(Context context, List<ListEntity> list, SharedPreferences sharedPreferences) {

        darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        mList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        ListEntity currentItem = mList.get(position);

        if (darkModeOn) {
            holder.textViewTitle.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewDate.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewFromEmail.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewVersion.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.container.setBackgroundResource(R.color.SecondaryDark);
            holder.container2.setBackgroundResource(R.drawable.rounded_edges_dark);
            ImageViewCompat.setImageTintList(holder.imageViewShare, ContextCompat.getColorStateList(mContext, R.color.PrimaryLight));
        }

        holder.textViewTitle.setText(currentItem.getTitle());
        holder.textViewDate.setText(getDate(currentItem.getTimeStamp(), "HH:mm\ndd-MM-yyyy"));
        holder.textViewVersion.setText("Version: " + currentItem.getVersion());

        holder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ListActivity.class);
                i.putExtra("version", currentItem.getVersion());
                i.putExtra("collectionId", currentItem.getCallingFragment());
                i.putExtra("uniqueId", currentItem.getId());
                i.putExtra("listName", currentItem.getTitle());

                if (currentItem.getCallingFragment().equals("My_lists")) {
                    i.putExtra("callingFragment", "ListsFragment");
                } else if (currentItem.getCallingFragment().equals("Shared_lists")) {
                    i.putExtra("callingFragment", "SharedFragment");
                } else if (currentItem.getCallingFragment().equals("Bin")) {
                    i.putExtra("callingFragment", "BinFragment");
                }

                mContext.startActivity(i);
            }
        });

        ArrayList<String> participantsEmailList = new ArrayList<>();

        db.collection("Users").document(current_user_id).collection(currentItem.getCallingFragment()).document(currentItem.getId()).collection("Participants")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                User user = document.toObject(User.class);
                                participantsEmailList.add(user.getEmailAddress());
                            }

                            String emailAddressesString = participantsEmailList.toString();
                            String emailAddressesString2 = emailAddressesString.replace("[", "");
                            String emailAddressesString3 = emailAddressesString2.replace("]", "");
                            String emailAddressesString4 = emailAddressesString3.replaceAll(",", "\n");
                            String emailAddressesString5 = emailAddressesString4.replace(" ", "");
                            holder.textViewFromEmail.setText(emailAddressesString5);
                        }
                    }
                });

        holder.imageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("Users").document(current_user_id).collection(currentItem.getCallingFragment()).document(currentItem.getId()).collection(currentItem.getId())
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
                                        i.putExtra("collectionId", currentItem.getCallingFragment());
                                        i.putExtra("uniqueId", currentItem.getId());
                                        i.putExtra("listName", currentItem.getTitle());
                                        i.putExtra("itemArrayList", itemArrayList);
                                        i.putExtra("newUniqueId", newUniqueId);
                                        mContext.startActivity(i);
                                    }
                                }
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static String getDate(long milliSeconds, String dateFormat) {

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
