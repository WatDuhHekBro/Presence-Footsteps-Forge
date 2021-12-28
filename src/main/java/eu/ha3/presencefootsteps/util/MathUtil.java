package eu.ha3.presencefootsteps.util;

import net.minecraft.util.Mth;

import java.util.Random;

public class MathUtil {
    public static float randAB(Random rng, float a, float b) {
        return a >= b ? a : a + rng.nextFloat() * (b - a);
    }

    public static long randAB(Random rng, long a, long b) {
        return a >= b ? a : a + rng.nextInt((int) b + 1);
    }

    public static float between(float from, float to, float value) {
        return from + (to - from) * value;
    }

    public static float scalex(float number, float min, float max) {
        return Mth.clamp((number - min) / (max - min), 0, 1);
    }
}
