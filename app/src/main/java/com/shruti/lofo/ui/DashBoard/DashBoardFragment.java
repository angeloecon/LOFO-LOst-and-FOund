package com.shruti.lofo.ui.DashBoard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.shruti.lofo.R;
import com.shruti.lofo.data.model.Item;
import com.shruti.lofo.databinding.FragmentDashboardBinding;
import com.shruti.lofo.ui.Found.FoundDetails;
import com.shruti.lofo.ui.Lost.LostDetails;

import java.util.ArrayList;

public class DashBoardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashBoardViewModel dashBoardViewModel;
    private RecyclerRecentLoFoAdapter adapter;
    private ArrayList<Item> arr_recent_lofo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dashBoardViewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);

        setupImageSlider();
        setupRecyclerView();
        setupObservers();

        getParentFragmentManager().setFragmentResultListener("refresh_feed", getViewLifecycleOwner(), (requestKey, result) -> {
            dashBoardViewModel.fetchFeed(true);
        });

        dashBoardViewModel.fetchFeed(false);

        setupItemClickListener();
        fetchUserName();

        return root;
    }

    private void setupImageSlider() {
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.dashboard_img1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.dashboard_img2, ScaleTypes.FIT));

        binding.imageSlider.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void setupRecyclerView() {
        arr_recent_lofo = new ArrayList<>();
        adapter = new RecyclerRecentLoFoAdapter(requireContext(), arr_recent_lofo);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false);

        binding.recentLostFoundList.setLayoutManager(gridLayoutManager);
        binding.recentLostFoundList.setAdapter(adapter);
    }

    private void setupObservers() {
        dashBoardViewModel.getRecentItem().observe(getViewLifecycleOwner(), items -> {
            arr_recent_lofo.clear();
            arr_recent_lofo.addAll(items);
            adapter.notifyDataSetChanged();
        });

        // Listen for errors
        dashBoardViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupItemClickListener() {
        adapter.setOnItemClickListener(item -> {
            Intent intent;
            int itemId = item.getItem_id();

            if (item.getType().equalsIgnoreCase("Lost")) {
                intent = new Intent(requireContext(), LostDetails.class);
            } else {
                intent = new Intent(requireContext(), FoundDetails.class);
            }

            intent.putExtra("itemId", itemId);
            startActivity(intent);
        });
    }

    private void fetchUserName() {
        binding.userName.setText("Campus User");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}