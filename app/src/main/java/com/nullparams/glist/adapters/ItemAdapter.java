package com.nullparams.glist.adapters;

import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.glist.R;
import com.nullparams.glist.models.Item;
import com.nullparams.glist.models.User;

import android.content.Context;

public class ItemAdapter extends FirestoreRecyclerAdapter<Item, ItemAdapter.ItemHolder> {

    private OnItemClickListener listener;
    private boolean darkModeOn;
    private Context mContext;
    private String mCallingFragment;
    private FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
    private String mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore mFireBaseFireStore = FirebaseFirestore.getInstance();
    private String mUniqueId;

    public ItemAdapter(@NonNull FirestoreRecyclerOptions<Item> options, SharedPreferences sharedPreferences, Context context, String callingFragment, String uniqueId) {
        super(options);

        darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
        mContext = context;
        mCallingFragment = callingFragment;
        mUniqueId = uniqueId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemHolder holder, int position, @NonNull Item model) {

        if (model.getAmount().equals("0")) {
            holder.textViewItem.setText(model.getName());
        } else {
            holder.textViewItem.setText(model.getAmount() + "  " + model.getName());
        }

        holder.textViewCost.setText(model.getCost());

        if (darkModeOn) {
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewItem.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewCost.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.itemLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.SecondaryDark));
        }

        if (model.getStrike()) {
            holder.textViewItem.setPaintFlags(holder.textViewItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewItem.setPaintFlags(holder.textViewItem.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,
                parent, false);
        return new ItemHolder(v);
    }

    public void strikeItem(int position) {

        if (mCallingFragment.equals("ListsFragment")) {

            getSnapshots().getSnapshot(position).getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Item item = document.toObject(Item.class);

                            if (!item.getStrike()) {
                                getSnapshots().getSnapshot(position).getReference().update("strike", true);

                                long timeStamp = System.currentTimeMillis();

                                DocumentReference listRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(mUniqueId);
                                listRef.update("timeStamp", timeStamp);

                            } else {
                                notifyDataSetChanged();
                            }
                        }
                    }
                }
            });

        } else if (mCallingFragment.equals("SharedFragment")) {

            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(mUniqueId).collection("Participants")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    User user = document.toObject(User.class);
                                    String sharedUserId = user.getId();

                                    mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(mUniqueId).collection(mUniqueId).document(getSnapshots().getSnapshot(position).getReference().getId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Item item = document.toObject(Item.class);

                                                    if (!item.getStrike()) {
                                                        DocumentReference itemPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(mUniqueId).collection(mUniqueId).document(document.getId());
                                                        itemPath.update("strike", true);

                                                        long timeStamp = System.currentTimeMillis();

                                                        DocumentReference listRef = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(mUniqueId);
                                                        listRef.update("timeStamp", timeStamp);

                                                    } else {
                                                        notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
    }

    public void unStrikeItem(int position) {

        if (mCallingFragment.equals("ListsFragment")) {

            getSnapshots().getSnapshot(position).getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Item item = document.toObject(Item.class);

                            if (item.getStrike()) {
                                getSnapshots().getSnapshot(position).getReference().update("strike", false);

                                long timeStamp = System.currentTimeMillis();

                                DocumentReference listRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("My_lists").document(mUniqueId);
                                listRef.update("timeStamp", timeStamp);

                            } else {
                                notifyDataSetChanged();
                            }
                        }
                    }
                }
            });

        } else if (mCallingFragment.equals("SharedFragment")) {

            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Shared_lists").document(mUniqueId).collection("Participants")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    User user = document.toObject(User.class);
                                    String sharedUserId = user.getId();

                                    mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(mUniqueId).collection(mUniqueId).document(getSnapshots().getSnapshot(position).getReference().getId())
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Item item = document.toObject(Item.class);

                                                    if (item.getStrike()) {
                                                        DocumentReference itemPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(mUniqueId).collection(mUniqueId).document(document.getId());
                                                        itemPath.update("strike", false);

                                                        long timeStamp = System.currentTimeMillis();

                                                        DocumentReference listRef = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Shared_lists").document(mUniqueId);
                                                        listRef.update("timeStamp", timeStamp);

                                                    } else {
                                                        notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        TextView textViewItem;
        TextView textViewCost;
        ConstraintLayout itemLayout;

        public ItemHolder(View itemView) {
            super(itemView);

            textViewItem = itemView.findViewById(R.id.textview_name_item);
            textViewCost = itemView.findViewById(R.id.textview_cost);
            itemLayout = itemView.findViewById(R.id.container);

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
}
