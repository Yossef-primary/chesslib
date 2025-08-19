package chesslib.types;

/**
 * Represents rank (row) constants and utility methods for a chessboard.
 * Ranks are indexed from 0 (rank '1') to 7 (rank '8').
 */
public class Rank {
    public static final int VALUES_COUNT = 8;

    public static final int NULL_RANK = -1;

    public static final int RANK_1 = 0;
    public static final int RANK_2 = 1;
    public static final int RANK_3 = 2;
    public static final int RANK_4 = 3;
    public static final int RANK_5 = 4;
    public static final int RANK_6 = 5;
    public static final int RANK_7 = 6;
    public static final int RANK_8 = 7;

    /**
     * Returns the character representation of the given rank (0–7 → '1'–'8').
     *
     * @param rank the rank index (0–7)
     * @return the corresponding character ('1'–'8')
     * @throws IllegalArgumentException if the rank is invalid
     */
    public static Character getName(int rank){
        return isValid(rank) ? (char) ('1' + rank) : null;
    }

    /**
     * Converts a character ('1'–'8') to a rank index (0–7).
     *
     * @param name the character representing the rank
     * @return the rank index (0–7)
     * @throws IllegalArgumentException if the character is invalid
     */
    public static int getBy(char name) {
        return isValid(name) ? name - '1' : NULL_RANK;
    }

    /**
     * Returns the rank valueBy flipped vertically if the side is black.
     *
     * @param side 0 for white, 1 for black
     * @param rank the original rank
     * @return the flipped rank if black, otherwise original
     */
    public static int flippedIfBlack(int side, int rank){
        return (rank ^ (7 * side));
    }

    /**
     * Checks whether the given rank index is within valid bounds (0–7).
     *
     * @param rank the rank index
     * @return true if the rank is valid, false otherwise
     */
    public static boolean isValid(int rank){
        return (rank & 7) == rank;
    }

    public static boolean isValid(char name){
        return name >= '1' && name <= '8';
    }

}
