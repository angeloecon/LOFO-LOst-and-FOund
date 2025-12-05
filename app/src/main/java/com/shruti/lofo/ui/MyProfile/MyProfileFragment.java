package com.shruti.lofo.ui.MyProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shruti.lofo.R;

public class MyProfileFragment extends Fragment {

    TextView profileName, profileEmail, profilePhone, titleName;
    Button editProfileButton;
    FirebaseFirestore database;
    String documentId; // store document id of user

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_profile, container, false);

        profileName = root.findViewById(R.id.profileName);
        profileEmail = root.findViewById(R.id.profileEmail);
        profilePhone = root.findViewById(R.id.profilephone);
        titleName = root.findViewById(R.id.titlename);
        editProfileButton = root.findViewById(R.id.editProfileButton);

        database = FirebaseFirestore.getInstance();

        fetchUserData();

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            intent.putExtra("documentId", documentId);
            startActivity(intent);
        });

        return root;
    }

    private void fetchUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        String userEmail = currentUser.getEmail();
        database.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                        documentId = documentSnapshot.getId();

                        String nameFromDB = documentSnapshot.getString("name");
                        String emailFromDB = documentSnapshot.getString("email");
                        String phoneFromDB = documentSnapshot.getString("phone");

                        titleName.setText(nameFromDB);
                        profileName.setText(nameFromDB);
                        profileEmail.setText(emailFromDB);
                        profilePhone.setText(phoneFromDB);
                    }
                });
    }
}
