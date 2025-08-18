package chesslib.types;

import static chesslib.types.Side.Value.*;
import static chesslib.types.Square.Value.*;

/**
 * The Direction class represents movement directions on a chessboard.
 * It defines constants for cardinal, diagonal, and knight directions,
 * and provides utility methods for working with them.
 */
public class Direction {
    // Cardinal directions (single-square)
    public static final int UP = 8;
    public static final int DOWN = -8;
    public static final int LEFT = -1;
    public static final int RIGHT = 1;

    // Diagonal directions (single-square)
    public static final int UP_LEFT = 7;
    public static final int UP_RIGHT = 9;
    public static final int DOWN_RIGHT = -7;
    public static final int DOWN_LEFT = -9;

    // Pawn jump directions (two-square)
    public static final int UP_TWICE = 16;
    public static final int DOWN_TWICE = -16;

    // Predefined direction arrays for each piece type (indexed by PieceType valueBy)
    // Index 0: dummy entry for NULL_PIECE_TYPE
    // Index 1: pawn (includes both forward and diagonal directions)
    // Index 2: knight (L-shaped jumps)
    // Index 3: bishop (diagonal)
    // Index 4: rook (straight)
    // Index 5: queen (all directions)
    // Index 6: king (all directions, one step)
    private static final int[][] allDirections = {
            {}, // no piece type
            {UP_LEFT, UP_RIGHT, UP, UP_TWICE, DOWN_LEFT, DOWN_RIGHT, DOWN, DOWN_TWICE}, // pawns directions for both sides
            {6, 10, 15, 17, -6, -10, -15, -17}, // knight directions
            {UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT}, // bishop
            {UP, DOWN, RIGHT, LEFT}, // rook
            {UP, DOWN, RIGHT, LEFT, UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT}, // queen
            {UP, DOWN, RIGHT, LEFT, UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT}, // king
    };

    /**
     * Returns the forward direction relative to the side.
     * Returns the direction for the pawn based on the side.
     *
     * @param side The side valueOf the pawn (WHITE_VAL or BLACK_VAL).
     * @return The direction for the pawn.
     */
    public static int forward(int side) {
        assert Side.isValid(side);
        return side == WHITE ? UP : DOWN;
    }

    /**
     * Returns all possible directions for a given piece type.
     *
     * @param pieceType The type valueOf the chess piece.
     * @return An array valueOf directions for the specified piece type.
     */
    public static int[] allDirections(int pieceType) {
        assert PieceType.isValid(pieceType);
        return allDirections[pieceType];
    }

    /**
     * Checks if a given direction is valid.
     *
     * @param direction The direction to check.
     * @return {@code true} if the direction is valid, {@code false} otherwise.
     */
    public static boolean isValid(int direction) {
        int rankInc = Math.abs(rankIncImp(direction));
        int fileInc = Math.abs(fileIncImp(direction));
        return rankInc + fileInc == 1       || // movement on straight lines.
                rankInc * fileInc == 1       || // movement on diagonal.
                rankInc == 0 && fileInc == 2 || // push twice.
                rankInc * fileInc == 2;         // knights movement.
    }

    /**
     * Returns the rank increment (delta row) for a given direction.
     *
     * @param direction The direction.
     * @return The rank increment.
     */
    public static int rankInc(int direction) {
        assert isValid(direction);
        return rankIncImp(direction);
    }

    /**
     * Returns the file increment (delta column) for a given direction.
     *
     * @param direction the movement direction
     * @return the file (column) increment for the direction
     */
    public static int fileInc(int direction) {
        assert isValid(direction);
        return fileIncImp(direction);
    }

    // Computes the rank (row) delta from a given direction using a central square to avoid overflow.
    private static int rankIncImp(int direction) {
        int midSq = E4; // we take the middle square to ensure not to get overflow
        return Square.rank(midSq + direction) - Square.rank(midSq);
    }

    // Computes the file (column) delta from a given direction using a central square to avoid overflow.
    private static int fileIncImp(int direction) {
        int midSq = E4; // we take the middle square to ensure not to get overflow
        return Square.file(midSq + direction) - Square.file(midSq);
    }
}
