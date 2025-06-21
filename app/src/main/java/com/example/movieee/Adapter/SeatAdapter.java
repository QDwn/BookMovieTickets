package com.example.movieee.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movieee.Model.Seat;
import com.example.movieee.R;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {
    private List<Seat> seats;
    private Context context;
    private OnSeatSelectedListener listener;

    public SeatAdapter(List<Seat> seats, Context context, OnSeatSelectedListener listener) {
        this.seats = seats;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seats.get(position);
        holder.txtSeat.setText(seat.getSeatId());

        if (seat.isBooked()) {
            holder.txtSeat.setBackgroundResource(R.drawable.bg_seat_booked);
            holder.txtSeat.setEnabled(false);
        } else {
            if (seat.isSelected()) {
                holder.txtSeat.setBackgroundResource(R.drawable.bg_seat_selected);
            } else {
                switch (seat.getType()) {
                    case "VIP":
                        holder.txtSeat.setBackgroundResource(R.drawable.bg_seat_vip);
                        break;
                    case "Couple":
                        holder.txtSeat.setBackgroundResource(R.drawable.bg_seat_couple);
                        break;
                    default:
                        holder.txtSeat.setBackgroundResource(R.drawable.bg_seat_available);
                }
            }

            holder.txtSeat.setOnClickListener(v -> {
                seat.setSelected(!seat.isSelected());
                notifyItemChanged(position);
                List<String> selectedSeats = new ArrayList<>();
                for (Seat s : seats) {
                    if (s.isSelected()) selectedSeats.add(s.getSeatId());
                }
                listener.onSeatSelected(selectedSeats);
            });
        }
    }

    @Override
    public int getItemCount() {
        return seats.size();
    }

    public interface OnSeatSelectedListener {
        void onSeatSelected(List<String> selectedSeats);
    }

    public static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView txtSeat;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSeat = itemView.findViewById(R.id.txt_seat);
        }
    }
}
