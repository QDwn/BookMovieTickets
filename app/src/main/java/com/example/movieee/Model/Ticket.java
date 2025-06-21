package com.example.movieee.Model;

import java.util.List;

public class Ticket {
    private String bookingId;
    private String movieId;
    private String movieTitle;
    private String ngayChieu;
    private String diaDiem;
    private String gioChieu;
    private List<String> gheDaChon;
    private int tongTienGhe;
    private int tongTienCombo;
    private int tongCong;
    private String userEmail;
    private String userName;
    private long timestamp;

    public Ticket() {
        // Default constructor required for Firebase
    }

    public Ticket(String bookingId, String movieId, String movieTitle, String ngayChieu, String diaDiem, String gioChieu, List<String> gheDaChon, int tongTienGhe, int tongTienCombo, int tongCong, String userEmail, String userName, long timestamp) {
        this.bookingId = bookingId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.ngayChieu = ngayChieu;
        this.diaDiem = diaDiem;
        this.gioChieu = gioChieu;
        this.gheDaChon = gheDaChon;
        this.tongTienGhe = tongTienGhe;
        this.tongTienCombo = tongTienCombo;
        this.tongCong = tongCong;
        this.userEmail = userEmail;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getNgayChieu() { return ngayChieu; }
    public void setNgayChieu(String ngayChieu) { this.ngayChieu = ngayChieu; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public String getGioChieu() { return gioChieu; }
    public void setGioChieu(String gioChieu) { this.gioChieu = gioChieu; }

    public List<String> getGheDaChon() { return gheDaChon; }
    public void setGheDaChon(List<String> gheDaChon) { this.gheDaChon = gheDaChon; }

    public int getTongTienGhe() { return tongTienGhe; }
    public void setTongTienGhe(int tongTienGhe) { this.tongTienGhe = tongTienGhe; }

    public int getTongTienCombo() { return tongTienCombo; }
    public void setTongTienCombo(int tongTienCombo) { this.tongTienCombo = tongTienCombo; }

    public int getTongCong() { return tongCong; }
    public void setTongCong(int tongCong) { this.tongCong = tongCong; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}