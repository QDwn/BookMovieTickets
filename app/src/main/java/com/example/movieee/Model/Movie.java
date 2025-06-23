package com.example.movieee.Model;

import java.util.List;

public class Movie {
    private String title;
    private int imageResId; // For local drawables (like in Now Playing hardcoded list)
    private String imageUrl; // For Firebase image URLs (like in HomeMovieAdapter)
    private String movieId;
    private String description;
    private String trailerUrl;
    private String releaseDate;
    private String duration;
    private String director;
    private List<String> cast;
    private List<String> theLoai;
    private Double rating;

    public Movie() {
        // Default constructor required for calls to DataSnapshot.getValue(Movie.class)
    }

    // Constructor sử dụng cho about us
    public Movie(String title, int imageResId, String movieId) {
        this.title = title;
        this.imageResId = imageResId;
        this.movieId = movieId;
        // Initialize other fields to null or default values if not provided
        this.description = null;
        this.trailerUrl = null;
        this.releaseDate = null;
        this.duration = null;
        this.director = null;
        this.cast = null;
        this.rating = null;
    }

    // Constructor cho chi tiét thông tin phim
    public Movie(String title, String imageUrl, String movieId, String description,
                 String trailerUrl, String releaseDate, String duration,
                 String director, List<String> cast, List<String> theLoai, Double rating) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
        this.description = description;
        this.trailerUrl = trailerUrl;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.director = director;
        this.cast = cast;
        this.theLoai = theLoai;
        this.rating = rating;
    }

    public Movie(String title, String imageUrl, String movieId) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.movieId = movieId;
        // các giá trị còn lại để là null
        this.description = null;
        this.trailerUrl = null;
        this.releaseDate = null;
        this.duration = null;
        this.director = null;
        this.cast = null;
        this.theLoai = null;
        this.rating = null;
    }


    // Getters
    public String getTitle() { return title; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
    public String getMovieId() { return movieId; }
    public String getDescription() { return description; }
    public String getTrailerUrl() { return trailerUrl; }
    public String getReleaseDate() { return releaseDate; }
    public String getDuration() { return duration; }
    public String getDirector() { return director; }
    public List<String> getCast() { return cast; }
    public List<String> getTheLoai() { return theLoai; }
    public Double getRating() { return rating; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public void setDescription(String description) { this.description = description; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setDirector(String director) { this.director = director; }
    public void setCast(List<String> cast) { this.cast = cast; }
    public void setTheLoai(List<String> theLoai) { this.theLoai = theLoai; }
    public void setRating(Double rating) { this.rating = rating; }
}