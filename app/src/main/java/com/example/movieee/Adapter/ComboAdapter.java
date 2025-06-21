package com.example.movieee.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieee.Model.ComboBN;
import com.example.movieee.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ComboAdapter extends RecyclerView.Adapter<ComboAdapter.ComboViewHolder> {
    private List<ComboBN> combos;
    private Context context;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public ComboAdapter(List<ComboBN> combos, Context context, OnQuantityChangeListener listener) {
        this.combos = combos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComboViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_combo, parent, false);
        return new ComboViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComboViewHolder holder, int position) {
        ComboBN combo = combos.get(position);
        holder.txtTen.setText(combo.getName());
        holder.txtMoTa.setText(combo.getDescription());

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.txtGia.setText(nf.format(combo.getPrice()) + "đ");
        holder.txtSoLuong.setText(String.valueOf(combo.getQuantity()));

        Glide.with(context).load(combo.getImageUrl()).into(holder.imgCombo);

        holder.btnCong.setOnClickListener(v -> {
            combo.setQuantity(combo.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onQuantityChanged();
        });

        holder.btnTru.setOnClickListener(v -> {
            if (combo.getQuantity() > 0) {
                combo.setQuantity(combo.getQuantity() - 1);
                notifyItemChanged(position);
                listener.onQuantityChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return combos.size();
    }

    public static class ComboViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCombo;
        TextView txtTen, txtMoTa, txtGia, txtSoLuong;
        Button btnCong, btnTru;

        public ComboViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCombo = itemView.findViewById(R.id.img_combo);
            txtTen = itemView.findViewById(R.id.txt_ten_combo);
            txtMoTa = itemView.findViewById(R.id.txt_mo_ta);
            txtGia = itemView.findViewById(R.id.txt_gia);
            txtSoLuong = itemView.findViewById(R.id.txt_so_luong);
            btnCong = itemView.findViewById(R.id.btn_cong);
            btnTru = itemView.findViewById(R.id.btn_tru);
        }
    }
}
