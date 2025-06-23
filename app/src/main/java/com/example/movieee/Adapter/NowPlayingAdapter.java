package com.example.movieee.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Thêm import này

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieee.Booking.ChitietMovie_Activity;
import com.example.movieee.Model.Movie;
import com.example.movieee.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class NowPlayingAdapter extends RecyclerView.Adapter<NowPlayingAdapter.NowPlayingViewHolder> {
    private List<Movie> movies;
    private Context context;

    public NowPlayingAdapter(List<Movie> movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public NowPlayingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_movie, parent, false);
        return new NowPlayingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NowPlayingViewHolder holder, int position) {
        Movie movie = movies.get(position);
        Glide.with(context)
                .load(movie.getImageResId()) // lấy ảnh từ máy
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.moviePoster);

        // Sử dụng ID chung mới cho TextView tiêu đề phim
        if (holder.movieTitle != null) { // Thêm kiểm tra null để an toàn, mặc dù với layout này sẽ luôn có
            holder.movieTitle.setText(movie.getTitle()); // Đây là dòng 45 gây lỗi trước đó
        }


        holder.moviePoster.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChitietMovie_Activity.class);
            intent.putExtra("movieId", movie.getMovieId()); // truyền ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class NowPlayingViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView moviePoster;
        TextView movieTitle; // Khai báo TextView movieTitle

        public NowPlayingViewHolder(@NonNull View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.movie_poster);
            // Cập nhật để sử dụng ID chung mới
            movieTitle = itemView.findViewById(R.id.tv_movie_title_common); // ĐÃ THAY ĐỔI ID NÀY
        }
    }
}