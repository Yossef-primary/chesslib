package chesslib.types;

import static chesslib.types.Piece.Value.*;
import static chesslib.types.Side.Value.*;

/**
 * Enum representing all possible chess pieces (white and black).
 * Provides methods for piece conversion, validation, character representation, and side/type handling.
 */
public enum Piece {
    WHITE_PAWN  (Value.WHITE_PAWN),
    WHITE_KNIGHT(Value.WHITE_KNIGHT),
    WHITE_BISHOP(Value.WHITE_BISHOP),
    WHITE_ROOK  (Value.WHITE_ROOK),
    WHITE_QUEEN (Value.WHITE_QUEEN),
    WHITE_KING  (Value.WHITE_KING),

    BLACK_PAWN  (Value.BLACK_PAWN),
    BLACK_KNIGHT(Value.BLACK_KNIGHT),
    BLACK_BISHOP(Value.BLACK_BISHOP),
    BLACK_ROOK  (Value.BLACK_ROOK),
    BLACK_QUEEN (Value.BLACK_QUEEN),
    BLACK_KING  (Value.BLACK_KING);

    private final int pieceVal;

    Piece(int pieceVal) {
        this.pieceVal = pieceVal;
    }

    /**
     * Retrieves the type of the piece.
     *
     * @return The type of the piece.
     */
    public PieceType type() {
        return PieceType.getBy(type(pieceVal));
    }

    /**
     * Retrieves the side of the piece.
     *
     * @return The side of the piece.
     */
    public Side side() {
        return Side.getBy(side(pieceVal));
    }

    // Returns true if this piece is a white piece (based on its valueBy).
    public boolean isWhite(){
        return pieceVal < Value.BLACK_PAWN;
    }

    /**
     * Retrieves the getName of the piece.
     *
     * @return The getName of the piece.
     */
    public char getName() {
        return getName(pieceVal);
    }

    // Returns the integer valueBy associated with this piece.
    public int value(){
        return pieceVal;
    }


    // -----               Static methods               -----------

    public static class Value {
        public static final int
                NULL_PIECE = 0,
                WHITE_PAWN = 1,
                WHITE_KNIGHT = 2,
                WHITE_BISHOP = 3,
                WHITE_ROOK = 4,
                WHITE_QUEEN = 5,
                WHITE_KING = 6,

                BLACK_PAWN = 9, // for esy separeate all black for fast method so all white and black piece has same bits exept the forht bit on black
                BLACK_KNIGHT = 10,
                BLACK_BISHOP = 11,
                BLACK_ROOK = 12,
                BLACK_QUEEN = 13,
                BLACK_KING = 14;
    }
    // Used for allocating arrays indexed by piece values. Should be >= max piece valueBy + 1.
    public static final int VALUES_COUNT = Value.BLACK_KING + 1;


    // Lookup array to get Piece enum from piece valueBy.
    // Indexed by internal piece valueBy; nulls are placeholders.
    private static final Piece[] ALL_PIECES =
            {null, WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN, WHITE_KING,
                    null, null, BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN, BLACK_KING
            };

    // Flat list of all valid piece values for iteration or indexing.
    private static final int[] ALL_PIECE_VALUES = {
            Value.WHITE_PAWN, Value.WHITE_KNIGHT, Value.WHITE_BISHOP, Value.WHITE_ROOK, Value.WHITE_QUEEN, Value.WHITE_KING,
            Value.BLACK_PAWN, Value.BLACK_KNIGHT, Value.BLACK_BISHOP, Value.BLACK_ROOK, Value.BLACK_QUEEN, Value.BLACK_KING
    };


    public static boolean isValid(int piece) {
        return (piece >= Value.WHITE_PAWN && piece <= Value.WHITE_KING) || (piece >= Value.BLACK_PAWN && piece <= Value.BLACK_KING);
    }


    /**
     * Creates a {@code Piece} based on the given piece valueBy.
     *
     * @param pieceVal The piece valueBy.
     * @return The {@code Piece} enum constant corresponding to the piece valueBy, or {@code NO_PIECE} if out of bounds.
     */
    public static Piece getBy(int pieceVal) {
        return isValid(pieceVal) ? ALL_PIECES[pieceVal] : null;
    }

    public static Piece getBy(Side side, PieceType pieceType) {
        if (side == null || pieceType == null) return null;
        return Piece.getBy(valueBy(side.value(), pieceType.value()));
    }

    public static Piece getBy(char name) {
        return Piece.getBy(valueBy(name));
    }


    /**
     * Creates a piece based on the side and piece type.
     *
     * @param side      The side (0 for white, 1 for black).
     * @param pieceType The type of the piece.
     * @return The piece valueBy.
     */
    public static int valueBy(int side, int pieceType) {
        assert Side.isValid(side) && PieceType.isValid(pieceType);
        return (side << 3) | pieceType;
    }

    public static boolean isValid(char name){
        return "PNBRQKpnbrqk".contains(""+name);
    }

    /**
     * Creates a piece based on the character getName.
     *
     * @param name The character getName representing the piece.
     * @return The piece valueBy.
     */
    public static int valueBy(char name) {
        if (!isValid(name))
            return NULL_PIECE;

        int side = Character.isUpperCase(name) ? WHITE : BLACK;
        int pieceType = PieceType.valueBy(name);
        return valueBy(side, pieceType);
    }


    /**
     * Extracts the piece type valueBy (lower 3 bits) from the full piece valueBy.
     *
     * @param piece The piece valueBy.
     * @return The type of the piece.
     */
    public static int type(int piece) { // Piece.type
        return piece & 7;
    }

    /**
     * Extracts the side (0 = white, 1 = black) from the piece valueBy.
     *
     * @param piece The piece valueBy.
     * @return The side of the piece.
     */
    public static int side(int piece) {
        return (piece & 8) >> 3;
    }


    public static Character getName(int piece) {
        return isValid(piece)? "-PNBRQK--pnbrqk".charAt(piece) : null; // '-' is a placeholder for correct indexing.
    }

    /**
     * Toggles the side of the piece (white becomes black, black becomes white).
     *
     * @param piece The piece valueBy.
     * @return The piece valueBy with the flipped side.
     */
    public static int flipped(int piece) {
        return piece ^ 8;
    }

    /**
     * Flips piece side if `intoSide` is black. Returns unchanged if white.
     *
     * @param piece The piece valueBy.
     * @param intoSide The side to flip into (0 = white, 1 = black).
     * @return The piece valueBy possibly flipped.
     */
    public static int flippedIfBlack(int piece, int intoSide) {
        return piece ^ (intoSide << 3);
    }

    /**
     * Retrieves an array of all actual piece values (excluding no piece).
     *
     * @return An array of all actual piece values.
     */
    public static int[] intValues() {
        return ALL_PIECE_VALUES;
    }
}
