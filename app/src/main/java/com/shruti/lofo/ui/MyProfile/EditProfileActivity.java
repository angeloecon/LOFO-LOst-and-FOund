package com.shruti.lofo.ui.MyProfile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.shruti.lofo.R;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName, editPhone, editEmail;  // ← ADD THIS
    Button saveBtn;
    FirebaseFirestore db;
    String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);   // ← NOW IT WORKS
        saveBtn = findViewById(R.id.saveBtn);

        db = FirebaseFirestore.getInstance();

        documentId = getIntent().getStringExtra("documentId");

        db.collection("users").document(documentId).get()
                .addOnSuccessListener(document -> {
                    editName.setText(document.getString("name"));
                    editEmail.setText(document.getString("email"));
                    editPhone.setText(document.getString("phone"));
                });

        saveBtn.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newName = editName.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();

        db.collection("users").document(documentId)
                .update("name", newName,
                        "email", newEmail,
                        "phone", newPhone)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show());
    }
}
