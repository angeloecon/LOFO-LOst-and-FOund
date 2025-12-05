package com.shruti.lofo.ui.DashBoard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shruti.lofo.R;
import com.shruti.lofo.databinding.FragmentDashboardBinding;
import com.shruti.lofo.ui.Found.FoundDetails;
import com.shruti.lofo.ui.Lost.LostDetails;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class DashBoardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private ArrayList<DashBoardViewModel> arr_recent_lofo;
    private RecyclerRecentLoFoAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // --- Image Slider Setup ---
        ImageSlider imageSlider = root.findViewById(R.id.imageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();

        slideModels.add(new SlideModel(R.drawable.dashboard_img1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.dashboard_img2, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        // --- RecyclerView Setup ---
        RecyclerView recentLostFoundList = root.findViewById(R.id.recent_lost_found_list);
        arr_recent_lofo = new ArrayList<>();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false);
        recentLostFoundList.setLayoutManager(gridLayoutManager);

        // NOTE: Ensure RecyclerRecentLoFoAdapter implements the OnItemClickListener interface
        adapter = new RecyclerRecentLoFoAdapter(requireContext(), arr_recent_lofo);
        recentLostFoundList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchDataAndPopulateList();
        setupItemClickListener();
        fetchUserName(root);

        return root;
    }

    private void fetchDataAndPopulateList() {
        // Query the 'lostItems' collection
        Query lostItemsQuery = db.collection("lostItems");
        // Query the 'foundItems' collection
        Query foundItemsQuery = db.collection("foundItems");

        // Execute the queries for both lost and found items
        lostItemsQuery.get().addOnSuccessListener(lostItemsSnapshot -> {
            foundItemsQuery.get().addOnSuccessListener(foundItemsSnapshot -> {

                List<DocumentSnapshot> mergedItems = new ArrayList<>();
                mergedItems.addAll(lostItemsSnapshot.getDocuments());
                mergedItems.addAll(foundItemsSnapshot.getDocuments());

                // Sort the merged items by date in descending order
                Collections.sort(mergedItems, (o1, o2) -> {
                    String dateLostString1 = o1.getString("dateLost");
                    String dateFoundString1 = o1.getString("dateFound");
                    String dateLostString2 = o2.getString("dateLost");
                    String dateFoundString2 = o2.getString("dateFound");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date date1 = null;
                    Date date2 = null;

                    // Parse Date 1 (Lost or Found)
                    String dateString1 = (dateLostString1 != null) ? dateLostString1 : dateFoundString1;
                    if (dateString1 != null) {
                        try {
                            date1 = dateFormat.parse(dateString1);
                        } catch (ParseException e) {
                            Log.e("DashBoard", "Date parse failed for item 1: " + dateString1, e);
                        }
                    }

                    // Parse Date 2 (Lost or Found)
                    String dateString2 = (dateLostString2 != null) ? dateLostString2 : dateFoundString2;
                    if (dateString2 != null) {
                        try {
                            date2 = dateFormat.parse(dateString2);
                        } catch (ParseException e) {
                            Log.e("DashBoard", "Date parse failed for item 2: " + dateString2, e);
                        }
                    }

                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1); // Sort descending (newest first)
                    }
                    return 0;
                });

                // Limit the list to 10 items
                if (mergedItems.size() > 10) {
                    mergedItems = mergedItems.subList(0, 10);
                }

                arr_recent_lofo.clear(); // Clear old data before adding new

                // Now, process the top 10 most recent items
                for (DocumentSnapshot item : mergedItems) {
                    DashBoardViewModel lofo = item.toObject(DashBoardViewModel.class);
                    if (lofo != null) {

                        // 1. Set the Document ID (CRITICAL FIX)
                        lofo.setDocumentId(item.getId());

                        // 2. Set the 'tag' based on the item's collection reference
                        if (item.getReference().getParent().getId().equalsIgnoreCase("lostItems")) {
                            lofo.setTag("lost");
                        } else if (item.getReference().getParent().getId().equalsIgnoreCase("foundItems")) {
                            lofo.setTag("found");
                        }

                        arr_recent_lofo.add(lofo);
                    }
                }
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void setupItemClickListener() {
        adapter.setOnItemClickListener(new RecyclerRecentLoFoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DashBoardViewModel item) {
                // Use the Document ID we manually attached in the processing loop
                String documentId = item.getDocumentId();

                Intent intent;
                if (item.getTag().equalsIgnoreCase("lost")) {
                    intent = new Intent(requireContext(), LostDetails.class);
                    intent.putExtra("itemId", documentId);
                } else {
                    intent = new Intent(requireContext(), FoundDetails.class);
                    intent.putExtra("itemId", documentId);
                }
                startActivity(intent);
            }
        });
    }

    private void fetchUserName(View root) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView userName = root.findViewById(R.id.userName);

        if (user != null) {
            String email = user.getEmail();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersCollectionRef = db.collection("users");
            Query query = usersCollectionRef.whereEqualTo("email", email);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name = document.getString("name");
                        if (name != null) {
                            userName.setText(name);
                        }
                    }
                } else {
                    Log.d("FirebaseDebug", "Error getting documents: ", task.getException());
                }
            });
        } else {
            Log.d("FirebaseDebug", "No user currently logged in.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}