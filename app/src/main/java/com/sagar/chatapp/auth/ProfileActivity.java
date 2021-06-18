package com.sagar.chatapp.auth;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sagar.chatapp.MainActivity;
import com.sagar.chatapp.Models.User;
import com.sagar.chatapp.R;
import com.sagar.chatapp.Utility.NetworkChangeListener;
import com.sagar.chatapp.databinding.ActivityProfileBinding;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;

    ActivityProfileBinding binding;
    Uri selectedImage;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference userReference;

    private String name;
    private String about = "";
    private String image = "default";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        broadcastReceiver = new NetworkChangeListener();
        registerNetworkBroadcastReceiver();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait.");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        userReference = database.getReference().child("Users");

        userReference.child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    name = user.getName();
                    image = user.getProfileImage();
                    about = user.getAbout();

                    binding.name.setText(name);

                    Glide.with(getApplicationContext())
                            .load(image)
                            .placeholder(R.drawable.unknown)
                            .into(binding.profileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 45);
        });

        binding.doneButton.setOnClickListener(v -> {
            String name = binding.name.getText().toString();

            if (name.isEmpty()) {
                binding.name.setError("Please enter name");
                return;
            }

            progressDialog.show();
            if (selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            User user = new User(binding.name.getText().toString()
                                    , Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber()
                                    , imageUrl
                                    , auth.getUid(),
                                    about);

                            database.getReference()
                                    .child("Users")
                                    .child(auth.getUid())
                                    .setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        });
                    }
                });
            } else {
                User user = new User(binding.name.getText().toString()
                        , Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber()
                        , image
                        , auth.getUid(),
                        about);


                database.getReference()
                        .child("Users")
                        .child(Objects.requireNonNull(auth.getUid()))
                        .setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            progressDialog.dismiss();
                            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (data.getData() != null) {
                binding.profileImage.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }

    }

    protected void registerNetworkBroadcastReceiver() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    protected void unRegister() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unRegister();
    }
}