package chesslib.types;

import chesslib.Bitboard;

import static chesslib.types.Side.Value.*;

/**
 * Bitmask utilities for castling rights.
 *
 * <p>Bit layout:</p>
 * <ul>
 *   <li>bit 0: WHITE_SHORT (K)</li>
 *   <li>bit 1: WHITE_LONG  (Q)</li>
 *   <li>bit 2: BLACK_SHORT (k)</li>
 *   <li>bit 3: BLACK_LONG  (q)</li>
 * </ul>
 *
 * <p>The castling mask (0..15) can be used directly as an index into the FEN table.</p>
 */
public class Castling {
    /** Number of possible castling masks (0..15). */
    public static final int SIZE = 16;

    /** White king-side (short) castling flag (K). */
    public static final int WHITE_SHORT = 0b0001;
    /** White queen-side (long) castling flag (Q). */
    public static final int WHITE_LONG   = 0b0010;
    /** Black king-side (short) castling flag (k). */
    public static final int BLACK_SHORT  = 0b0100;
    /** Black queen-side (long) castling flag (q). */
    public static final int BLACK_LONG   = 0b1000;

    /** Any white castling (K or Q). */
    public static final int ALL_WHITE   = WHITE_SHORT | WHITE_LONG;
    /** Any black castling (k or q). */
    public static final int ALL_BLACK   = BLACK_SHORT | BLACK_LONG;
    /** Mask of all castling flags. */
    public static final int ALL_CASTLING = 0b1111;
    /** Any short (king-side) castling (white or black). */
    public static final int ALL_SHORT = WHITE_SHORT | BLACK_SHORT;
    /** Any long (queen-side) castling (white or black). */
    public static final int ALL_LONG = WHITE_LONG | BLACK_LONG;

    /**
     * FEN encodings for every castling mask.
     * <p>Index equals the castling mask value (0..15). Example: 3 (0b0011) → "KQ".</p>
     */
    private static final String[] castlingOptionsFen = {
            "-",   // option 0
            "K",   //1
            "Q",   //2
            "KQ",  //3
            "k",   //4
            "Kk",  //5
            "Qk",  //6
            "KQk", //7
            "q",   //8
            "Kq",  //9
            "Qq",  //10
            "KQq", //11
            "kq",  //12
            "Kkq", //13
            "Qkq", //14
            "KQkq" //15
    };

    /**
     * Returns the FEN substring for the given castling mask (e.g., 0b0101 → "Kk").
     * <p>Precondition: {@link #isValid(int)} is true.</p>
     */
    public static String castlingName(int castlingRights){
        assert isValid(castlingRights);
        return castlingOptionsFen[castlingRights];
    }




    /**
     * Returns the short (king-side) castling flag for a side.
     * <p>Side: 0 = WHITE, 1 = BLACK. The shift maps white bits [0,1] to black bits [2,3].</p>
     */
    public static int shortCastling(int side){
        return WHITE_SHORT << (2 * side);
    }

    /**
     * Valid if no bits outside {@link #ALL_CASTLING} are set (i.e., range 0..15).
     */
    public static boolean isValid(int castleRight){
        return (castleRight & ALL_CASTLING) == castleRight;
    }



    /**
     * Returns the long (queen-side) castling flag for a side.
     * <p>Side: 0 = WHITE, 1 = BLACK.</p>
     */
    public static int longCastling(int side){
        return WHITE_LONG << (2 * side);
    }

    /**
     * Checks if the mask contains any short (king-side) castling (white or black).
     * Equivalent to: (castleRight & ALL_SHORT) != 0
     */
    public static boolean isShortCastle(int castleRight){
        return (castleRight & 5) != 0; //0b0101;
    }

    /**
     * Returns the side (WHITE/BLACK) for a single castling flag.
     * <p>Assumes input is a single flag (use {@link #isSingleCastlingRights(int)} to verify).</p>
     */
    public static int castlingSide(int castleRight) {
        return castleRight <= (WHITE_SHORT | WHITE_LONG) ? WHITE: BLACK;
    }

    /**
     * Both castling flags for the given side (e.g., WHITE → WHITE_SHORT | WHITE_LONG).
     */
    public static int bothCastling(int side){
        return side == WHITE? WHITE_SHORT | WHITE_LONG : BLACK_SHORT | BLACK_LONG;
    }

    /** Mask of all short (king-side) castling flags. */
    public static int shortCastling(){
        return WHITE_SHORT | BLACK_SHORT;
    }

    /** Mask of all long (queen-side) castling flags. */
    public static int longCastling(){
        return WHITE_LONG | BLACK_LONG;
    }

    /**
     * Both castling flags for the given side (alias of {@link #bothCastling(int)}).
     * <p>Left shift maps white mask to black when side = BLACK (1).</p>
     */
    public static int allCastling(int side) {
        return ALL_WHITE << (2 * side);
    }

    /**
     * Returns the castling flag for a side, choosing long/short by {@code isLong}.
     */
    public static int castlingRight(int side, boolean isLong){
        return isLong ? longCastling(side) : shortCastling(side);
    }

    /**
     * Returns true if the given castling right represents a single flag only
     * (e.g., WHITE_SHORT), not a combination like (WHITE_SHORT | WHITE_LONG).
     *
     * @param castleRight the castling right to test
     * @return true if exactly one castling right bit is set
     */
    public static boolean isSingleCastlingRights(int castleRight) {
        assert isValid(castleRight);
        return Bitboard.hasOneBit(castleRight);
    }

}