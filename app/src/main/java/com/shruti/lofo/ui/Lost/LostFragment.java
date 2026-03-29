package com.shruti.lofo.ui.Lost;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shruti.lofo.R;
import com.shruti.lofo.data.model.Item;
import com.shruti.lofo.databinding.FragmentLostBinding;
import com.shruti.lofo.ui.CreatePost.CreatePostFragment;
import com.shruti.lofo.ui.DashBoard.RecyclerRecentLoFoAdapter;

import java.util.ArrayList;

public class LostFragment extends Fragment {

    private FragmentLostBinding binding;
    private LostViewModel lostViewModel;
    private RecyclerRecentLoFoAdapter adapter;


    private ArrayList<Item> allLostItems = new ArrayList<>();
    private ArrayList<Item> displayList = new ArrayList<>();
    private final String ALL_CATEGORIES_TEXT = "All Categories";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lostViewModel = new ViewModelProvider(requireActivity()).get(LostViewModel.class);

        binding.lostRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.lostRecyclerView.setItemAnimator(null);
        adapter = new RecyclerRecentLoFoAdapter(requireContext(), displayList);
        binding.lostRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), LostDetails.class);
            intent.putExtra("itemId", item.getItem_id());
            startActivity(intent);
        });

        setupSpinner();
        setupObservers();

        getParentFragmentManager().setFragmentResultListener("refresh_feed", getViewLifecycleOwner(), (requestKey, result) -> {
            lostViewModel.fetchLostItems(true);
        });

        lostViewModel.fetchLostItems(false);

        binding.addLost.setOnClickListener(v -> {
            CreatePostFragment dialog = new CreatePostFragment().newInstance("Lost");
            dialog.show(getParentFragmentManager(), "CreatePostDialog");
        });

        return root;
    }

    private void setupObservers() {
        lostViewModel.getLostItem().observe(getViewLifecycleOwner(), items -> {
            allLostItems.clear();
            allLostItems.addAll(items);

            String currentCategories = ALL_CATEGORIES_TEXT;
            if(binding.categorySpinner.getSelectedItem() != null) {
                currentCategories = binding.categorySpinner.getSelectedItem().toString();
            }

            filterByCategory(currentCategories);
        });

        lostViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });{
        }
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.categories_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(spinnerAdapter);

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 filterByCategory(parent.getItemAtPosition(position).toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Local Filtering
    private void filterByCategory (String category) {
        displayList.clear();
        if(category.equals(ALL_CATEGORIES_TEXT)) {
            displayList.addAll(allLostItems);
        } else {
            for (Item item : allLostItems){
                if(item.getCategory() != null && item.getCategory().equalsIgnoreCase(category)) {
                    displayList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        binding = null;
    }
}
