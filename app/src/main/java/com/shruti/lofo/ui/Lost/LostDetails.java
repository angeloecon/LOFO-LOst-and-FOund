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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shruti.lofo.R;

public class LostDetails extends AppCompatActivity {

    private ImageView img;
    private TextView title, address, email, dateLost, timeLost, description, category, ownerName;
    private String phnum;
    private Button backBtn, callBtn, smsBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost_details);

        db = FirebaseFirestore.getInstance();

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

        String itemId = getIntent().getStringExtra("itemId");

        if (itemId != null) {
            DocumentReference itemRef = db.collection("lostItems").document(itemId);
            itemRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("imageURI");
                    title.setText(documentSnapshot.getString("itemName"));
                    address.setText(documentSnapshot.getString("location"));
                    email.setText(documentSnapshot.getString("email"));
                    description.setText(documentSnapshot.getString("description"));
                    ownerName.setText(documentSnapshot.getString("ownerName"));
                    category.setText(documentSnapshot.getString("category"));
                    dateLost.setText(documentSnapshot.getString("dateLost"));
                    timeLost.setText(documentSnapshot.getString("timeLost"));

                    Object phnumObj = documentSnapshot.get("phnum");
                    phnum = (phnumObj != null) ? phnumObj.toString() : "";

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(LostDetails.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.baseline_image_search_24)
                                .into(img);
                    } else {
                        img.setImageResource(R.drawable.placeholder_image);
                    }

                } else {
                    Toast.makeText(this, "Item details not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            Toast.makeText(this, "Error: Item ID missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        callBtn.setOnClickListener(v -> makeCall(phnum));
        smsBtn.setOnClickListener(v -> sendSms(phnum));
        backBtn.setOnClickListener(v -> finish());
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
