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

    public HomeMovieAdapter(List<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_movie, parent, false);
        return new HomeMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());

        Glide.with(context)
                .load(movie.getImageUrl())
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.moviePoster);

        holder.moviePoster.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChitietMovie_Activity.class);
            intent.putExtra("movieId", movie.getMovieId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class HomeMovieViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView moviePoster;
        TextView movieTitle;

        public HomeMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.movie_poster);
            movieTitle = itemView.findViewById(R.id.movie_title);
        }
    }
}
