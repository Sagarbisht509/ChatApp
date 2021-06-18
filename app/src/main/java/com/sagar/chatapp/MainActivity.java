package com.sagar.chatapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagar.chatapp.Adapter.UserAdapter;
import com.sagar.chatapp.Models.User;
import com.sagar.chatapp.Utility.NetworkChangeListener;
import com.sagar.chatapp.auth.SignupActivity;
import com.sagar.chatapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    BroadcastReceiver broadcastReceiver;

    private FirebaseDatabase database;

    private String currentUserId , name, phone , about, image;

    ActivityMainBinding binding;

    ArrayList<User> users;
    UserAdapter adapter;

    private Dialog dialog;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        broadcastReceiver = new NetworkChangeListener();
        registerNetworkBroadcastReceiver();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);

        builder = new AlertDialog.Builder(this);

        users = new ArrayList<>();

        adapter = new UserAdapter(MainActivity.this, users);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.showShimmerAdapter();

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if(!currentUserId.equals(user.getuId())) {
                        users.add(user);
                    }
                }

                binding.recyclerView.hideShimmerAdapter();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                name = user.getName();
                image = user.getProfileImage();
                phone = user.getPhoneNumber();
                about = user.getAbout();

                Glide.with(getApplicationContext())
                        .load(image)
                        .placeholder(R.drawable.unknown)
                        .into(binding.senderProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.senderProfileImage.setOnClickListener(v -> {
            ImageView close;
            CircleImageView userProfileImage;
            TextView userName, userPhoneNumber, userAbout;
            Button editProfile;

            dialog.setContentView(R.layout.users_profile_popup);

            close = dialog.findViewById(R.id.close);
            userProfileImage = dialog.findViewById(R.id.circleImageView);
            userName = dialog.findViewById(R.id.userName);
            userPhoneNumber = dialog.findViewById(R.id.userPhoneNumber);
            userAbout = dialog.findViewById(R.id.userAbout);
            editProfile = dialog.findViewById(R.id.editProfile);

            editProfile.setVisibility(View.VISIBLE);

            close.setOnClickListener(v1 -> dialog.dismiss());

            Glide.with(MainActivity.this)
                    .load(image)
                    .placeholder(R.drawable.unknown)
                    .into(userProfileImage);

            userName.setText(name);
            userPhoneNumber.setText(phone);
            userAbout.setText(about);

            editProfile.setOnClickListener(v12 -> {
                Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("phone", phone);
                intent.putExtra("image",image);
                intent.putExtra("about", about);
                startActivity(intent);
                dialog.dismiss();
            });

            dialog.show();
        });

        binding.options.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            popupMenu.setOnMenuItemClickListener(MainActivity.this);
            popupMenu.inflate(R.menu.menu);
            popupMenu.show();
        });

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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {

//            case R.id.nav_invite:
//                inviteFriend();
//                return  true;
            case R.id.nav_logout:
                logout();
                return true;
            case R.id.nav_help:
                help();
                return true;
            default:
                return false;
        }
    }

//    private void inviteFriend() {
//        ApplicationInfo api = getApplicationContext().getApplicationInfo();
//        String apkPath = api.sourceDir;
//
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("application/vnd.android.package-archive");
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkPath)));
//        startActivity(Intent.createChooser(intent,"Share Via..."));
//    }

    private void logout() {

        builder.setTitle("Logout!");
        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
        }).setNegativeButton("No", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void help() {
        startActivity(new Intent(MainActivity.this, HelpActivity.class));
    }

    protected void registerNetworkBroadcastReceiver() {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    protected  void unRegister() {
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