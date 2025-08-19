package chesslib.types;

import java.util.Arrays;

/**
 * Represents the current outcome or ongoing state of a chess game.
 * This includes win conditions, draws, and ongoing play.
 */
public enum GameStatus {
    ONGOING, // Game is still ongoing

    // Win conditions
    WHITE_WON_BY_CHECKMATE,
    BLACK_WON_BY_CHECKMATE,
    WHITE_WON_BY_TIME,
    BLACK_WON_BY_TIME,
    BLACK_WON_BY_RESIGNATION,
    WHITE_WON_BY_RESIGNATION,

    // Draw conditions
    DRAW_BY_STALEMATE,
    DRAW_BY_REPETITION,
    DRAW_BY_INSUFFICIENT_MATERIAL,
    DRAW_BY_REACH_RULE_50;

    /**
     * @return true if the game ended with a win for White.
     */
    public boolean isWhiteWon() {
        return this == WHITE_WON_BY_CHECKMATE || this == WHITE_WON_BY_TIME || this == WHITE_WON_BY_RESIGNATION;
    }

    /**
     * @return true if the game ended with a win for Black.
     */
    public boolean isBlackWon() {
        return this == BLACK_WON_BY_CHECKMATE || this == BLACK_WON_BY_TIME || this == BLACK_WON_BY_RESIGNATION;
    }

    /**
     * @return true if the game ended in any type of draw.
     */
    public boolean isDraw() { // todo write this in sympler way
        return this == DRAW_BY_STALEMATE || this == DRAW_BY_REPETITION
                || this == DRAW_BY_INSUFFICIENT_MATERIAL || this == DRAW_BY_REACH_RULE_50;
    }

    /**
     * @return true if the game ended by checkmate (regardless of winner).
     */
    public boolean isCheckmate() {
        return this == WHITE_WON_BY_CHECKMATE || this == BLACK_WON_BY_CHECKMATE;
    }

    /**
     * @return true if the game is no longer in progress (won or drawn).
     */
    public boolean isGameOver() {
        return this != ONGOING;
    }

    // return the enum getName but without the '_' and upper case letter
    public String getName(){
        return String.join(" ", name().toLowerCase().split("_"));
    }
}


