package com.shruti.lofo.ui.Lost;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;
import com.shruti.lofo.models.LostItems;

public class LostFragment extends Fragment implements LostItemsFragment.OnItemAddedListener {

    private RecyclerView recyclerView;
    private LostItemsAdapter adapter;
    private final String ALL_CATEGORIES_TEXT = "All Categories";
    private String selectedCategory = ALL_CATEGORIES_TEXT;
    private Spinner categorySpinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lost, container, false);

        recyclerView = root.findViewById(R.id.lostRecyclerView);
        categorySpinner = root.findViewById(R.id.categorySpinner);
        FloatingActionButton addBtn = root.findViewById(R.id.add_lost);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(null); // Disable animations to prevent IndexOutOfBoundsException

        // Floating action button to open LostItemsFragment
        addBtn.setOnClickListener(v -> {
            LostItemsFragment dialog = new LostItemsFragment();
            dialog.setOnItemAddedListener(this);
            dialog.show(getParentFragmentManager(), "form_dialog");
        });

        // Setup category spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.categories_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        categorySpinner.setSelection(0);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newCategory = parent.getItemAtPosition(position).toString();
                if (!newCategory.equals(selectedCategory)) {
                    selectedCategory = newCategory;
                    setupAdapter(); // Refresh adapter when category changes
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupAdapter(); // Initial adapter setup
        return root;
    }

    private void setupAdapter() {
        // Stop previous adapter if exists
        if (adapter != null) {
            adapter.stopListening();
            recyclerView.setAdapter(null); // Clear adapter to prevent inconsistencies
        }

        // Build Firestore query
        Query query = Utility.getCollectionReferrenceForItems2().orderBy("dateLost", Query.Direction.DESCENDING);
        if (!selectedCategory.equals(ALL_CATEGORIES_TEXT)) {
            query = query.whereEqualTo("category", selectedCategory);
        }

        FirestoreRecyclerOptions<LostItems> options = new FirestoreRecyclerOptions.Builder<LostItems>()
                .setQuery(query, LostItems.class)
                .build();

        adapter = new LostItemsAdapter(options, requireContext(), false);
        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override
    public void onItemAdded() {
        Utility.showToast(getContext(), "Item successfully posted!");
    }
}
