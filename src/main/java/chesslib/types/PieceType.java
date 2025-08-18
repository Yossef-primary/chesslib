package chesslib.types;

import static chesslib.types.PieceType.Value.*;

/**
 * Enum representing the different chess piece types.
 * Provides utility methods for value conversion, validation, and material evaluation.
 */
public enum PieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

    // Returns the integer value of this piece type, based on its ordinal.
    public int value() {
        return Value.PAWN + ordinal() ;
    }

    public char getName(){
        return getName(value());
    }

    // Returns the material value of this piece type, used in evaluation.
    public int materialValue(){
        return materialValue(value());
    }

    // Returns a Unicode chess symbol.
    public char getSymbol(boolean isFilled){
        return isFilled ?"♟♞♝♜♛♚".charAt(ordinal()) : "♙♘♗♖♕♔".charAt(ordinal());
    }

    // Integer constants representing internal values for each piece type.
    // IMPORTANT: Do not change the declaration order; some methods rely on it.
    public static class Value {
        public static final int
                NULL_PIECE_TYPE = 0,
                PAWN = 1,
                KNIGHT = 2,
                BISHOP = 3,
                ROOK = 4,
                QUEEN = 5,
                KING = 6;
    }

    // Total number of valid piece types, used for array indexing and internal operations.
    public static final int VALUES_COUNT = Value.KING + 1;

    // Integer values corresponding to each piece type, excluding NULL_PIECE_TYPE.
    private static final int[] ALL_PIECE_TYPE_VALUES = {
            Value.PAWN, Value.KNIGHT, Value.BISHOP, Value.ROOK, Value.QUEEN, Value.KING
    };

    // Material values used by the engine for evaluation. Indexed by piece type value.
    // Index 0 is a dummy (0). KING is Integer.MIN_VALUE because it is not counted in material.
    private static final int[] MATERIAL_VALUES = {Integer.MIN_VALUE, 1, 3, 3, 5, 9, Integer.MIN_VALUE};

    // Converts a character symbol (e.g., 'n' or 'Q') to its corresponding PieceType.
    public static PieceType getBy(char pieceTypeName){
        return getBy(valueBy(pieceTypeName));
    }

    // Returns the PieceType for the given integer value, or null if it is NULL_PIECE_TYPE.
    public static PieceType getBy(int pieceTypeVal){
        assert isValid(pieceTypeVal) || pieceTypeVal == NULL_PIECE_TYPE;
        return isValid(pieceTypeVal)? values()[pieceTypeVal - Value.PAWN] : null;
    }

    // Returns the basic material value of the given piece type in pawn units
    // (e.g., a rook equals 5 pawns, so returns 5).
    // For an invalid piece type or for the king, returns Integer.MIN_VALUE.
    public static int materialValue(int pieceType) {
        return isValid(pieceType) ? MATERIAL_VALUES[pieceType] : Integer.MIN_VALUE;
    }

    // Checks whether the given value represents a valid piece type.
    public static boolean isValid(int pieceType){
        return pieceType >= Value.PAWN && pieceType <= Value.KING;
    }

    public static boolean isValidOrNull(int pieceType){
        return pieceType >= NULL_PIECE_TYPE && pieceType <= Value.KING;
    }

    // Returns an array of all piece type integer values (excluding NULL_PIECE_TYPE).
    public static int[] intValues() {
        return ALL_PIECE_TYPE_VALUES;
    }

    // Returns the character symbol of a piece type (e.g., 1 -> 'p', 2 -> 'n').
    // Index 0 corresponds to a space (' ') for NULL_PIECE_TYPE.
    public static Character getName(int pieceType){
        return isValid(pieceType) ? "-pnbrqk".charAt(pieceType) : null; // '-' is a placeholder for correct indexing.
    }

    public static int valueBy(char name){
        name = Character.toLowerCase(name);
        return switch (name) {
            case 'p' -> Value.PAWN;
            case 'n' -> Value.KNIGHT;
            case 'b' -> Value.BISHOP;
            case 'r' -> Value.ROOK;
            case 'q' -> Value.QUEEN;
            case 'k' -> Value.KING;
            default -> NULL_PIECE_TYPE;
        };
    }

}