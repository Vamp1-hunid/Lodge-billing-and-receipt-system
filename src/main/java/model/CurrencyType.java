package com.example.lodgebilling.model; // CHANGED: Added .model

import java.util.Locale;

// Locale.US forces Java to use a dot (.) for decimals instead of a comma (,).
// This prevents math errors if your Windows region settings use commas.
public enum CurrencyType {
    ZMW("Kwacha (K)", "K", 1.0),
    USD("US Dollar ($)", "$", 25.0);

    private final String label;
    private final String symbol;
    private final double kwachaPerUnit;

    CurrencyType(String label, String symbol, double kwachaPerUnit) {
        this.label = label;
        this.symbol = symbol;
        this.kwachaPerUnit = kwachaPerUnit;
    }

    public String getSymbol() { return symbol; }

    // Converts kwacha into this currency (e.g., 550 / 25 = $22.00)
    public double fromKwacha(double kwacha) {
        return kwacha / kwachaPerUnit;
    }

    // Formats numbers to 2 decimal places, e.g., "K 350.00"
    public String format(double amount) {
        return symbol + " " + String.format(Locale.US, "%.2f", amount);
    }

    @Override
    public String toString() { return label; }
}