package com.example.pricebenz;

public class Fuel {
    private final String name;
    private final double price;
    private final String type; // "[Точная]" или "[Средняя]"

    public Fuel(String name, double price, String type) {
        this.name = name;
        this.price = price;
        this.type = type;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return String.format("%-10s: %6.2f руб. %s", name, price, type);
    }
}