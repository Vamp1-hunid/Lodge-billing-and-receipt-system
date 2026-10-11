package com.example.lodgebilling.model; // CHANGED: Added .model

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// The core business logic. Calculates totals, applies the 5% discount rule,
// and builds the text receipt. It knows NOTHING about JavaFX buttons or text fields.
public class Bill {
    public static final int DISCOUNT_MIN_NIGHTS = 5;
    public static final double DISCOUNT_RATE = 0.05;

    private final String id;
    private final String guest;
    private final RoomType room;
    private final int nights;
    private final CurrencyType currency;
    private final String notes;

    private final double ratePerNight;
    private final double roomCharge;
    private final double discount;
    private final double extras;
    private final double total;
    private final double paid;
    private final double balance;

    private final String issued = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH));

    public Bill(String id, String guest, RoomType room, int nights,
                CurrencyType currency, double extras, double paid, String notes) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.nights = nights;
        this.currency = currency;
        this.extras = extras;
        this.paid = paid;
        this.notes = notes;

        this.ratePerNight = currency.fromKwacha(room.getPriceKwacha());
        this.roomCharge = ratePerNight * nights;
        this.discount = (nights >= DISCOUNT_MIN_NIGHTS) ? roomCharge * DISCOUNT_RATE : 0.0;
        this.total = roomCharge - discount + extras;
        this.balance = total - paid;
    }

    // Getters used by the Controller and View to read the data
    public String getId() { return id; }
    public String getGuest() { return guest; }
    public RoomType getRoom() { return room; }
    public int getNights() { return nights; }
    public CurrencyType getCurrency() { return currency; }
    public double getTotal() { return total; }
    public double getPaid() { return paid; }
    public double getBalance() { return balance; }

    public String buildReceipt() {
        String line = "-".repeat(36) + "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("        LODGE BILLING LAB\n         OFFICIAL RECEIPT\n").append(line);
        sb.append(row("Bill no", id)).append(row("Date", issued)).append(row("Guest", guest));
        sb.append(row("Room", room + " x " + nights + " night(s)"));
        sb.append(row("Currency", currency.name())).append(line);
        sb.append(row("Rate per night", currency.format(ratePerNight)));
        sb.append(row("Room charge", currency.format(roomCharge)));
        if (discount > 0) sb.append(row("5% discount", "-" + currency.format(discount)));
        sb.append(row("Extras", currency.format(extras))).append(line);
        sb.append(row("TOTAL", currency.format(total)));
        sb.append(row("Paid", currency.format(paid)));
        sb.append(row("Balance", currency.format(balance)));
        sb.append(row("Status", Math.abs(balance) < 0.005 ? "PAID IN FULL" : "BALANCE DUE")).append(line);
        if (!notes.isEmpty()) sb.append("Notes: ").append(notes).append("\n").append(line);
        sb.append("   Thank you for staying with us!\n");
        return sb.toString();
    }

    private static String row(String label, String value) {
        return String.format("%-15s%21s%n", label, value);
    }
}