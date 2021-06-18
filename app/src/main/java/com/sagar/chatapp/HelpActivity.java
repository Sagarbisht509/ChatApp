package com.sagar.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sagar.chatapp.Utility.NetworkChangeListener;
import com.sagar.chatapp.databinding.ActivityHelpBinding;

import java.util.Objects;

public class HelpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityHelpBinding binding;

    BroadcastReceiver broadcastReceiver;

    String currentUserId;
    FirebaseDatabase database;

    private String subject, body, to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();

        broadcastReceiver = new NetworkChangeListener();
        registerNetworkBroadcastReceiver();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Help");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(HelpActivity.this, R.array.reason, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(this);

        binding.next.setOnClickListener(v -> {
            body = binding.editText.getText().toString().trim();

            if (body.length() < 10) {
                Toast.makeText(HelpActivity.this, "Please be as descriptive as possible to help us understand the issue", Toast.LENGTH_SHORT).show();
            } else {
                to = "sagarbisht509@gmail.com";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);

                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        subject = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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