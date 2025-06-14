package com.example.movieee.Model;

public class Movie {
    private String title;
    private int imageResId; // ảnh từ drawable
    private String imageUrl; // ảnh từ Firebase
    private String movieId;

    // Constructor cho NowPlaying
    public Movie(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    // Constructor cho Upcoming
    public Movie(String title, String imageUrl, String movieId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMovieId() {
        return movieId;
    }
}


