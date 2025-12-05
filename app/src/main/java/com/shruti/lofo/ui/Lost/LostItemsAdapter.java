package com.shruti.lofo.ui.Lost;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.shruti.lofo.R;
import com.shruti.lofo.models.LostItems;

public class LostItemsAdapter extends FirestoreRecyclerAdapter<LostItems, LostItemsAdapter.ItemViewHolder> {

    private final boolean showDelete;
    private final Context context;

    public LostItemsAdapter(@NonNull FirestoreRecyclerOptions<LostItems> options, Context context, boolean showDelete) {
        super(options);
        this.context = context;
        this.showDelete = showDelete;
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull LostItems item) {
        Context viewContext = holder.itemView.getContext();

        if (item.getImageURI() != null && !item.getImageURI().equals("no_image")) {
            Glide.with(viewContext)
                    .load(item.getImageURI())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.baseline_image_search_24)
                    .into(holder.itemImageView);
        } else {
            holder.itemImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemNameTextView.setText(item.getItemName());
        holder.ownerNameTextView.setText(item.getOwnerName());
        holder.description.setText(item.getDescription());
        holder.location.setText(item.getLocation());
        holder.dateContentTextView.setText(item.getDateLost());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(viewContext, LostDetails.class);
            intent.putExtra("itemId", getSnapshots().getSnapshot(position).getId());
            viewContext.startActivity(intent);
        });

        if (showDelete) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                getSnapshots().getSnapshot(position).getReference().delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(viewContext, "Item Deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            Log.e("LostAdapter", "Error deleting item: " + e.getMessage());
                            Toast.makeText(viewContext, "Error deleting item", Toast.LENGTH_SHORT).show();
                        });
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lost_item_card, parent, false));
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemNameTextView, ownerNameTextView, description, location, dateContentTextView;
        ImageButton deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            ownerNameTextView = itemView.findViewById(R.id.ownerNameTextView);
            description = itemView.findViewById(R.id.item_description);
            location = itemView.findViewById(R.id.location);
            dateContentTextView = itemView.findViewById(R.id.dateLost);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
