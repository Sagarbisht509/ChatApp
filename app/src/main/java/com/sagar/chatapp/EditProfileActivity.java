package com.sagar.chatapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sagar.chatapp.Utility.NetworkChangeListener;
import com.sagar.chatapp.databinding.ActivityEditProfileBinding;

import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;

    ActivityEditProfileBinding binding;

    String currentUserId;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;

    Uri newSelectedImage;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        broadcastReceiver = new NetworkChangeListener();
        registerNetworkBroadcastReceiver();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit profile");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile.");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phoneNumber = intent.getStringExtra("phone");
        String image = intent.getStringExtra("image");
        String about = intent.getStringExtra("about");

        Glide.with(EditProfileActivity.this)
                .load(image)
                .placeholder(R.drawable.unknown)
                .into(binding.profileImage);

        binding.name.setText(name);
        binding.phoneNumber.setText(phoneNumber);
        binding.about.setText(about);

        binding.profileImage.setOnClickListener(v -> {
            Intent intent1 = new Intent();
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            intent1.setType("image/*");
            startActivityForResult(intent1, 40);
        });

        binding.cancel.setOnClickListener(v -> finish());

        binding.saveChanges.setOnClickListener(v -> {

            if (binding.name.getText().toString().equals("")) {
                binding.name.setError("Field cannot be empty");
            } else {
                progressDialog.show();
                if (newSelectedImage != null) {
                    StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(currentUserId));
                    reference.putFile(newSelectedImage).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();

                                HashMap<String, Object> map = new HashMap<>();

                                map.put("name", binding.name.getText().toString().trim());
                                map.put("phoneNumber", phoneNumber);
                                map.put("profileImage", imageUrl);
                                map.put("about", binding.about.getText().toString().trim());

                                database.getReference().child("Users")
                                        .child(currentUserId).updateChildren(map).addOnSuccessListener(aVoid -> {

                                            progressDialog.dismiss();
                                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        });
                            });
                        }
                    });
                } else {
                    HashMap<String, Object> map = new HashMap<>();

                    map.put("name", binding.name.getText().toString().trim());
                    map.put("phoneNumber", phoneNumber);
                    map.put("profileImage", image);
                    map.put("about", binding.about.getText().toString().trim());

                    database.getReference().child("Users")
                            .child(currentUserId).updateChildren(map).addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                }

            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                binding.profileImage.setImageURI(data.getData());
                newSelectedImage = data.getData();
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("Status").child(currentUserId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("Status").child(currentUserId).setValue("");
    }
}

