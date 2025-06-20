package com.example.movieee.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieee.Model.Movie;
import com.example.movieee.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Locale;

public class MovieManagementAdapter extends RecyclerView.Adapter<MovieManagementAdapter.MovieManagementViewHolder> {

    private List<Movie> movieList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Movie movie);
        void onDeleteClick(Movie movie);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MovieManagementAdapter(List<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieManagementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_movie_management, parent, false);
        return new MovieManagementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieManagementViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.tvMovieTitle.setText(movie.getTitle());
        holder.tvMovieReleaseDate.setText("Ngày chiếu: " + (movie.getReleaseDate() != null ? movie.getReleaseDate() : "N/A"));
        holder.tvMovieDirector.setText("Đạo diễn: " + (movie.getDirector() != null ? movie.getDirector() : "N/A"));
        holder.tvMovieRating.setText("Đánh giá: " + (movie.getRating() != null ? String.format(Locale.getDefault(), "%.1f ★", movie.getRating()) : "N/A"));

        Glide.with(context)
                .load(movie.getImageUrl())
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.ivMoviePoster);

        holder.btnEditMovie.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(movie);
            }
        });

        holder.btnDeleteMovie.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieManagementViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivMoviePoster;
        TextView tvMovieTitle, tvMovieReleaseDate, tvMovieDirector, tvMovieRating;
        ImageView btnEditMovie, btnDeleteMovie;

        public MovieManagementViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.ivMoviePosterManagement);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitleManagement);
            tvMovieReleaseDate = itemView.findViewById(R.id.tvMovieReleaseDate);
            tvMovieDirector = itemView.findViewById(R.id.tvMovieDirector);
            tvMovieRating = itemView.findViewById(R.id.tvMovieRating);
            btnEditMovie = itemView.findViewById(R.id.btnEditMovie);
            btnDeleteMovie = itemView.findViewById(R.id.btnDeleteMovie);
        }
    }
}