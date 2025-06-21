package com.example.movieee.Model;

public class Seat {
    private String seatId;
    private boolean isBooked;
    private boolean isSelected;
    private boolean isDouble;
    private String type; // Stand, VIP, Couple

    public Seat(String seatId, boolean isBooked, boolean isDouble, String type) {
        this.seatId = seatId;
        this.isBooked = isBooked;
        this.isSelected = false;
        this.isDouble = isDouble;
        this.type = type;
    }

    public String getSeatId() {
        return seatId;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isDouble() {
        return isDouble;
    }

    public String getType() {
        return type;
    }
}
