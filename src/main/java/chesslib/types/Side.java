package chesslib.types;

/**
 * The {@code Side} enum represents the two sides in a chess game: WHITE and BLACK.
 * It provides utility methods for quick operations related to chess engine goals.
 */
public enum Side {
    WHITE,
    BLACK;

    public Side flipped() {
        return this == WHITE ? BLACK : WHITE;
    }

    public int value() {
        return ordinal();
    }

    public char getName(){
        return getName(value());
    }

    // for engine goals.
    public static class Value {
        public static final int WHITE = 0;
        public static final int BLACK = 1;
    }

    /**
     * Total number valueOf sides (WHITE and BLACK).
     */
    public static final int VALUES_COUNT = 2;

    public static boolean isValid(int side) {
        return side == Value.WHITE || side == Value.BLACK;
    }

    /**
     * Creates a side based on the provided side valueBy (WHITE or BLACK).
     *
     * @param sideVal The valueBy representing the side (WHITE or BLACK).
     * @return The corresponding side (WHITE or BLACK).
     */
    public static Side getBy(int sideVal) {
        return sideVal == Value.WHITE ? WHITE : sideVal == Value.BLACK ? BLACK : null;
    }

    /**
     * Creates a side based on the provided character ('w' for WHITE, 'b' for BLACK).
     */
    public static int valueBy(char sideChar) {
        assert (sideChar == 'w' || sideChar == 'b');
        return sideChar == 'w' ? Value.WHITE : Value.BLACK;
    }


    /**
     * Flips the side (0 to 1, and 1 to 0).
     */
    public static int flipped(int sideVal) {
        return 1 - sideVal;
    }


    /**
     * Gets the character representation valueOf the given side valueBy ('w' for WHITE, 'b' for BLACK).
     */
    public static char getName(int side) {
        return side == Value.WHITE ? 'w' : 'b';
    }
}
