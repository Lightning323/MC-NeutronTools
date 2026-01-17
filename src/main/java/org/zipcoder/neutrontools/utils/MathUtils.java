package org.zipcoder.neutrontools.utils;

public class MathUtils {
    public static float clamp(float value, float min, float max) {
        if (value > max) return max;
        else if (value < min) return min;
        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value > max) return max;
        else if (value < min) return min;
        return value;
    }
}
