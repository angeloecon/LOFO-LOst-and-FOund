package com.shruti.lofo.ui.Found;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shruti.lofo.R;
import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.Item;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoundDetails extends AppCompatActivity {

    private ImageView img;
    private TextView title, address, email, dateFound, timeFound, description, category, finderName; // Added timeFound
    private String phnum;
    private Button backBtn, callBtn, smsBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_details);

        img = findViewById(R.id.img);
        title = findViewById(R.id.title);
        address = findViewById(R.id.address);
        email = findViewById(R.id.mail);
        dateFound = findViewById(R.id.dateFound);
        timeFound = findViewById(R.id.timeFound); // Assuming ID exists
        finderName = findViewById(R.id.finderName);
        description = findViewById(R.id.description);
        category = findViewById(R.id.category);
        backBtn = findViewById(R.id.backBtn);
        callBtn = findViewById(R.id.call);
        smsBtn = findViewById(R.id.sms);

        int itemId = getIntent().getIntExtra("itemId", -1);

        if (itemId != -1) {
            fetchItemDetail(itemId);
        } else {
            Toast.makeText(FoundDetails.this, "Error: Item ID missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set an onClickListener for the Call button
        callBtn.setOnClickListener(view -> makeCall());

        // Set an onClickListener for the SMS button
        smsBtn.setOnClickListener(view -> sendSms());
        // Set an onClickListener for the Back button
        backBtn.setOnClickListener(view -> finish());
    }

    private void fetchItemDetail(int itemId){
        ApiService apiService = RetrofitClient.getApiService();
        Call<Item> call = apiService.getItemById(itemId);

        call.enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Item item = response.body();

                    title.setText(item.getItem_name());
                    address.setText(item.getLocation());
                    description.setText(item.getDescription());
                    category.setText(item.getCategory());
                    dateFound.setText(item.getDate());
                    timeFound.setText(item.getTime());

                    phnum = item.getContact_phone();

                    // temporary
                    finderName.setText(item.getItem_name());

                    Glide.with(FoundDetails.this)
                            .load(item.getImage_url())
                            .placeholder(R.drawable.dashboard_img1)
                            .error(R.drawable.dashboard_img1)
                            .into(img);
                } else {
                    Toast.makeText(FoundDetails.this, "Item details not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e("LostDetails", "API Error: " + t.getMessage());
                Toast.makeText(FoundDetails.this, "Error loading data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void makeCall () {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phnum));
        startActivity(intent);
    }

    private void sendSms() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:" + phnum));
        intent.putExtra("sms_body", "Hello, I want to inquire about item you found.");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 🟢 CRITICAL FIX: Clean up Glide to prevent crashes on Activity destroy (memory leak)
        if (img != null) {
            try {
                // 'this' refers to the Activity context, which is the correct context to clear.
                Glide.with(this).clear(img);
            } catch (Exception e) {
                // Log if Glide fails to clear, but don't crash the app
            }
        }
    }
}
