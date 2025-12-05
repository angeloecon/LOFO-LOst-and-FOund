package com.shruti.lofo.ui.MyItems;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;
import com.shruti.lofo.models.LostItems;
import com.shruti.lofo.ui.Found.FoundItems;
import com.shruti.lofo.ui.Found.FoundItemsAdapter;
import com.shruti.lofo.ui.Lost.LostItemsAdapter;

public class MyItems extends Fragment {

    private LostItemsAdapter lostAdapter;
    private FoundItemsAdapter foundAdapter;
    private String userId;
    private RecyclerView lostRecyclerView;
    private RecyclerView foundRecyclerView;
    private TextView titleTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_items, container, false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = (currentUser != null) ? currentUser.getUid() : "";

        lostRecyclerView = root.findViewById(R.id.lostRecyclerView);
        foundRecyclerView = root.findViewById(R.id.foundRecyclerView);
        titleTextView = root.findViewById(R.id.text);

        lostRecyclerView.setItemAnimator(null);
        foundRecyclerView.setItemAnimator(null);

        setupRecyclerView();
        return root;
    }

    void setupRecyclerView() {
        final boolean showDeleteButton = true;

        if (!userId.isEmpty()) {
            titleTextView.setText("My LoFo");

            // ---------------------------
            // RESET BEFORE SETTING NEW ADAPTERS  <<< IMPORTANT FIX
            // ---------------------------
            if (lostAdapter != null) lostAdapter.stopListening();
            if (foundAdapter != null) foundAdapter.stopListening();

            lostRecyclerView.setAdapter(null);
            foundRecyclerView.setAdapter(null);

            // --------------------- LOST ----------------------
            Query lostQuery = Utility.getCollectionReferrenceForItems2()
                    .whereEqualTo("userId", userId);

            FirestoreRecyclerOptions<LostItems> lostOptions =
                    new FirestoreRecyclerOptions.Builder<LostItems>()
                            .setQuery(lostQuery, LostItems.class)
                            .build();

            lostRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            lostAdapter = new LostItemsAdapter(lostOptions, requireContext(), showDeleteButton);
            lostRecyclerView.setAdapter(lostAdapter);

            // --------------------- FOUND ----------------------
            Query foundQuery = Utility.getCollectionReferrenceForFound()
                    .whereEqualTo("finderId", userId);

            FirestoreRecyclerOptions<FoundItems> foundOptions =
                    new FirestoreRecyclerOptions.Builder<FoundItems>()
                            .setQuery(foundQuery, FoundItems.class)
                            .build();

            foundRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            foundAdapter = new FoundItemsAdapter(foundOptions, requireContext(), showDeleteButton);
            foundRecyclerView.setAdapter(foundAdapter);

            // Start listeners
            lostAdapter.startListening();
            foundAdapter.startListening();

        } else {
            Utility.showToast(getContext(), "Please log in to view your items.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (lostAdapter != null) lostAdapter.startListening();
        if (foundAdapter != null) foundAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (lostAdapter != null) lostAdapter.stopListening();
        if (foundAdapter != null) foundAdapter.stopListening();
    }
}
