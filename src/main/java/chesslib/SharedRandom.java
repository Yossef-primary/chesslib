package chesslib;

import java.util.Random;

/**
 * Singleton class for managing a shared random number generator with a fixed seed for consistency.
 */
public class SharedRandom {
    // Fixed seed for consistency.
    private static final long SEED = 999;//35672, 13,92538709

    // Shared random number generator instance.
    public static final java.util.Random RANDOM = new Random(SEED);
}

