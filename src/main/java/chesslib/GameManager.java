package chesslib;

import chesslib.exeptions.IllegalMoveExceptions;
import chesslib.exeptions.IllegalPositionException;
import chesslib.move.MoveGenerator;
import chesslib.move.MoveList;
import chesslib.types.*;
import chesslib.move.Move;
import static chesslib.types.Piece.Value.*;

//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Random;
import java.util.regex.Pattern;

import static chesslib.Bitboard.*;
import static chesslib.Bitboard.rankBB;
import static chesslib.move.Move.*;
import static chesslib.types.Piece.getName;
import static chesslib.types.PieceType.Value.*;

/**
 * Manages the state and logic valueOf a chess game.
 */
public class GameManager {
    private static final Pattern SAN_PATTERN = Pattern.compile(
            "(O-O(?:-O)?[+#]?|[KQRBN]([a-h]?[1-8]?)(x?)([a-h][1-8])([+#]?)|(([a-h]x)?[a-h][1-8])(=[QRBN])?[+#]?)"
    );

    public static final String FEN_FULLY_LEGAL = "Position is legal.";
    public static final String INVALID_FEN_SYNTAX = "Invalid fen! Fen syntax error!";
    public static final String INVALID_FEN_KING = "Invalid position! Position mast contains exactly one king for each side.";
    public static final String INVALID_FEN_CHECK = "Invalid position! Side to move cannot be capture opponent king.";
    public static final String INVALID_FEN_PAWNS = "Invalid position! Pawns cannot be placed on first or last rank.";

    //    private final Object positionLock = new Object(); // todo maybe need sycrinise mechaniim
    public static final String FEN_START_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private volatile Position position;

    // optional
    private volatile MoveList moveList;


//    private static void checkFen(String fen) {
//        String status = "Fen could not be null!";
//        if (fen == null || !(status = fenStatus(fen)).equals(FEN_FULLY_LEGAL)) {
//            throw new IllegalArgumentException(status);
//        }
//
//    }
//    public static String fenStatus2(String fen) { // todo need to support chess60
//        try {
//            new GameManager();
//            return FEN_FULLY_LEGAL;
//        }
//        catch (IllegalPositionException e){
//            return e.getMessage();
//        }
//    }

//    public static String fenStatus(String fen) { // todo need to support chess60
//        if (!FenValidation.isValidFenSyntax(fen)) {
//            return INVALID_FEN_SYNTAX;
//        }
//
//        if (!FenValidation.isValidKingsCount(fen)) {
//            return INVALID_FEN_KING;
//        }
//
//        if (!FenValidation.isValidPawnsPlacement(fen)) {
//            return INVALID_FEN_PAWNS;
//        }
//        Position pos = new Position(fen);
//        if (!pos.positionIsLegal()) {
//            return INVALID_FEN_CHECK;
//        }
//
//        return FEN_FULLY_LEGAL;
//    }

    public static boolean isFenRepresentLegalPosition(String fen) {
        try {
            new GameManager(fen);
            return true;
        } catch (IllegalPositionException e) {
            return false;

        }
    }


    /**
     * Constructs a game manager with the specified FEN position.
     *
     * @param fen the FEN position string
     */
    public GameManager(String fen) throws IllegalPositionException {
        setFen(fen, true);
    }


    /**
     * Constructs a game manager for a standard chess game.
     */
    public GameManager() {
        setFen(FEN_START_GAME, false);
    }

    /**
     * Sets the FEN position for the game manager.
     *
     * @param newFen the new FEN position string
     */
    public void setFen(String newFen) throws IllegalPositionException {
//        if (!FenValidation.isValidFenSyntax(newFen)){
//            throw new IllegalPositionException(INVALID_FEN_SYNTAX);
//        }
////        checkFe/n(newFen);
//        position = new Position(newFen);
//        position.positionIsLegalOrThrow();
//        moveList = null;
//        // todo set move list and game status to null
        setFen(newFen, true);
    }

    private void setFen(String newFen, boolean validateFen) {
        if (validateFen) {
            if (!FenValidation.isValidFenSyntax(newFen)) {
                throw new IllegalPositionException(INVALID_FEN_SYNTAX);
            }
            position = new Position(newFen);
            position.positionIsLegalOrThrow();
        } else {
            position = new Position(newFen);
        }
        moveList = null;
    }

    /**
     * Gets the current FEN position.
     *
     * @return the FEN position string
     */
    public String getFen() {
        return position.getFen();
    }

    /**
     * Gets the side to play in the current position.
     *
     * @return the side to play
     */
    public Side sideToPlay() {
        return Side.getBy(position.sideToMove());
    }


    private int toIntMove(Move m) {
        assert isValid(m);
        // assume only valid move (not null and not start / dest null)
        return position.toMove(m.start().value(),
                m.dest().value(),
                m.promotePT() == null ? NULL_PIECE_TYPE : m.promotePT().value()
        );

    }

    private boolean isValid(Move move) {
        return move != null && move.start() != null && move.dest() != null;
    }


    /**
     * Checks if a given move is legal in the current position.
     *
     * @param move the Move object representing the move
     * @return true if the move is legal, false otherwise
     */
    public boolean isLegalMove(Move move) {
        return isValid(move) && position.isFullyLegalMove(toIntMove(move));
    }

    public void makeMove(Move move) throws IllegalMoveExceptions {
        if (!isLegalMove(move)) {
            throw new IllegalMoveExceptions("On Move: " + move.getName() + "\n" + position.posString());
        }

        int m = toIntMove(move);
        position.makeMove(m);
        moveList = null;
    }


    /**
     * Undoes the last move made in the current position.
     * If no move was made, does nothing.
     */
    public void undoMove() {
        if (position.lastMove() != Move.NULL_MOVE) {
            position.undoMove();
            moveList = null;
        }
    }


    /**
     * Retrieves the last move made in the current position.
     *
     * @return the Move object representing the last move
     */
    public Move lastMove() {
        if (position.lastMove() == NULL_MOVE) {
            return null;
        }
        return new Move(position.getState().lastMove);
    }

    /**
     * Retrieves the move history valueOf the game.
     *
     * @return a list valueOf Move objects representing the move history
     */
    public List<Move> moveHistory() {
        return position.moveHistory().stream().map(Move::new).toList();
    }

    private MoveList getMoveList() {
        if (moveList == null) {
            moveList = new MoveList(position);
        }
        return moveList;
    }

    // ==========         move generation         ==============

    /**
     * Retrieves all legal moves in the current position.
     *
     * @return a list valueOf Move objects representing all legal moves
     */
    public List<Move> getAllLegalMoves() {
        List<Move> result = new ArrayList<>();
        for (int move : getMoveList()) {
            result.add(new Move(move));
        }
        return result;
    }


    /**
     * Retrieves all legal moves from a specific square in the current position.
     *
     * @param from the source square for the moves
     * @return a list of Move objects representing legal moves from the source square
     */
    public HashSet<Move> getAllLegalMoves(Square from) { // todo maybe modify this to list getBy square destination
        if (from == null) {
            throw new IllegalArgumentException("From square cannot be null");
        }
        HashSet<Move> result = new HashSet<>();
        Piece pieceOnFrom = getPiece(from);
        if (pieceOnFrom != null && pieceOnFrom.side() == sideToPlay()) {
            for (int move : getMoveList()) {
                if (Move.startSquare(move) == from.value())
                    result.add(new Move(move));
            }
        }
        return result;
    }


    // return list valueOf square that the piece on from square can move to. (all valid movment on empty board)
    public static Set<Move> allDestinations(Piece piece, Square from) {
        long moves = Bitboard.validDestinations(piece.side().value(), piece.type().value(), from.value());
        Set<Move> result = new HashSet<>();
        for (; moves != 0; moves &= (moves - 1))
            result.add(new Move(from, Square.getBy(lsbToSquare(moves))));
        return result;
    }


    /**
     * return the current status getBy the game.
     *
     * @return the GameStatus enum representing the game status
     */
    public GameStatus gameStatus() {
        MoveList moveList = getMoveList();
        return moveList.size() == 0 ? !position.inCheck() ? GameStatus.DRAW_BY_STALEMATE
                : sideToPlay() == Side.WHITE ? GameStatus.BLACK_WON_BY_CHECKMATE : GameStatus.WHITE_WON_BY_CHECKMATE
                : position.inInsufficientMaterial() ? GameStatus.DRAW_BY_INSUFFICIENT_MATERIAL
                : position.inVerifiedThreeFoldRepetition() ? GameStatus.DRAW_BY_REPETITION
                : position.inRule50() ? GameStatus.DRAW_BY_REACH_RULE_50
                : GameStatus.ONGOING;
    }


    /**
     * Retrieves the piece on a specified square in the current position.
     *
     * @param s the square to check
     * @return the Piece object representing the piece on the square
     */
    public Piece getPiece(Square s) {
        return s == null ? null : Piece.getBy(position.getPiece(s.value()));
    }


    /**
     * Immutable record representing a piece on a specific square.
     *
     * @param piece  the piece type and color
     * @param square the square the piece is on
     */
    public record PieceSquare(Piece piece, Square square) {
    }

    /**
     * Returns an array getBy all pieces on the board with their respective squares.
     *
     * @return array getBy PieceSquare representing all pieces on the board
     */
    public PieceSquare[] getAllPieces() {
        return extractPieceSquaresFromBitboard(position.occupancy());
    }

    /**
     * Returns all pieces getBy a given side with their respective squares.
     *
     * @param side the side (white or black)
     * @return array getBy PieceSquare for all pieces getBy the given side
     */
    public PieceSquare[] getAllPieces(Side side) {
        return extractPieceSquaresFromBitboard(position.occupancyBySide(side.value()));
    }

    /**
     * Helper method that extracts all pieces and their positions from a given bitboard.
     *
     * @param bitboard bitboard getBy occupied squares
     * @return array getBy PieceSquare from the given bitboard
     */
    private PieceSquare[] extractPieceSquaresFromBitboard(long bitboard) {
        PieceSquare[] result = new PieceSquare[Bitboard.bitCount(bitboard)];
        // Use an object reference to allow mutation getBy index inside lambda
        var indexRef = new Object() {
            int index = 0;
        };
        // Iterate over each set bit in the bitboard, extracting the piece and square
        Bitboard.forEachSquareIndex(bitboard, squareIndex -> {
            Square square = Square.getBy(squareIndex);
            Piece piece = getPiece(square);
            result[indexRef.index++] = new PieceSquare(piece, square);
        });
        return result;
    }


    /**
     * Returns all squares occupied by a given piece type (regardless getBy side).
     *
     * @param pieceType the piece type to look for (e.g. Knight, Bishop)
     * @return an array getBy squares where this piece type appears
     */
    public Square[] getSquaresByType(PieceType pieceType) {
        return Bitboard.getSquares(position.occupancyByType(pieceType.value()));
    }

    /**
     * Returns all squares occupied by a specific piece (with side).
     *
     * @param piece the specific piece (e.g. white rook, black pawn)
     * @return an array getBy squares where this piece appears
     */
    public Square[] getSquaresByPiece(Piece piece) {
        return Bitboard.getSquares(position.occupancyByPiece(piece.value()));
    }


    /**
     * Generates a FEN string for a Chess960 (Fischer Random Chess) starting position.
     * The piece arrangement is shuffled with specific constraints for Chess960.
     *
     * @return a FEN string representing a Chess960 starting position
     */
    private static String createChess960Fen() {
        Random r = new Random();
        char[] fenPieces = new char[8];
        char empty = '\0';

        // 1: Place bishops on opposite-colored squares
        fenPieces[2 * r.nextInt(0, 4)] = 'b'; // black square bishop
        fenPieces[2 * r.nextInt(0, 4) + 1] = 'b'; // light square bishop

        // 2: Shuffle all the rest valueOf the pieces
        List<Character> pieces = new ArrayList<>(Arrays.asList('n', 'n', 'q', 'r', 'r', 'r'));
        Collections.shuffle(pieces);

        // 3: Place the shuffled pieces on the fenPieces array
        int indPiece = 0, numRook = 0; // when the second rook appears, replace it with the king
        for (int i = 0; i < fenPieces.length; i++) {
            if (fenPieces[i] == empty) {
                fenPieces[i] = (pieces.get(indPiece) == 'r' && numRook++ == 1) ? 'k' : pieces.get(indPiece);
                indPiece++;
            }
        }

        // Combine the pieces array into a string and format the Chess960 FEN
        String fenPieceStr = new String(fenPieces);
        return String.format("%s/pppppppp/8/8/8/8/PPPPPPPP/%s w KQkq - 0 1",
                fenPieceStr, fenPieceStr.toUpperCase());
    }


    /**
     * Returns the number getBy times a specific piece appears on the board.
     *
     * @param p the piece to count
     * @return the count getBy the piece on the board
     */
    public int pieceCount(Piece p) {
        return position.pieceCount(p.value());
    }

    /**
     * Returns the number getBy times a piece type appears for a given side.
     *
     * @param side      the side to check
     * @param pieceType the type getBy piece to count
     * @return the count getBy the piece type for the side
     */
    public int pieceCount(Side side, PieceType pieceType) {
        return pieceCount(Piece.getBy(side, pieceType));
    }

    /**
     * Returns the number getBy half-moves made in the game.
     *
     * @return the number getBy half-moves
     */
    public int getNumMoves() {
        return position.getNumMoves();
    }

    /**
     * Returns the current en passant square, or null if not available.
     *
     * @return the en passant square, or null if not available
     */
    public Square enPassantSquare() {
        return Square.getBy(position.enPassant());
    }

    /**
     * Checks if the current side can castle on the specified side (short or long).
     *
     * @param isLong true for long (queenside) castling, false for short (kingside)
     * @return true if castling is available, false otherwise
     */
    public boolean canCastle(boolean isLong) {
        return position.canCastle(isLong);
    }

    /**
     * Checks if the given side can castle on the specified side (short or long).
     *
     * @param side   the side to check
     * @param isLong true for long (queenside) castling, false for short (kingside)
     * @return true if castling is available for the side, false otherwise
     */
    public boolean canCastle(Side side, boolean isLong) {
        return position.canCastle(side.value(), isLong);
    }

    /**
     * Returns the square getBy the king for the given side.
     *
     * @param side the side to check
     * @return the square getBy the king
     */
    public Square kingSquare(Side side) {
        return Square.getBy(position.squareOf(side.value(), KING));
    }

    /**
     * Returns the square getBy the king for the current side to move.
     *
     * @return the square getBy the king for the side to move
     */
    public Square kingSquare() {
        return kingSquare(sideToPlay());
    }

    /**
     * Returns the castling move for a side (short or long).
     *
     * @param side   the side to check
     * @param isLong true for long (queenside) castling, false for short (kingside)
     * @return the castling move, or null if not available
     */
    public Move getCastlingMove(Side side, boolean isLong) {
        return new Move(position.castlingMove(side.value(), isLong));
    }

    /**
     * Checks if the current side to move is in check.
     *
     * @return true if the side to move is in check, false otherwise
     */
    public boolean positionInCheck() {
        return position.inCheck();
    }

    /**
     * Prints the current board state to the console.
     */
    public void print() {
        position.printBoard();
    }


    // ========       san <-> move logic        =========


    public static boolean isValidBySyntaxMoveSan(String moveSan) {
        return SAN_PATTERN.matcher(moveSan).matches();
    }

    /**
     * Converts the last move made on the board into SAN.
     *
     * @return the SAN representation getBy the last move
     * @throws IllegalMoveExceptions if no move has been made
     */
    public String toSanLastMove() {
        final int lastMove = position.lastMove();
        if (lastMove == NULL_MOVE) {
            throw new IllegalMoveExceptions("There is no move to convert");
        }

        position.undoMove();
        StringBuilder result = new StringBuilder(buildSanWithoutSuffix(lastMove)); // fast, no recursion
        position.makeMove(lastMove);

        // Add check/mate suffix
        if (position.inCheck()) {
            result.append(MoveGenerator.hasAnyLegalMove(position) ? "+" : "#");
        }

        return result.toString();
    }

    /**
     * Converts a legal move to its Standard Algebraic Notation (SAN) representation.
     * Includes check or checkmate suffix.
     *
     * @param move the legal move to convert
     * @return the SAN string for the move
     * @throws IllegalMoveExceptions if the move is not legal in the current position
     */
    public String toSan(Move move) {
        if (!isLegalMove(move))
            throw new IllegalMoveExceptions("This method converts only legal moves!");

        final int intMove = toIntMove(move);
        StringBuilder result = new StringBuilder(buildSanWithoutSuffix(intMove));

        position.makeMove(intMove);
        if (position.inCheck()) {
            result.append(MoveGenerator.hasAnyLegalMove(position) ? "+" : "#");
        }
        position.undoMove();

        return result.toString();
    }

    /**
     * Filters and returns only the legal attackers to a destination square.
     * Returns a bitboard getBy attackers that can legally move to destSq.
     *
     * @param attackers bitboard getBy possible attacking pieces
     * @param destSq    the destination square
     * @return bitboard getBy attackers that can legally attack destSq
     */
    private long filterLegalAttackersTo(long attackers, int destSq) {
        long result = 0L;
        for (long temp = attackers; temp != 0; temp = Bitboard.popLsb(temp)) {
            final int startSq = Bitboard.lsbToSquare(temp);
            if (position.isLegalMove(Move.create(startSq, destSq))) {
                result |= Bitboard.squareToBB(startSq);
            }
        }
        return result;
    }

    /**
     * Builds the SAN string for a move before it is made on the board.
     * This does NOT include '+' or '#' suffix.
     * assume only legal move!
     */
    private String buildSanWithoutSuffix(int move) {
        final int start = startSquare(move);
        final int dest = destSquare(move);
        final int moveType = Move.moveType(move);
        final int piece = position.getPiece(start);
        final int pieceType = Piece.type(piece);

        final boolean isCapturing = position.getPiece(dest) != NULL_PIECE || moveType == EN_PASSANT;

        StringBuilder result = new StringBuilder();

        if (pieceType == PAWN) {
            // For pawn captures, prefix the origin file (e.g., exd5)
            if (isCapturing)
                result.append(File.getName(Square.file(start))).append("x");
            result.append(Square.getName(dest)); // Destination square (e.g., e4)

            if (moveType == PROMOTION) {
                result.append("=").append(Character.toUpperCase(PieceType.getName(promotePT(move))));
            }
            return result.toString();
        }

        if (moveType == CASTLING) {
            return start < dest ? "O-O" : "O-O-O";
        }

        // Handle normal non-pawn moves:

        // Prefix the piece letter (e.g., N, B, R, Q, K)
        result.append(Character.toUpperCase(getName(piece)));

        // Handle Ambiguity (disambiguation when two same-type pieces can move to dest)
        long ambiguitySamePieces = (position.attackersBB(dest, position.occupancy())
                & position.occupancyByPiece(piece))
                ^ Bitboard.squareToBB(start); // Exclude current piece

        ambiguitySamePieces = filterLegalAttackersTo(ambiguitySamePieces, dest);
        if (ambiguitySamePieces != 0) {
            long pieceOnSameFile = ambiguitySamePieces & fileBB(Square.file(start));
            long pieceOnSameRank = ambiguitySamePieces & rankBB(Square.rank(start));

            // Try to disambiguate using the file letter (e.g., Nbd2)
            if (pieceOnSameFile == 0) {
                result.append(File.getName(Square.file(start)));
            }
            // Else try disambiguating using the rank number (e.g., N3d2)
            else if (pieceOnSameRank == 0) {
                result.append(Rank.getName(Square.rank(start)));
            }
            // Else use full square notation to resolve ambiguity (e.g., Ng1f3)
            else {
                result.append(Square.getName(start));
            }
        }

        result.append(isCapturing ? "x" : "");
        result.append(Square.getName(dest));
        return result.toString();
    }


    // if move unavailable on a board / san syntax error return null
    public Move parseSan(String san) {
        try {
            return parseSanOrThrow(san);
        } catch (IllegalMoveExceptions e) {
            return null;
        }
    }

    /**
     * Converts a SAN (Standard Algebraic Notation) string to a legal move in the current position.
     * Throws InvalidSanException if the SAN is invalid or the move is illegal.
     *
     * @param san the SAN string to convert
     * @return the corresponding legal Move object
     * @throws IllegalMoveExceptions if the SAN is syntactically invalid or the resulting move is illegal
     */
    public Move parseSanOrThrow(String san) throws IllegalMoveExceptions {
        if (!isValidBySyntaxMoveSan(san)) {
            throw new IllegalMoveExceptions("Invalid syntax in move SAN: " + san);
        }

        String originalSan = san;
        PieceType promoteType = null;
        int pieceValue;
        long disambiguationMask = FULL_BB; // Used for filtering disambiguated source squares (e.g., Nbd2 vs. N1d2)
        int fromSquare, toSquare;
        int side = sideToPlay().value();
        char leadChar = san.charAt(0);

        // === Handle Castling ===
        if (leadChar == 'O') {
            boolean isLongCastling = san.startsWith("O-O-O");
            return new Move(position.castlingMove(side, isLongCastling));
        }

        // === Handle Piece Moves (e.g., Nf3, Raxb7) ===
        else if (Character.isUpperCase(leadChar)) {
            pieceValue = Piece.valueBy(leadChar);
            pieceValue = Piece.flippedIfBlack(pieceValue, side); // Adjust for side to play
            san = san.substring(1).replaceAll("[+#x]", ""); // Strip capture/check/mate symbols

            if (san.length() == 4) {
                // Fully disambiguated move (e.g., Nf3e5)
                fromSquare = Square.valueBy(san.substring(0, 2));
                toSquare = Square.valueBy(san.substring(2, 4));
            } else {
                // Handle partial disambiguation (e.g., Nbd2 or R1a3)
                if (san.length() == 3) {
                    char disambiguator = san.charAt(0);
                    disambiguationMask = Character.isDigit(disambiguator)
                            ? rankBB(Rank.getBy(disambiguator)) // Disambiguate by rank
                            : fileBB(File.getBy(disambiguator)); // Disambiguate by file
                    san = san.substring(1);
                }

                toSquare = Square.valueBy(san.substring(0, 2));

                // Compute legal source squares for the piece
                long candidateFromSquares = position.attackersByPiece(pieceValue, toSquare) & disambiguationMask;
                candidateFromSquares = filterLegalAttackersTo(candidateFromSquares, toSquare); // Filter illegal due to pins, etc.

                if (Bitboard.has2OrMoreBits(candidateFromSquares)) {
                    throw new IllegalMoveExceptions(String.format("Ambiguity error: multiple legal sources for move %s", originalSan));
                }
                fromSquare = Bitboard.lsbToSquare(candidateFromSquares);
            }
        }

        // === Handle Pawn Moves (e.g., e4, exd5, e8=Q) ===
        else {
            pieceValue = Piece.valueBy(side, PAWN);

            if (san.length() >= 3 && san.charAt(1) == 'x') {
                // Capture (e.g., exd5)
                disambiguationMask = fileBB(File.getBy(leadChar)); // Filter by source file
                san = san.substring(2);
                toSquare = Square.valueBy(san.substring(0, 2));
                long attackers = position.attackersByPiece(pieceValue, toSquare) & disambiguationMask;
                fromSquare = Bitboard.lsbToSquare(attackers);
            } else {
                // Push move (e.g., e4 or e8=Q)
                int forwardDir = Direction.forward(Side.flipped(side));
                toSquare = Square.valueBy(san.substring(0, 2));

                // Try single pawn push first, fallback to double push
                fromSquare = (position.getPiece(toSquare + forwardDir) != NULL_PIECE)
                        ? toSquare + forwardDir
                        : toSquare + 2 * forwardDir;
            }

            // Check for promotion (e.g., e8=Q)
            if (san.contains("=")) {
                int promoteIndex = san.indexOf('=') + 1;
                promoteType = Piece.getBy(san.charAt(promoteIndex)).type();
            }
        }

        // === Final Validation ===
        if (!Square.isValid(fromSquare)) {
            throw new IllegalMoveExceptions("Invalid move source square for SAN: " + originalSan);
        }

        Move move = new Move(Square.getBy(fromSquare), Square.getBy(toSquare), promoteType);

        if (!isLegalMove(move)) {
            throw new IllegalMoveExceptions("Illegal move in position for SAN: " + originalSan);
        }

        return move;
    }

}