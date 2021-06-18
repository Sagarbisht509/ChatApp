package com.sagar.chatapp.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sagar.chatapp.ChatActivity;
import com.sagar.chatapp.Models.User;
import com.sagar.chatapp.R;
import com.sagar.chatapp.databinding.UserItemBinding;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    Context context;
    ArrayList<User> users;

    private Dialog dialog;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        String sender = FirebaseAuth.getInstance().getCurrentUser().getUid() + user.getuId();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(sender).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String lastMessage = snapshot.child("lastMessage").getValue(String.class);
                    // Long lastMessageTime = snapshot.child("lastMessageTime").getValue(Long.class);

                    holder.binding.lastMessage.setText(lastMessage);

//                    @SuppressLint("SimpleDateFormat")
//                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
//                    holder.binding.time.setText(dateFormat.format(new Date(lastMessageTime)));
                } else {
                    String temp = "Say hi";
                    holder.binding.lastMessage.setText(temp);
                    //  holder.binding.time.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.binding.userName.setText(user.getName());

        Glide.with(context)
                .load(user.getProfileImage())
                .placeholder(R.drawable.unknown)
                .into(holder.binding.userProfileImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("uId", user.getuId());
            intent.putExtra("receiverProfileImage", user.getProfileImage());
            context.startActivity(intent);
        });

        holder.binding.userProfileImage.setOnClickListener(v -> {
            dialog = new Dialog(context);
            dialog.setCanceledOnTouchOutside(false);

            ImageView close;
            CircleImageView userProfileImage;
            TextView userName, userPhoneNumber, userAbout;

            dialog.setContentView(R.layout.users_profile_popup);

            close = dialog.findViewById(R.id.close);
            userProfileImage = dialog.findViewById(R.id.circleImageView);
            userName = dialog.findViewById(R.id.userName);
            userPhoneNumber = dialog.findViewById(R.id.userPhoneNumber);
            userAbout = dialog.findViewById(R.id.userAbout);

            close.setOnClickListener(v1 -> dialog.dismiss());


            Glide.with(context)
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.unknown)
                    .into(userProfileImage);

            userName.setText(user.getName());
            userPhoneNumber.setText(user.getPhoneNumber());
            userAbout.setText(user.getAbout());

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        UserItemBinding binding;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserItemBinding.bind(itemView);
        }
    }
}
