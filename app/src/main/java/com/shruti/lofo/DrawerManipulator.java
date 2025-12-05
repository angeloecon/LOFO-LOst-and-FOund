package com.shruti.lofo;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
// Removed unused import: import com.google.firebase.firestore.QuerySnapshot;

public class DrawerManipulator {

    public static void updateDrawerHeader(Context context, View headerView) {
        TextView usernameTextView = headerView.findViewById(R.id.usernameTextView);

        // Use the string resource for a clean default state
        String defaultText = context.getString(R.string.nav_header_default_text);
        usernameTextView.setText(defaultText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check for user existence, no longer need to check user.getEmail()
        if (user != null) {
            // ⭐️ FIX 2a: Use the fast UID for direct access ⭐️
            final String userUid = user.getUid();

            // Set email as the loading/fallback text immediately
            String userEmail = user.getEmail();
            if (userEmail != null) {
                usernameTextView.setText(userEmail);
            }

            // ⭐️ FIX 2b: Change from slow whereEqualTo query to fast document() lookup ⭐️
            FirebaseFirestore.getInstance().collection("users")
                    .document(userUid) // Direct document access by UID
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Document found
                            String name = documentSnapshot.getString("name");
                            Log.d("Firestore", "Name retrieved: " + name);

                            if (name != null && !name.isEmpty()) {
                                usernameTextView.setText("Hello, " + name);
                            } else if (userEmail != null) {
                                usernameTextView.setText(userEmail); // Fallback to email if name is empty
                            } else {
                                usernameTextView.setText(defaultText);
                            }
                        } else {
                            // User document doesn't exist for this UID
                            Log.w("Firestore", "User document not found for UID: " + userUid);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching user data: " + e.getMessage(), e);
                        Toast.makeText(context, "Error loading profile info.", Toast.LENGTH_SHORT).show();
                        // If fetching fails, the text remains the email or the default text
                    });
        }
    }
}