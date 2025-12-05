package com.shruti.lofo.ui.Found;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shruti.lofo.R;

public class FoundDetails extends AppCompatActivity {

    private ImageView img;
    private TextView title, address, email, dateFound, timeFound, description, category, finderName; // Added timeFound
    String phnum;
    private Button backBtn, callBtn, smsBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

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

        // Get the Document ID passed from the adapter
        String itemId = getIntent().getStringExtra("itemId");

        if (itemId != null) {
            // CRITICAL FIX: Get the document directly using the unique ID
            DocumentReference itemRef = db.collection("foundItems").document(itemId);

            itemRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageURI");
                        String itemName = documentSnapshot.getString("itemName");
                        String itemLocation = documentSnapshot.getString("location");
                        String itemEmail = documentSnapshot.getString("email");
                        String itemDescription = documentSnapshot.getString("description");

                        // Use correct field names
                        String itemFinderName = documentSnapshot.getString("finderName");
                        String itemPhnum = documentSnapshot.getString("phnum");
                        String itemDate = documentSnapshot.getString("dateFound");
                        String itemTime = documentSnapshot.getString("timeFound"); // New field
                        String itemCategory = documentSnapshot.getString("category");

                        // Load the image using Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(FoundDetails.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.placeholder_image)
                                    .error(R.drawable.baseline_image_search_24)
                                    .into(img);
                        } else {
                            img.setImageResource(R.drawable.placeholder_image);
                        }

                        // Set the text for the attributes
                        title.setText(itemName);
                        address.setText(itemLocation);
                        dateFound.setText(itemDate);
                        timeFound.setText(itemTime); // Set new field
                        description.setText(itemDescription);
                        email.setText(itemEmail);
                        finderName.setText(itemFinderName);
                        category.setText(itemCategory);
                        phnum = itemPhnum;
                    } else {
                        Toast.makeText(FoundDetails.this, "Item details not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(FoundDetails.this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            Toast.makeText(FoundDetails.this, "Error: Item ID missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set an onClickListener for the Call button
        callBtn.setOnClickListener(view -> {
            // When the Call button is clicked, open the phone dialer
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phnum));
            startActivity(intent);
        });

        // Set an onClickListener for the SMS button
        smsBtn.setOnClickListener(view -> {
            // When the SMS button is clicked, open the SMS app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + phnum));
            intent.putExtra("sms_body", "Hello, I want to inquire about item you found.");
            startActivity(intent);
        });


        // Set an onClickListener for the Back button
        backBtn.setOnClickListener(view -> finish());
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
} // <--
