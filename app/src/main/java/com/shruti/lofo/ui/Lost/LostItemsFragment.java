package com.shruti.lofo.ui.Lost;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shruti.lofo.CloudinaryConfig;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;
import com.shruti.lofo.models.LostItems;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class LostItemsFragment extends DialogFragment {

    public interface OnItemAddedListener {
        void onItemAdded();
    }

    private OnItemAddedListener listener;

    private ImageButton datePickerButton, timePickerButton;
    private TextView dateEdit, timeEdit;
    private Spinner categorySpinner;
    private EditText description, location, itemNameEditText;
    private Button upload, submitButton;

    private Uri imageUri = null;
    private String selectedCategoryString = null;

    private int mYear, mMonth, mDay, mHour, mMinute;
    private String date, time;

    private static final String TAG = "LostItemsFragment";
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public void setOnItemAddedListener(OnItemAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();

                        if (imageUri != null) {
                            Log.i(TAG, "Image URI received successfully: " + imageUri.toString());
                            if (upload != null) upload.setText(getString(R.string.image_added));
                        } else {
                            Utility.showToast(getContext(), "Failed to get image URI.");
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getDialog() != null) {
            Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawableResource(R.drawable.border_shape);
        }
        return inflater.inflate(R.layout.fragment_lost_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemNameEditText = view.findViewById(R.id.item_name_edittext);
        description = view.findViewById(R.id.description);
        location = view.findViewById(R.id.location);
        datePickerButton = view.findViewById(R.id.datePickerButton);
        timePickerButton = view.findViewById(R.id.timePickerButton);
        dateEdit = view.findViewById(R.id.selectedDateEditText);
        timeEdit = view.findViewById(R.id.selectedTimeEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        upload = view.findViewById(R.id.uploadImageButton);
        submitButton = view.findViewById(R.id.submit_button);

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        datePickerButton.setOnClickListener(v -> showDatePicker());
        timePickerButton.setOnClickListener(v -> showTimePicker());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryString = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryString = null;
            }
        });

        upload.setOnClickListener(v -> {
            Intent iGallery = new Intent(Intent.ACTION_GET_CONTENT);
            iGallery.setType("image/*");
            iGallery.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(iGallery);
        });

        submitButton.setOnClickListener(v -> submitItem());
    }

    private void submitItem() {
        String itemName = itemNameEditText.getText().toString().trim();
        String desc = description.getText().toString().trim();
        String loc = location.getText().toString().trim();
        String selectedCategory = this.selectedCategoryString;

        if (itemName.isEmpty()) { Utility.showToast(getContext(), "Name cannot be empty"); return; }
        if (selectedCategory == null || selectedCategory.isEmpty() || selectedCategory.equals(getString(R.string.category_default))) {
            Utility.showToast(getContext(), "Please select a category"); return;
        }
        if (date == null) { Utility.showToast(getContext(), "Please select date"); return; }
        if (time == null) { Utility.showToast(getContext(), "Please select time"); return; }
        if (loc.isEmpty()) { Utility.showToast(getContext(), "Please provide location"); return; }
        if (desc.isEmpty()) { Utility.showToast(getContext(), "Please add description"); return; }
        if (imageUri == null) { Utility.showToast(getContext(), "Please select an image to upload"); return; }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) { Utility.showToast(getContext(), "User not logged in"); return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", currentUser.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);

                        LostItems lostItem = new LostItems();
                        lostItem.setItemName(itemName);
                        lostItem.setCategory(selectedCategory);
                        lostItem.setDateLost(date);
                        lostItem.setTimeLost(time);
                        lostItem.setLocation(loc);
                        lostItem.setDescription(desc);
                        lostItem.setOwnerName(doc.getString("name"));

                        try {
                            String phoneStr = doc.getString("phone");
                            lostItem.setPhnum(phoneStr != null && !phoneStr.isEmpty() ? Long.parseLong(phoneStr) : 0L);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Phone number format exception: " + e.getMessage());
                            lostItem.setPhnum(0L);
                        }

                        lostItem.setEmail(currentUser.getEmail());
                        lostItem.setUserId(currentUser.getUid());

                        uploadImageAndSavePost(lostItem);

                    } else {
                        Utility.showToast(getContext(), "Failed to fetch user info");
                    }
                });
    }

    private void uploadImageAndSavePost(LostItems itemData) {
        if (imageUri == null) {
            Utility.showToast(getContext(), "Image URI is missing.");
            return;
        }

        String uploadUrl = "https://api.cloudinary.com/v1_1/" + CloudinaryConfig.CLOUD_NAME + "/image/upload";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        final String UNSIGNED_PRESET_ID = "dny4hxil";

        Utility.showToast(getContext(), "Uploading image...");
        submitButton.setEnabled(false);

        try {
            params.put("upload_preset", UNSIGNED_PRESET_ID);
            params.put("file", requireActivity().getContentResolver().openInputStream(imageUri), "file.jpg");
        } catch (Exception e) {
            Log.e(TAG, "Image access failed: " + e.getMessage(), e);
            Utility.showToast(getContext(), "Error accessing image file. Please try again.");
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
                    Log.e(TAG, "Cloudinary JSON parsing failed: " + e.getMessage());
                    Utility.showToast(getContext(), "Upload successful but failed to process response.");
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                submitButton.setEnabled(true);
                Log.e(TAG, "Cloudinary upload failed. Status: " + statusCode, error);
                Utility.showToast(getContext(), "Image upload failed. Status: " + statusCode);
            }
        });
    }

    private void savePostDataToFirestore(String imageUrl, LostItems itemData) {
        itemData.setImageURI(imageUrl);

        Utility.getCollectionReferrenceForItems2().add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Utility.showToast(getContext(), "Item added successfully!");
                    resetForm();
                    dismiss();
                    if (listener != null) listener.onItemAdded();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore save failed: " + e.getMessage(), e);
                    Utility.showToast(getContext(), "Failed to save post data.");
                });
    }

    private void resetForm() {
        itemNameEditText.setText("");
        description.setText("");
        location.setText("");
        dateEdit.setText("");
        timeEdit.setText("");
        categorySpinner.setSelection(0);
        upload.setText(R.string.upload_image);
        imageUri = null;
        date = null;
        time = null;
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            mYear = year; mMonth = month; mDay = day; updateDateButton();
        }, mYear, mMonth, mDay).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            mHour = hour; mMinute = minute; updateTimeButton();
        }, mHour, mMinute, false).show();
    }

    private void updateDateButton() {
        date = mDay + "/" + (mMonth + 1) + "/" + mYear;
        dateEdit.setText(date);
    }

    private void updateTimeButton() {
        int hour12 = mHour % 12; if (hour12 == 0) hour12 = 12;
        String am_pm = (mHour < 12) ? "AM" : "PM";
        time = hour12 + ":" + String.format("%02d", mMinute) + " " + am_pm;
        timeEdit.setText(time);
    }
}
