package com.shruti.lofo.ui.Lost;

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
import com.shruti.lofo.R;
import com.shruti.lofo.api.ApiService;
import com.shruti.lofo.api.RetrofitClient;
import com.shruti.lofo.data.model.Item;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LostDetails extends AppCompatActivity {

    private ImageView img;
    private TextView title, address, email, dateLost, timeLost, description, category, ownerName;
    private String phnum;
    private Button backBtn, callBtn, smsBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost_details);

        img = findViewById(R.id.img);
        title = findViewById(R.id.title);
        address = findViewById(R.id.address);
        dateLost = findViewById(R.id.dateLost);
        timeLost = findViewById(R.id.timeLost);
        email = findViewById(R.id.mail);
        description = findViewById(R.id.description);
        ownerName = findViewById(R.id.ownerName);
        category = findViewById(R.id.category);
        backBtn = findViewById(R.id.backBtn);
        callBtn = findViewById(R.id.call);
        smsBtn = findViewById(R.id.sms);

        int itemId = getIntent().getIntExtra("itemId", -1);

        if (itemId != -1) {
             fetchItemDetail(itemId);
        } else {
            Toast.makeText(this, "Error: Item ID missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        callBtn.setOnClickListener(v -> makeCall(phnum));
        smsBtn.setOnClickListener(v -> sendSms(phnum));
        backBtn.setOnClickListener(v -> finish());
    }

    private void fetchItemDetail(int itemId) {
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
                    dateLost.setText(item.getDate());
                    timeLost.setText(item.getTime());

                    phnum = item.getContact_phone();

                    // Temporary, will change later
                    ownerName.setText(item.getItem_name());

                    Glide.with(LostDetails.this)
                            .load(item.getImage_url())
                            .placeholder(R.drawable.dashboard_img1) // Use your placeholder
                            .error(R.drawable.dashboard_img1)
                            .into(img);


                } else {
                    Toast.makeText(LostDetails.this, "Item details not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                Log.e("LostDetails", "API Error: " + t.getMessage());
                Toast.makeText(LostDetails.this, "Error loading data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void makeCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || phoneNumber.equals("0")) {
            Toast.makeText(this, "Contact number unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
    }

    private void sendSms(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || phoneNumber.equals("0")) {
            Toast.makeText(this, "Contact number unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
        intent.putExtra("sms_body", "Hello, I want to inquire about your lost item.");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (img != null) {
            try { Glide.with(this).clear(img); } catch (Exception ignored) {}
        }
    }
}
