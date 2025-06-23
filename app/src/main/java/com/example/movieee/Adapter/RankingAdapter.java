package com.example.movieee.Adapter;
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
import com.example.movieee.Booking.ChitietMovie_Activity;
import com.example.movieee.Model.Movie;
import com.example.movieee.R;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.MovieViewHolder> {
    private List<Movie> movieList;
    private Context context;

    public RankingAdapter(List<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_ranking, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.rating.setText("⭐ " + movie.getRating());
        holder.duration.setText("Thời lượng: " + movie.getDuration());

        Glide.with(context)
                .load(movie.getImageUrl())
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.poster);

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

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating, duration;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.img_poster);
            title = itemView.findViewById(R.id.txt_title);
            rating = itemView.findViewById(R.id.txt_rating);
            duration = itemView.findViewById(R.id.txt_duration);
        }
    }
}