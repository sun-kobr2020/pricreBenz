package com.example.pricebenz;

public class FuelIdMapper {
    public static int getBaseId(int targetId) {
        return switch (targetId) {
            case 4 -> 3;   case 9 -> 8;
            case 12 -> 11; case 15 -> 14;
            default -> targetId;
        };
    }

    public static int getPlusId(int baseId) {
        return switch (baseId) {
            case 8 -> 9;   case 11 -> 12;
            case 14 -> 15; case 3 -> 4;
            default -> -1;
        };
    }
}