package com.example.movieapp;

public class Movie {
    private String title;
    private String movieId;
    private String imageUrl;

    public Movie(String title, String imageUrl, String movieId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMovieId() {
        return movieId;
    }
}

