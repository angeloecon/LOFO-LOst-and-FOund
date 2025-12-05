package com.shruti.lofo.ui.Found;

import android.content.Context;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;

public class FoundItemsAdapter extends FirestoreRecyclerAdapter<FoundItems, FoundItemsAdapter.ItemViewHolder> {

    private boolean showDeleteButton;
    private String category;

    // 1. Main Constructor (4 arguments - used when category filtering IS required)
    public FoundItemsAdapter(@NonNull FirestoreRecyclerOptions<FoundItems> options, Context context, String category,  boolean showDeleteButton) {
        super(options);
        this.category = category;
        this.showDeleteButton = showDeleteButton;
    }

    // ⭐ 2. Overloaded Constructor (3 arguments - used when no category filtering is needed, like in MyItems) ⭐
    public FoundItemsAdapter(@NonNull FirestoreRecyclerOptions<FoundItems> options, Context context, boolean showDeleteButton) {
        // Calls the main constructor, passing "" for the category (which means "show all" in your logic)
        this(options, context, "", showDeleteButton);
    }

    public void setCategory(String category) { this.category = category; }
    public String getCategory() { return category; }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull FoundItems item) {

        final String documentId = getSnapshots().getSnapshot(position).getId();

        // SAFETY FIX: Always get the currently attached context from the view.
        final Context safeContext = holder.itemView.getContext();

        // Check category filter
        if(category.isEmpty() || item.getCategory().equals(category))  {

            // Use the safeContext for Glide
            if (item.getImageURI() != null && !item.getImageURI().isEmpty() && !item.getImageURI().equals("no_image")) {
                Glide.with(safeContext)
                        .load(item.getImageURI())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.baseline_image_search_24)
                        .into(holder.itemImageView);
            } else {
                holder.itemImageView.setImageResource(R.drawable.placeholder_image);
            }

            // ... (Your existing TextView settings) ...
            holder.itemNameTextView.setText(item.getItemName());
            holder.finderNameTextView.setText(item.getFinderName());
            holder.description.setText(item.getDescription());
            holder.location.setText(item.getLocation());
            holder.date.setText(item.getDateFound());


            // Use the safeContext for the Intent launch
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(safeContext, FoundDetails.class);
                intent.putExtra("itemId", documentId);
                safeContext.startActivity(intent);
            });

            if (showDeleteButton) {
                holder.deleteButton.setVisibility(View.VISIBLE);

                holder.deleteButton.setOnClickListener(v -> {
                    // Use the safeContext for Toast
                    Utility.getCollectionReferrenceForFound().document(documentId).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(safeContext, "Deleted!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(safeContext, "Couldn't delete!", Toast.LENGTH_SHORT).show());
                });
            } else {
                holder.deleteButton.setVisibility(View.GONE);
            }

            // Ensure items that match the filter are visible
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        } else {
            // Hide items that don't match the category if the filter is active
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        // ... (Your existing ItemViewHolder definition) ...
        ImageView itemImageView;
        TextView itemNameTextView, finderNameTextView, description, location, date;
        ImageButton deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            finderNameTextView = itemView.findViewById(R.id.finderNameTextView);
            description= itemView.findViewById(R.id.item_description);
            location = itemView.findViewById((R.id.location));
            date = itemView.findViewById(R.id.dateFound);
            deleteButton= itemView.findViewById(R.id.deleteButton);
        }
    }
}