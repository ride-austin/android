package com.rideaustin.utils;

/**
 * Created by Sergey Petrov on 26/06/2017.
 */

public class MathUtils {

    /**
     * Same as {@link Long#compare(long, long)}
     * Original method is added in API 19
     */
    public static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * Find greater common divisor
     * https://stackoverflow.com/a/4009247
     */
    public static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * Find greater common divisor
     * https://stackoverflow.com/a/4009247
     */
    public static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * Check doubles difference is less than {@code tolerance}
     * https://stackoverflow.com/a/9090575
     */
    public static boolean almostEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) < tolerance;
    }
}
