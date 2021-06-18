package com.sagar.chatapp.auth;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;
import com.sagar.chatapp.MainActivity;
import com.sagar.chatapp.R;
import com.sagar.chatapp.Utility.NetworkChangeListener;

public class SignupActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;

    private CountryCodePicker ccp;
    private EditText editText;
    private ImageButton next;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        broadcastReceiver = new NetworkChangeListener();
        registerNetworkBroadcastReceiver();

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        ccp = findViewById(R.id.ccp);
        editText = findViewById(R.id.phoneNumber);
        next = findViewById(R.id.nextButton);

        ccp.registerCarrierNumberEditText(editText);

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            if (isValidNumber) {
                next.setOnClickListener(v -> new AlertDialog.Builder(SignupActivity.this)
                        .setTitle("A verification code will be send to:\n" + ccp.getFullNumberWithPlus() + "\nIs your phone number above correct?")
                        .setPositiveButton("EDIT NUMBER", (dialog, which) -> editText.requestFocus()).setNegativeButton("CONFIRM", (dialog, which) -> {
                            Intent intent = new Intent(SignupActivity.this, OtpActivity.class);
                            intent.putExtra("phoneNumber", ccp.getFullNumberWithPlus());
                            startActivity(intent);
                        }).show());
            } else {
                if (!editText.getText().toString().isEmpty()) {
                    editText.setError("Please enter valid phone number");
                }
            }
        });

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