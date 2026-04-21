package com.example.pricebenz;

public class TripCalculator {

    /**
     * Считает средний расход: (Потраченное топливо / Пройденный путь) * 100
     */
    public double calculateConsumption(double fuelVolume, int currentOdo, int prevOdo) {
        int distance = currentOdo - prevOdo;
        if (distance <= 0) return 0;
        return (fuelVolume / distance) * 100;
    }

    /**
     * Считает общую стоимость заправки
     */
    public double calculateTotalCost(double fuelVolume, double pricePerLiter) {
        return fuelVolume * pricePerLiter;
    }
}