package chesslib.move;

import chesslib.Position;
import chesslib.types.PieceType;
import chesslib.types.Square;

import java.util.Objects;
import java.util.regex.Pattern;

import static chesslib.types.File.*;
//import static chesslib.types.PieceType.NO_PIECE_TYPE;
import static chesslib.types.PieceType.Value.*;

/**
 * The {@code Move} class represents a chess move with information about the start square,
 * destination square, and optionally the promoted piece type.
 *
 * int move define like this:
     bits 0-5 first 6 bits is the square start move
     bits 6 - 11 the square dest move
    bits 12 - 14 is the move type (3 bits)
    bits 15 - 17 the promotion piece (3 bits)
 */
public record Move(Square start, Square dest, PieceType promotePT) {

    private static final Pattern movePattern = Pattern.compile("([a-h][1-8]){2}([nbrq])?");


    private static final int  START_MASK = 0b111111;
    private static final int  DEST_INDEX = 6;
    private static final int  MOVE_TYPE_INDEX = 12;
    private static final int  MOVE_TYPE_MASK = 0b111 << MOVE_TYPE_INDEX;
    private static final int  PROMOTE_PIECE_INDEX = 15;


    public static final int NULL_MOVE = 0;

    // move type constants reordering by the rearty getBy the moves. BECARFUL if u change the order
    // becuse there is method rely on the order

    /**
     * Move type constant for a normal move.
     */
    public static final int NORMAL = 0;

    /**
     * Move type constant for a normal pawn move.
     */
    public static final int NORMAL_PAWN_MOVE = 1 << MOVE_TYPE_INDEX;

    /**
     * Move type constant for a pawn push twice move.
     */
    public static final int PAWN_PUSH_TWICE = 2 << MOVE_TYPE_INDEX;

    /**
     * Move type constant for a castling move.
     */
    public static final int CASTLING = 3 << MOVE_TYPE_INDEX;


    /**
     * Move type constant for a promotion move.
     */
    public static final int PROMOTION = 4 << MOVE_TYPE_INDEX;

    /**
     * Move type constant for an en passant move.
     */
    public static final int EN_PASSANT = 5 << MOVE_TYPE_INDEX;


    public Move(Square start, Square dest){
        this(start, dest, null);
    }

    public Move(String moveName){
        this(Move.create(moveName));
    }

    public Move(int moveVal){
        this(Square.getBy(startSquare(moveVal)),
                Square.getBy(destSquare(moveVal)),
                PieceType.getBy(promotePT(moveVal)));
    }


    public String getName(){
        String sqStartStr = start == null ? "-" : start.getName();
        String sqDestStr = dest == null ? "-" : dest.getName();
        String promotePtSrt = promotePT == null ? "" : "" + promotePT.getName();

        return sqStartStr + sqDestStr + promotePtSrt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Move o && start == o.start && dest == o.dest && promotePT == o.promotePT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, dest, promotePT);
    }

    @Override
    public String toString() {
        return getName();
    }


    // ==============    static method           ===============

    public static int fromUci(String uciMove, Position position){
        int move = Move.create(uciMove);
        return position.toMove(startSquare(move), destSquare(move), promotePT(move));
    }

    // uci move represents castling in not cess960 as king startSq to king destSq.
    // so in that case we need to change the king dest square.
    public static String toUci(int move, Position position){
        if (moveType(move) == CASTLING && !position.isChess960()){
            int startSq = startSquare(move);
            int destSq = destSquare(move);
            // modify the dest square
            destSq = Square.valueBy(startSq < destSq ? FILE_G : FILE_C, Square.rank(startSq));
            move = Move.create(startSq, destSq, CASTLING);
        }
        return Move.getName(move);
    }

    /**
     * Creates a move with the specified start and destination squares.
     *
     * @param start The start square.
     * @param dest  The destination square.
     * @return The created move.
     */
    public static int create(int start, int dest) {
        assert Square.isValid(start) && Square.isValid(dest);
        return start | (dest << DEST_INDEX);
    }

    /**
     * Creates a move with the specified start, destination, and move type.
     *
     * @param start    The start square.
     * @param dest     The destination square.
     * @param moveType The move type.
     * @return The created move.
     */
    public static int create(int start, int dest, int moveType) {
        assert Square.isValid(start) && Square.isValid(dest) && isValidMoveType(moveType);
        return start | (dest << DEST_INDEX) | moveType;
    }


    /**
     * Creates a move with the specified start, destination, move type, and promoted piece type.
     *
     * @param start            The start square.
     * @param dest             The destination square.
     * @param moveType         The move type.
     * @param promotePieceType The promoted piece type.
     * @return The created move.
     */
    public static int create(int start, int dest, int moveType, int promotePieceType) {
        assert Square.isValid(start) && Square.isValid(dest)
                && isValidMoveType(moveType) &&  PieceType.isValidOrNull(promotePieceType);
        return start | (dest << DEST_INDEX) | moveType | (promotePieceType << PROMOTE_PIECE_INDEX);
    }


    public static int create(String moveName) {
        if (!isValidMoveName(moveName)) {
            throw new IllegalArgumentException("Illegal move getName!");
        }
        int start = Square.valueBy(moveName.substring(0, 2));
        int dest = Square.valueBy(moveName.substring(2, 4));

        int promotePT = moveName.length() < 5 ? 0 : PieceType.valueBy(moveName.charAt(4));


        return Move.create(start, dest, NORMAL, promotePT);
    }


    /**
     * Retrieves the start square from the given move.
     *
     * @param move The move encoded as an integer.
     * @return The start square.
     */
    public static int startSquare(int move) {
        return move & START_MASK;
    }

    /**
     * Retrieves the destination square from the given move.
     *
     * @param move The move encoded as an integer.
     * @return The destination square.
     */
    public static int destSquare(int move) {
        return (move >> DEST_INDEX) & START_MASK;
    }

    /**
     * Retrieves the move type from the given move.
     *
     * @param move The move encoded as an integer.
     * @return The move type.
     */
    public static int moveType(int move) {
        return move & MOVE_TYPE_MASK;
    }

    // include NORMAL, PAWN_PUSH_TWICE, NORMAL_PAWN_MOVE
    public static boolean isNormalPieceAndPawnOrPushTwice(int moveType){
        assert isValidMoveType(moveType);
        return moveType < CASTLING;
    }

    public static boolean isPawnMoveType(int moveType){
        return moveType != CASTLING && moveType != NORMAL;
    }


    /**
     * Retrieves the promoted piece type from the given move.
     *
     * @param move The move encoded as an integer.
     * @return The promoted piece type.
     */
    public static int promotePT(int move) {
        return move >> PROMOTE_PIECE_INDEX;
    }

    public static boolean isValidMoveType(int moveType){
        return ((moveType & MOVE_TYPE_MASK) == moveType) && (moveType <= EN_PASSANT);
    }

    public static boolean isValid(int move){
        return move != NULL_MOVE && isValidMoveType(moveType(move)) &&
                (promotePT(move) == NULL_PIECE_TYPE || isValidPromotePt(promotePT(move)));
    }

    public static boolean isValidPromotePt(int pieceType){
        return pieceType == KNIGHT || pieceType == BISHOP
                || pieceType == ROOK || pieceType == QUEEN;
    }

    public static boolean isValidMoveName(String moveName) {
        return moveName != null && movePattern.matcher(moveName).matches();
    }



    public static String moveTypeName(int move){
        int moveType = move & MOVE_TYPE_MASK;
        return switch (moveType){
            case NORMAL -> "NORMAL";
            case CASTLING -> "CASTLING";
            case NORMAL_PAWN_MOVE -> "NORMAL_PAWN_MOVE";
            case PAWN_PUSH_TWICE -> "PAWN_PUSH_TWICE";
            case PROMOTION -> "PROMOTION";
            case EN_PASSANT -> "EN_PASSANT";
            default -> "NO_MOVE_TYPE";
        };
    }


    public static String getName(int move) {
        return move == NULL_MOVE ? "null" : new Move(move).getName();
    }
}
