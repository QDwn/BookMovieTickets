package com.example.movieee.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Model.Ticket;
import com.example.movieee.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> ticketList;
    private Context context;
    private OnItemClickListener listener; // Khai báo listener

    // Interface cho sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Ticket ticket);
    }

    // Setter cho listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TicketAdapter(List<Ticket> ticketList, Context context) {
        this.ticketList = ticketList;
        this.context = context;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        holder.tvMovieTitle.setText(ticket.getMovieTitle());
        holder.tvTicketDate.setText("Ngày: " + ticket.getNgayChieu());
        holder.tvTicketTime.setText("Giờ: " + ticket.getGioChieu());
        holder.tvTicketLocation.setText("Rạp: " + ticket.getDiaDiem());
        holder.tvTicketSeats.setText("Ghế: " + (ticket.getGheDaChon() != null ? String.join(", ", ticket.getGheDaChon()) : "N/A"));

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvTicketTotalAmount.setText("Tổng tiền: " + formatter.format(ticket.getTongCong()) + "đ");

        // Đặt OnClickListener cho toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvTicketDate, tvTicketTime, tvTicketLocation, tvTicketSeats, tvTicketTotalAmount;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvTicketMovieTitle);
            tvTicketDate = itemView.findViewById(R.id.tvTicketDate);
            tvTicketTime = itemView.findViewById(R.id.tvTicketTime);
            tvTicketLocation = itemView.findViewById(R.id.tvTicketLocation);
            tvTicketSeats = itemView.findViewById(R.id.tvTicketSeats);
            tvTicketTotalAmount = itemView.findViewById(R.id.tvTicketTotalAmount);
        }
    }
}