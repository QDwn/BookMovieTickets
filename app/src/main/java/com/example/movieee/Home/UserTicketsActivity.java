// File: qdwn/bookmovietickets/BookMovieTickets-codetest/app/src/main/java/com/example/movieee/UserTicketsActivity.java

package com.example.movieee.Home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater; // Thêm import này
import android.view.View;
import android.widget.Button; // Thêm import này
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Adapter.TicketAdapter;
import com.example.movieee.Model.Ticket;
import com.example.movieee.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class UserTicketsActivity extends AppCompatActivity implements TicketAdapter.OnItemClickListener { // Triển khai interface

    private RecyclerView recyclerViewUserTickets;
    private TextView tvNoTickets;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;

    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tickets);

        recyclerViewUserTickets = findViewById(R.id.recyclerViewUserTickets);
        tvNoTickets = findViewById(R.id.tvNoTickets);
        recyclerViewUserTickets.setLayoutManager(new LinearLayoutManager(this));

        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList, this);
        recyclerViewUserTickets.setAdapter(ticketAdapter);
        ticketAdapter.setOnItemClickListener(this); // Đặt listener cho adapter

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserEmail = sharedPref.getString("user_email", null);

        Log.d("UserTicketsDebug", "Current logged-in email from SharedPreferences: " + currentUserEmail);
        Toast.makeText(this, "Email hiện tại: " + (currentUserEmail != null ? currentUserEmail : "NULL"), Toast.LENGTH_LONG).show();

        if (currentUserEmail != null) {
            loadUserTickets(currentUserEmail);
        } else {
            tvNoTickets.setVisibility(View.VISIBLE);
            tvNoTickets.setText("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập.");
            Toast.makeText(this, "Không tìm thấy email người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadUserTickets(String email) {
        DatabaseReference lichSuDatVeRef = FirebaseDatabase.getInstance().getReference("lich_su_dat_ve");

        lichSuDatVeRef.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                        Ticket ticket = ticketSnapshot.getValue(Ticket.class);
                        if (ticket != null) {
                            ticket.setBookingId(ticketSnapshot.getKey());
                            ticketList.add(ticket);
                        }
                    }
                    Collections.sort(ticketList, Comparator.comparingLong(Ticket::getTimestamp).reversed());
                    ticketAdapter.notifyDataSetChanged();
                    tvNoTickets.setVisibility(View.GONE);
                } else {
                    tvNoTickets.setVisibility(View.VISIBLE);
                    tvNoTickets.setText("Bạn chưa có vé nào được đặt.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserTicketsActivity.this, "Lỗi khi tải vé: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                tvNoTickets.setVisibility(View.VISIBLE);
                tvNoTickets.setText("Lỗi khi tải vé. Vui lòng thử lại.");
            }
        });
    }

    @Override
    public void onItemClick(Ticket ticket) {
        showTicketDetailsDialog(ticket);
    }

    private void showTicketDetailsDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_RedBlackTheme); // Sử dụng theme đã định nghĩa
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_details, null);
        builder.setView(dialogView);

        TextView tvMovieTitle = dialogView.findViewById(R.id.tvDetailMovieTitle);
        TextView tvUsername = dialogView.findViewById(R.id.tvDetailUsername);
        TextView tvEmail = dialogView.findViewById(R.id.tvDetailEmail);
        TextView tvTicketId = dialogView.findViewById(R.id.tvDetailTicketId);
        TextView tvDate = dialogView.findViewById(R.id.tvDetailDate);
        TextView tvTime = dialogView.findViewById(R.id.tvDetailTime);
        TextView tvLocation = dialogView.findViewById(R.id.tvDetailLocation);
        TextView tvSeats = dialogView.findViewById(R.id.tvDetailSeats);
        TextView tvTotalAmount = dialogView.findViewById(R.id.tvDetailTotalAmount);
        Button btnClose = dialogView.findViewById(R.id.btnDetailClose);

        // Đổ dữ liệu từ đối tượng Ticket vào các TextView
        tvMovieTitle.setText("Phim: " + ticket.getMovieTitle());
        tvUsername.setText("Người đặt: " + (ticket.getUserName() != null ? ticket.getUserName() : "N/A"));
        tvEmail.setText("Email: " + (ticket.getUserEmail() != null ? ticket.getUserEmail() : "N/A"));
        tvTicketId.setText("Mã vé: " + (ticket.getBookingId() != null ? ticket.getBookingId() : "N/A"));
        tvDate.setText("Ngày chiếu: " + (ticket.getNgayChieu() != null ? ticket.getNgayChieu() : "N/A"));
        tvTime.setText("Giờ chiếu: " + (ticket.getGioChieu() != null ? ticket.getGioChieu() : "N/A"));
        tvLocation.setText("Rạp: " + (ticket.getDiaDiem() != null ? ticket.getDiaDiem() : "N/A"));
        tvSeats.setText("Ghế đã chọn: " + (ticket.getGheDaChon() != null ? String.join(", ", ticket.getGheDaChon()) : "Không có"));

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText("Tổng cộng: " + formatter.format(ticket.getTongCong()) + "đ");

        AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> dialog.dismiss()); // Đóng dialog khi nhấn nút Đóng

        dialog.show();
    }
}