package com.shruti.lofo.ui.Found;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog; // ADDED
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // Added for convenience

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.shruti.lofo.CloudinaryConfig; // ADDED
import com.shruti.lofo.Utility;
import com.shruti.lofo.R;

// Cloudinary Imports
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.util.Calendar;

public class FoundItemsFragment extends DialogFragment {
    private ImageButton datePickerButton, timePickerButton; // ADDED timePickerButton
    private TextView dateEdit, timeEdit; // ADDED timeEdit
    private Spinner categorySpinner;
    private Button upload, submitButton; // submitButton defined as a field
    private EditText description, location, itemNameEditText; // Defined as fields
    private Uri imageUri;

    final int REQ_CODE=1000;
    private int mYear, mMonth, mDay, mHour, mMinute; // Added mHour, mMinute
    String date= null;
    String time = null; // ADDED time

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_found_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        itemNameEditText = view.findViewById(R.id.item_name_edittext); // Need to initialize item name field
        description = view.findViewById(R.id.description);
        location = view.findViewById(R.id.location);
        datePickerButton = view.findViewById(R.id.datePickerButton);
        dateEdit = view.findViewById(R.id.selectedDateEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        upload = view.findViewById(R.id.uploadImageButton);
        submitButton = view.findViewById(R.id.submit_button);

        // Time Picker Initialization (Must be added to fragment_found_items.xml)
        timePickerButton = view.findViewById(R.id.timePickerButton);
        timeEdit = view.findViewById(R.id.selectedTimeEditText);


        datePickerButton.setOnClickListener(v -> showDatePicker());
        timePickerButton.setOnClickListener(v -> showTimePicker()); // Set listener for time picker

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        final String[] selectedCategory = new String[1];
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory[0] = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory[0] = null;
            }
        });

        // Image Upload
        upload.setOnClickListener(v -> {
            Intent iGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(iGallery, REQ_CODE);
        });

        // Submission Logic
        submitButton.setOnClickListener(v -> submitItem(selectedCategory[0]));
    }

    private void submitItem(String selectedCategory) {
        String itemName = itemNameEditText.getText().toString().trim();
        String desc = description.getText().toString().trim();
        String loc = location.getText().toString().trim();

        // Validation
        if (itemName.isEmpty()) { Utility.showToast(getContext(), "Name cannot be empty"); return; }
        if (selectedCategory == null || selectedCategory.isEmpty()) { Utility.showToast(getContext(), "Please select a category"); return; }
        if (date == null) { Utility.showToast(getContext(), "Please select the date found"); showDatePicker(); return; }
        if (time == null) { Utility.showToast(getContext(), "Please select the time found"); showTimePicker(); return; } // NEW TIME VALIDATION
        if (loc.isEmpty()) { Utility.showToast(getContext(), "Please provide location"); return; }
        if (desc.isEmpty()) { Utility.showToast(getContext(), "Please add description"); return; }
        if(imageUri == null){ Utility.showToast(getContext(),"Please upload the image of the thing you found"); return; }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) { Utility.showToast(getContext(), "User not logged in"); return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Fetch user data
        db.collection("users")
                .whereEqualTo("email", currentUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                        FoundItems foundItem = new FoundItems();
                        foundItem.setItemName(itemName);
                        foundItem.setCategory(selectedCategory);
                        foundItem.setDateFound(date);
                        foundItem.setTimeFound(time); // SET TIME FOUND
                        foundItem.setLocation(loc);
                        foundItem.setDescription(desc);

                        // Set user details
                        foundItem.setFinderName(doc.getString("name"));
                        foundItem.setPhnum(doc.getString("phone")); // Storing phone as String
                        foundItem.setEmail(currentUser.getEmail());
                        foundItem.setFinderId(currentUser.getUid());

                        // 2. Upload image and save post
                        uploadImageAndSavePost(foundItem);
                    } else {
                        Utility.showToast(getContext(), "Failed to fetch user info.");
                    }
                });
    }

    // --- Cloudinary Upload Logic ---
    private void uploadImageAndSavePost(FoundItems itemData) {

        if (imageUri == null) return;

        final String CLOUD_NAME = CloudinaryConfig.CLOUD_NAME;
        // IMPORTANT: Replace "dny4hxil" with YOUR actual UNSIGNED upload preset ID from Cloudinary!
        final String UNSIGNED_PRESET_ID = "dny4hxil";

        String uploadUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        Utility.showToast(getContext(), "Uploading image to Cloudinary...");
        submitButton.setEnabled(false);

        try {
            params.put("upload_preset", UNSIGNED_PRESET_ID);
            params.put("file", requireActivity().getContentResolver().openInputStream(imageUri), "file.jpg");

        } catch (FileNotFoundException e) {
            Utility.showToast(getContext(), "Error: Image stream failed.");
            submitButton.setEnabled(true);
            return;
        }

        client.post(uploadUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                submitButton.setEnabled(true);
                try {
                    JSONObject jsonResponse = new JSONObject(new String(responseBody));
                    String imageUrl = jsonResponse.getString("secure_url");

                    savePostDataToFirestore(imageUrl, itemData);

                } catch (Exception e) {
                    Utility.showToast(getContext(), "Upload successful but failed to process response.");
                    Log.e(TAG, "JSON Processing Error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                submitButton.setEnabled(true);
                Utility.showToast(getContext(), "Image upload failed. Status: " + statusCode);
                Log.e(TAG, "Cloudinary Failure: " + statusCode, error);
            }
        });
    }

    private void savePostDataToFirestore(String imageUrl, FoundItems itemData) {
        itemData.setImageURI(imageUrl);

        Utility.getCollectionReferrenceForFound().add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Utility.showToast(getContext(), "Found Item added successfully!");
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getContext(), "Failed to save post data.");
                });
    }

    // --- Date/Time Pickers ---
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR); mMonth = c.get(Calendar.MONTH); mDay = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, year, monthOfYear, dayOfMonth) -> {
            mYear = year; mMonth = monthOfYear; mDay = dayOfMonth; updateDateButton();
        }, mYear, mMonth, mDay).show();
    }

    // NEW TIME PICKER
    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY); mMinute = c.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            mHour = hour; mMinute = minute; updateTimeButton();
        }, mHour, mMinute, false).show();
    }

    private void updateDateButton() {
        date = mDay + "/" + (mMonth + 1) + "/" + mYear;
        dateEdit.setText(date);
    }

    // NEW TIME FORMATTER
    private void updateTimeButton() {
        String am_pm = (mHour < 12) ? "AM" : "PM";
        int hour = mHour % 12; if (hour == 0) hour = 12;
        time = hour + ":" + String.format("%02d", mMinute) + " " + am_pm;
        timeEdit.setText(time);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQ_CODE && data != null){
            imageUri = data.getData();
            if (imageUri != null) {
                upload.setText("Image Added");
            } else {
                Utility.showToast(getContext(), "Failed to get image URI.");
            }
        }
    }
}