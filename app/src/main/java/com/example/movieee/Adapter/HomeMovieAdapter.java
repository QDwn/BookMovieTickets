package com.example.movieee.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieee.ChitietMovie_Activity;
import com.example.movieee.Model.Movie;
import com.example.movieee.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class HomeMovieAdapter extends RecyclerView.Adapter<HomeMovieAdapter.HomeMovieViewHolder> {

    private List<Movie> movieList;
    private Context context;
    private int layoutId;

    // Constructor mặc định — dùng layout item mặc định cũ
    public HomeMovieAdapter(List<Movie> movieList, Context context) {
        this(movieList, context, R.layout.activity_item_movie); // Gọi constructor chính
    }

    // Constructor mới — cho phép truyền layout tuỳ ý
    public HomeMovieAdapter(List<Movie> movieList, Context context, int layoutId) {
        this.movieList = movieList;
        this.context = context;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public HomeMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new HomeMovieViewHolder(view); // KHÔNG CẦN TRUYỀN layoutId VÀO ViewHolder nữa
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        // Sử dụng ID chung cho TextView tiêu đề phim
        if (holder.movieTitle != null) { // Đảm bảo TextView đã được tìm thấy
            holder.movieTitle.setText(movie.getTitle());
        }

        // Chỉ tải ảnh nếu ImageView tồn tại trong layout (tức là không phải layout tìm kiếm chỉ có chữ)
        if (holder.moviePoster != null) {
            Glide.with(context)
                    .load(movie.getImageUrl())
                    .placeholder(R.drawable.placeholder_poster)
                    .into(holder.moviePoster);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChitietMovie_Activity.class);
            intent.putExtra("movieId", movie.getMovieId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    // Phương thức mới để cập nhật danh sách phim
    public void updateList(List<Movie> newList) {
        movieList.clear();
        movieList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class HomeMovieViewHolder extends RecyclerView.ViewHolder {
        // Khai báo là null ban đầu để xử lý các layout không có View đó
        ShapeableImageView moviePoster = null;
        TextView movieTitle = null;

        public HomeMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            // Cố gắng tìm cả hai View. Nếu không tìm thấy, chúng sẽ vẫn là null.
            // Điều này an toàn hơn là cố gắng truyền layoutId vào đây.
            moviePoster = itemView.findViewById(R.id.movie_poster);
            movieTitle = itemView.findViewById(R.id.tv_movie_title_common); // SỬ DỤNG ID CHUNG MỚI
        }
    }
}