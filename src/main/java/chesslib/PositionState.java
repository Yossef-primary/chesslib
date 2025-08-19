package chesslib;

import chesslib.types.Side;

/**
 * Represents the state of a chess position, including information about the current game state.
 */
public class PositionState {
    public int kingSquare;          // Square index of the king
    public int numRepetition;       // Number of times the position has been repeated

    public int castlingRights;      // Castling rights for the position
    public int rule50;              // Rule 50 counter for the fifty-move rule
    public int enPassant;           // Square index for en passant capture, NO_SQUARE_VAL if none
    public int capturedPiece;       // Piece type that was captured in the last move, NULL_PIECE if none

    public long checkers;           // Bitboard representing squares attacked by opponent's pieces
    public long pinMask;            // Bitboard representing pinned pieces. by defoult is 0.
//

    public long key;                // Zobrist key for hashing the position
    public int ply;                 // Count of half-moves from the initial position
    public int lastMove = 0;        // Encoded valueBy of the last move made in the position

    public PositionState previous;  // Reference to the previous state (for move undo functionality)


    // maybe to apply letter

    public int enemyKingSquare;
    public long enemyBlocker; // or blocker for each side...
    public long[] enemyCheckedSquares;
}


/*

        public long[] pinMask2 = new long[Side.VALUES_COUNT];            // Bitboard representing pinned pieces
//    // pinMask2[sideToMove] use for calculate teh mask around the sideToMove king use for generating legal move
//    // pinMask2[flipped(sideToMove)] use for calculate teh mask around the opponent king (to detect checks)


 */
