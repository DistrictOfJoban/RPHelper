package com.lx862.rphelper;

public class Util {
    public static double get1DecPlace(double d) {
        return Math.round(d * 10.0) / 10.0;
    }

    public static String decimalToHex(int decimal) {
        return "0x" + Integer.toHexString(decimal).toUpperCase();
    }

    public static int hexToDecimal(String hex) {
        return Integer.decode(hex);
    }
}
