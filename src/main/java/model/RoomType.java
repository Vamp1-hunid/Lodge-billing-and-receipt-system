package com.example.lodgebilling.model; // CHANGED: Added .model

// Stores the business data: room choices and their base prices in Kwacha.
public enum RoomType {
    STANDARD("Standard", 350.0),
    DELUXE("Deluxe", 550.0),
    FAMILY("Family", 750.0),
    GRAND("Grand", 2300.0);

    private final String label;
    private final double priceKwacha;

    RoomType(String label, double priceKwacha) {
        this.label = label;
        this.priceKwacha = priceKwacha;
    }

    public double getPriceKwacha() { return priceKwacha; }

    @Override
    public String toString() { return label; }
}