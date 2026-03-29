package com.shruti.lofo.ui.Found;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shruti.lofo.R;
import com.shruti.lofo.data.model.Item;
import com.shruti.lofo.databinding.FragmentFoundBinding;
import com.shruti.lofo.ui.CreatePost.CreatePostFragment;
import com.shruti.lofo.ui.DashBoard.RecyclerRecentLoFoAdapter;

import java.util.ArrayList;

public class FoundFragment extends Fragment {

    private FragmentFoundBinding binding;
    private FoundViewModel foundViewModel;
    private RecyclerRecentLoFoAdapter adapter;


    private ArrayList<Item> allFoundItem = new ArrayList<>();
    private ArrayList<Item> displayList = new ArrayList<>();

    private  final String ALL_CATEGORIES_TEXT = "All Categories";


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoundBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        foundViewModel = new ViewModelProvider(requireActivity()).get(FoundViewModel.class);

        binding.foundRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.foundRecyclerView.setItemAnimator(null);
        adapter = new RecyclerRecentLoFoAdapter(requireContext(), displayList);
        binding.foundRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), FoundDetails.class);
            intent.putExtra("itemId", item.getItem_id());
            startActivity(intent);
        });

        getParentFragmentManager().setFragmentResultListener("refresh_feed", getViewLifecycleOwner(), (requestKey, result) -> {
            foundViewModel.fetchFoundItems(true);
        });
        setupFilterToggle();
        setupSpinner();
        setupOberserver();

        foundViewModel.fetchFoundItems(false);

        binding.addFound.setOnClickListener(v -> {
            CreatePostFragment dialog = new CreatePostFragment().newInstance("Found");
            dialog.show(getParentFragmentManager(), "CreatePostDialog");
        });

        return root;
    }

    private void setupFilterToggle () {
        binding.filterButton.setOnClickListener(v -> {
            if (binding.categorySpinner.getVisibility() == View.VISIBLE) {
                binding.categorySpinner.setVisibility(View.GONE);
                binding.categorySpinner.setSelection(0);
            } else {
                binding.categorySpinner.setVisibility(View.VISIBLE);
            }
        });
    }
    private void setupOberserver() {
        foundViewModel.getFoundItems().observe(getViewLifecycleOwner(), items -> {
            allFoundItem.clear();
            allFoundItem.addAll(items);
            filterByCategory(binding.categorySpinner.getSelectedItem().toString());

            String currentCategories = ALL_CATEGORIES_TEXT;
            if (binding.categorySpinner != null ) {
                currentCategories = binding.categorySpinner.getSelectedItem().toString();
            }

            filterByCategory(currentCategories);
        });

        foundViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.categories_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(spinnerAdapter);

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterByCategory(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void filterByCategory(String category) {
        displayList.clear();
        if(category.equals(ALL_CATEGORIES_TEXT)) {
            displayList.addAll(allFoundItem);
        } else  {
            for(Item item: allFoundItem) {
                if(item.getCategory() != null && item.getCategory().equalsIgnoreCase(category)){
                    displayList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    @Override
    public  void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
