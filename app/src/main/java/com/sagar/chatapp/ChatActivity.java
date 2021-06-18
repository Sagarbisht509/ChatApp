package com.sagar.chatapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sagar.chatapp.Adapter.MessageAdapter;
import com.sagar.chatapp.Models.Message;
import com.sagar.chatapp.Utility.NetworkChangeListener;
import com.sagar.chatapp.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    ArrayList<Message> messages;
    MessageAdapter messageAdapter;

    FirebaseStorage storage;
    FirebaseDatabase database;

    String sender, receiver;
    String receiverUid, senderUid;

    BroadcastReceiver broadcastReceiver;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        String receiverName = getIntent().getStringExtra("name");
        String receiverProfileImage = getIntent().getStringExtra("receiverProfileImage");
        receiverUid = getIntent().getStringExtra("uId");

        binding.receiverName.setText(receiverName);
        Glide.with(this).load(receiverProfileImage).placeholder(R.drawable.unknown).into(binding.receiverProfileImage);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image");
        progressDialog.setCancelable(false);

        binding.back.setOnClickListener(v -> finish());

        broadcastReceiver = new NetworkChangeListener();
        registerNetworkBroadcastReceiver();

        senderUid = FirebaseAuth.getInstance().getUid();

        sender = senderUid + receiverUid;
        receiver = receiverUid + senderUid;

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(sender, receiver, this, messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(messageAdapter);

        database.getReference()
                .child("Chats")
                .child(sender)
                .child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messages.add(message);
                }

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.send.setOnClickListener(v -> {
            String senderMessage = binding.message.getText().toString().trim();

            if (!senderMessage.isEmpty()) {
                binding.message.setText("");

                String key = database.getReference().push().getKey();

                Date date = new Date();
                Message message = new Message(date.getTime(), senderUid, senderMessage, key);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("lastMessage", message.getMessage());
                hashMap.put("lastMessageTime", message.getMessageTime());
                hashMap.put("messageId", message.getMessageId());

                database.getReference()
                        .child("Chats")
                        .child(sender).updateChildren(hashMap);

                database.getReference()
                        .child("Chats")
                        .child(receiver).updateChildren(hashMap);

                database.getReference()
                        .child("Chats")
                        .child(sender)
                        .child("Messages")
                        .child(key)
                        .setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                        .child("Chats")
                        .child(receiver)
                        .child("Messages")
                        .child(key)
                        .setValue(message).addOnSuccessListener(aVoid1 -> {

                        }));
            }
        });

        binding.attachFile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            startActivityForResult(intent, 20);
        });

//        final Handler handler = new Handler();
//        binding.message.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                database.getReference().child("Status").child(senderUid).setValue("Typing...");
//                handler.removeCallbacksAndMessages(null);
//                handler.postDelayed(userStopTyping, 1000);
//            }
//
//            final Runnable userStopTyping = new Runnable() {
//                @Override
//                public void run() {
//                    database.getReference().child("Status").child(senderUid).setValue("Online");
//                }
//            };
//        });

        database.getReference().child("Status").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String receiverStatus = snapshot.getValue(String.class);

                    if (!receiverStatus.isEmpty()) {
                        binding.receiverStatus.setText(receiverStatus);
                        binding.receiverStatus.setVisibility(View.VISIBLE);
                    } else {
                        binding.receiverStatus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 20) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();

                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("Chats").child("" + calendar.getTimeInMillis());
                    progressDialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {

                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String filePath = uri.toString();

                                String senderMessage = binding.message.getText().toString().trim();

                                binding.message.setText("");

                                String key = database.getReference().push().getKey();

                                Date date = new Date();
                                Message message = new Message(date.getTime(), senderUid, senderMessage, key);
                                message.setImageUrl(filePath);
                                message.setMessage("photo");

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("lastMessage", message.getMessage());
                                hashMap.put("lastMessageTime", message.getMessageTime());
                                hashMap.put("messageId", message.getMessageId());

                                database.getReference()
                                        .child("Chats")
                                        .child(sender).updateChildren(hashMap);

                                database.getReference()
                                        .child("Chats")
                                        .child(receiver).updateChildren(hashMap);

                                database.getReference()
                                        .child("Chats")
                                        .child(sender)
                                        .child("Messages")
                                        .child(key).setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                                        .child("Chats")
                                        .child(receiver)
                                        .child("Messages")
                                        .child(key).setValue(message).addOnSuccessListener(aVoid1 -> {

                                        }));

                            });
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Status").child(uId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String uId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Status").child(uId).setValue("");
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