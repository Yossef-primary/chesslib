package chesslib.types;

import chesslib.Bitboard;

import java.util.regex.Pattern;

import static chesslib.types.Square.Value.NULL_SQUARE;

/**
 * The {@code Square} enum represents chess squares on a chessboard.
 */
public enum Square {
    A1, B1, C1, D1, E1, F1, G1, H1,
    A2, B2, C2, D2, E2, F2, G2, H2,
    A3, B3, C3, D3, E3, F3, G3, H3,
    A4, B4, C4, D4, E4, F4, G4, H4,
    A5, B5, C5, D5, E5, F5, G5, H5,
    A6, B6, C6, D6, E6, F6, G6, H6,
    A7, B7, C7, D7, E7, F7, G7, H7,
    A8, B8, C8, D8, E8, F8, G8, H8;



    // todo add explanation
    public int value() {
        return ordinal();
    }

    /**
     * Retrieves the file of the square.
     */
    public int file() {
        return file(value());
    }

    /**
     * Retrieves the rank of the square.
     */
    public int rank() {
        return rank(value());
    }

    /**
     * @return true if is the dark square otherwise false.
     */
    public boolean isDarkSquare(){
        return isDarkSquare(value());
    }

    public Square flipped() {
        return Square.getBy(flipped(value()));
    }

    public String getName(){
        return getName(value());
    }

    @Override
    public String toString() {
        return getName();
    }


    // --------            static constants and methods      ---------
    public static class Value {
        // Using for engine goals for fast operations.
        public static final int
                A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7,
                A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15,
                A3 = 16, B3 = 17, C3 = 18, D3 = 19, E3 = 20, F3 = 21, G3 = 22, H3 = 23,
                A4 = 24, B4 = 25, C4 = 26, D4 = 27, E4 = 28, F4 = 29, G4 = 30, H4 = 31,
                A5 = 32, B5 = 33, C5 = 34, D5 = 35, E5 = 36, F5 = 37, G5 = 38, H5 = 39,
                A6 = 40, B6 = 41, C6 = 42, D6 = 43, E6 = 44, F6 = 45, G6 = 46, H6 = 47,
                A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55,
                A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63,
                NULL_SQUARE = 64;
    }

    public static final int VALUES_COUNT = NULL_SQUARE + 1;
    private static final int FILE_MASK = 0b111;
    private static final int RANK_SHIFT = 3;
    public static final int BOARD_DIM = 8;


    /**
     * Calculates the distance between two squares.
     *
     * @param square1 The index of the first square.
     * @param square2 The index of the second square.
     * @return The distance between the two squares.
     */
    public static int distance(int square1, int square2) {
        return Math.abs(square1 - square2);
    }


    /**
     * Flips the square horizontally.
     *
     * @param square The index of the square to be flipped.
     * @return The index of the flipped square.
     */
    public static int flipped(int square) {
        return square ^ 56;
    }

    /**
     * Flips the square horizontally based on the side. only flip if side is black.
     *
     * @param side   The side (0 for white, 1 for black).
     * @param square The index of the square to be flipped.
     * @return The index of the flipped square.
     */
    public static int flippedIfBlack(int side, int square) {
        return (square ^ (56 * side));
    }

    /**
     * Retrieves the file of the square.
     *
     * @param square The index of the square.
     * @return The file of the square.
     */
    public static int file(int square) {
        assert isValid(square);
        return square & FILE_MASK;
    }

    /**
     * Retrieves the rank of the square.
     *
     * @param square The index of the square.
     * @return The rank of the square.
     */
    public static int rank(int square) {
        assert isValid(square);
        return square >> RANK_SHIFT;
    }

    public static Square getBy(String name) {
        return Square.getBy(valueBy(name));
    }

    public static Square getBy(int file, int rank) {
        return Square.getBy(valueBy(file, rank));
    }

    /**
     * Creates a {@code Square} based on the given square valueBy.
     *
     * @param squareVal The square valueBy.
     * @return The {@code Square} enum constant corresponding to the square valueBy.
     */
    public static Square getBy(int squareVal) {
        return isValid(squareVal) ? values()[squareVal] : null;
    }


    /**
     * Creates a square based on the square getName.
     *
     * @param name The getName of the square (e.g., "A1").
     * @return The index of the created square.
     */
    public static int valueBy(String name) {
        return isValid(name) ?
                valueBy(File.getBy(name.charAt(0)), Rank.getBy(name.charAt(1)))
                : NULL_SQUARE;
    }

    /**
     * Creates a square based on the file and rank indices.
     *
     * @param file The file index (0 to 7).
     * @param rank The rank index (0 to 7).
     * @return The index of the created square.
     */
    public static int valueBy(int file, int rank) {
        return File.isValid(file) && Rank.isValid(rank)
                ? (rank * Rank.VALUES_COUNT) + file
                : NULL_SQUARE;
    }


    // if square + direction go out from the board (for example H1 + right) it will reurn the square it self
    public static boolean canAddDirection(int square, int direction){
        return File.isValid(file(square) + Direction.fileInc(direction)) &&
                Rank.isValid(rank(square) + Direction.rankInc(direction));
    }

    // if square + direction go out from the board (for example H1 + right) it will reurn the square it self
    public static int addSafety(int square, int direction){
        return canAddDirection(square, direction) ? square + direction : square;
    }

    public static boolean isValid(int square){
        return (square & 63) == square;
    }

    public static boolean isValid(String squareName){
        return squareName != null
                && squareName.length() == 2
                && File.isValid(squareName.charAt(0))
                && Rank.isValid(squareName.charAt(1));
    }

    /**
     * Retrieves the getName of the square.
     *
     * @param square The index of the square.
     * @return The getName of the square.
     */
    public static String getName(int square) {
        return isValid(square)
                ? "" + File.getName(file(square)) + Rank.getName(rank(square))
                : null;
    }


    public static boolean isDarkSquare(int square){
        return (Bitboard.squareToBB(square) & Bitboard.DARK_SQUARES_BB) != 0;
    }


}
