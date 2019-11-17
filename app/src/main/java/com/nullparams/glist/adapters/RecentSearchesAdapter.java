package com.nullparams.glist.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nullparams.glist.R;
import com.nullparams.glist.database.RecentSearchesEntity;

import java.util.List;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesAdapter.RecentSearchesViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RecentSearchesEntity item);
    }

    private List<RecentSearchesEntity> recentSearchesList;
    private final OnItemClickListener listener;
    private boolean darkModeOn;
    private Context context;

    class RecentSearchesViewHolder extends RecyclerView.ViewHolder {

        TextView searchTermTextView;
        RelativeLayout card;

        RecentSearchesViewHolder(View itemView) {
            super(itemView);

            searchTermTextView = itemView.findViewById(R.id.search_term);
            card = itemView.findViewById(R.id.card);
        }

        public void bind(final RecentSearchesEntity item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public RecentSearchesAdapter(List<RecentSearchesEntity> recentSearchesList, SharedPreferences sharedPreferences, Context context, OnItemClickListener listener) {

        this.recentSearchesList = recentSearchesList;
        this.listener = listener;
        this.context = context;
        darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
    }

    @NonNull
    @Override
    public RecentSearchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_searches_item, parent, false);
        return new RecentSearchesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentSearchesViewHolder holder, int position) {
        holder.bind(recentSearchesList.get(position), listener);
        RecentSearchesEntity currentItem = recentSearchesList.get(position);

        if (darkModeOn) {
            holder.searchTermTextView.setTextColor(ContextCompat.getColor(context, R.color.SecondaryLight));
        }

        String searchTerm = currentItem.getSearchTerm();
        holder.searchTermTextView.setText(searchTerm);
    }

    @Override
    public int getItemCount() {
        return recentSearchesList.size();
    }
}
