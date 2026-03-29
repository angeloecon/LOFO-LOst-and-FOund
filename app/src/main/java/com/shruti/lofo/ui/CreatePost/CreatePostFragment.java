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
import android.widget.Toast;

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
import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.Item;
import com.shruti.lofo.data.model.ItemRequest;
import com.shruti.lofo.databinding.FragmentCreatePostBinding;
import com.shruti.lofo.models.LostItems;

import org.json.JSONObject;


import java.util.Calendar;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostFragment extends DialogFragment {

    private FragmentCreatePostBinding binding;
    private String itemType;
    private Uri imageUri = null;
    private String selectedCategoryString = "Other";
    private String selectedDate, selectedTime;

    private int mYear, mMonth, mDay, mHour, mMinute;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // A way to tell the form if it should be "Lost" or "Found"
    public static CreatePostFragment newInstance(String type) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putString("TYPE", type);
        fragment.setArguments(args);
        return fragment;
    }
    public interface OnItemAddedListener {
        void onItemAdded();
    }

    private OnItemAddedListener listener;

    private ImageButton datePickerButton, timePickerButton;
    private TextView dateEdit, timeEdit;
    private Spinner categorySpinner;
    private EditText description, location, itemNameEditText;
    private Button upload, submitButton;

    private String date, time;

    private static final String TAG = "CreatePostFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            itemType = getArguments().getString("TYPE", "Lost");
        }

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                binding.uploadImageButton.setText("IMAGE SELECTED ✓");
                binding.uploadImageButton.setBackgroundColor(0xFF4CAF50);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDynamicUI();
        setupPickers();
        setupSpinner();

        binding.uploadImageButton.setOnClickListener(v-> {
            Intent iGallery = new Intent(Intent.ACTION_GET_CONTENT);
            iGallery.setType("image/*");
            imagePickerLauncher.launch(iGallery);
        });

        binding.submitButton.setOnClickListener(v -> validateAndSubmit());
    }

    private void setupDynamicUI() {
        if(itemType.equals("Found")){
            binding.headerTitle.setText("Report Found Item");
            binding.headerSubtitle.setText("Help someone find their lost belongings");
            binding.submitButton.setText("SUBMIT FOUND ITEM");
        } else {
            binding.headerTitle.setText("Report Lost Item");
            binding.headerSubtitle.setText("Let others know about your lost belongings");
            binding.submitButton.setText("SUBMIT LOST ITEM");
        }
    }

    private void setupPickers() {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        binding.datePickerButton.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                selectedDate = year + "-" + String.format("%02d", (month + 1)) + "-" + String.format("%02d", day);
                binding.selectedDateEditText.setText(selectedDate);
            }, mYear, mMonth, mDay).show();
        });

        binding.timePickerButton.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view, hour, minute) -> {
                int hour12 = hour % 12 == 0 ? 12 : hour % 12;
                String am_pm = (hour < 12) ? "AM" : "PM";
                selectedTime = hour12 + ":" + String.format("%02d", minute) + " " + am_pm;
                binding.selectedTimeEditText.setText(selectedTime);
            }, mHour, mMinute, false).show();
        });
    }

    private void setupSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        binding.categorySpinner.setAdapter(adapter);

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryString = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void validateAndSubmit() {
        String itemName = binding.itemNameEdittext.getText().toString().trim();
        String desc = binding.description.getText().toString().trim();
        String loc = binding.description.getText().toString().trim();

        if(itemName.isEmpty() || desc.isEmpty() || loc.isEmpty() || selectedDate == null || selectedTime == null || imageUri == null) {
            Toast.makeText(getContext(), "Please fill out all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.submitButton.setEnabled(false);
        binding.submitButton.setText("UPLOADING IMAGE..");

        uploadImageToCloudinary(itemName, desc, loc);
    }


    private void uploadImageToCloudinary(String itemName, String desc, String loc){
        String uploadUrl = "https://api.cloudinary.com/v1_1" + CloudinaryConfig.CLOUD_NAME + "/image/upload";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        try {
            params.put("upload_preset", "dny4hxil");
            params.put("file", requireActivity());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error reading image file.", Toast.LENGTH_SHORT).show();
            binding.submitButton.setEnabled(true);
            return;
        }

        client.post(uploadUrl, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonResponse = new JSONObject(new String(responseBody));
                    String imageUrl = jsonResponse.getString("secure_url");
                    saveToDataBase(itemName, desc, loc, imageUrl);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error parsing image upload response.", Toast.LENGTH_SHORT).show();
                    binding.submitButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), "Image upload failed.", Toast.LENGTH_SHORT).show();
                binding.submitButton.setEnabled(true);
                binding.submitButton.setText("SUBMIT " + itemType.toUpperCase() + " ITEM");
            }
        });
    }

    private void saveToDataBase(String itemName, String desc, String loc, String imageUrl) {
        binding.submitButton.setText("SAVING..");

        ItemRequest request = new ItemRequest(
                itemName, desc, loc, selectedDate, selectedTime, selectedCategoryString, itemType, imageUrl, "09123456789"
        );

        ApiService apiService = RetrofitClient.getApiService();
        Call<Item> call = apiService.createItem(request);

        call.enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getContext(), "Item posted successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to save item.", Toast.LENGTH_SHORT).show();
                    binding.submitButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e("CreatePost", "API Error: " + t.getMessage());
                Toast.makeText(getContext(), "Network error.", Toast.LENGTH_SHORT).show();
                binding.submitButton.setEnabled(true);
            }
        });

    }






      @Override
    public  void onDestroy(){
        super.onDestroy();
        binding = null;
      }
}
