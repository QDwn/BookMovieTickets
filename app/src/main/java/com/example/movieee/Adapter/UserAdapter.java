package com.example.movieee.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Model.HelperClass;
import com.example.movieee.R;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private ArrayList<HelperClass> userList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HelperClass user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public UserAdapter(ArrayList<HelperClass> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        HelperClass user = userList.get(position);
        holder.tvUserEmail.setText("Email: " + user.getEmail());
        holder.tvUserPhone.setText("Phone: " + user.getPhone());
        holder.tvUserRole.setText("Role: " + user.getRole());

        // Đặt ảnh dựa trên vai trò
        if ("admin".equals(user.getRole())) {
            holder.ivUserAvatar.setImageResource(R.drawable.admin);
        } else {
            holder.ivUserAvatar.setImageResource(R.drawable.user);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserEmail, tvUserPhone, tvUserRole;
        ImageView ivUserAvatar; // Khai báo ImageView

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar); // Tham chiếu ImageView
        }
    }

    public void updateUsers(ArrayList<HelperClass> newUsers) {
        this.userList = newUsers;
        notifyDataSetChanged();
    }
}