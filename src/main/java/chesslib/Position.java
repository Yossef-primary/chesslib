package chesslib;

import chesslib.exceptions.IllegalPositionException;
import chesslib.move.Move;
import chesslib.types.*;


import java.util.*;

import static chesslib.move.Move.*;
import static chesslib.Bitboard.*;
import static chesslib.types.Castling.*;
import static chesslib.types.File.*;
import static chesslib.types.Rank.*;
import static chesslib.types.PieceType.Value.*;
import static chesslib.types.Piece.Value.*;
import static chesslib.types.Side.Value.*;
import static chesslib.types.Square.Value.*;

/**
 * Represents a chess position and provides methods for manipulating the position state.
 * The class utilizes bitboards and arrays for an efficient board representation.
 * Key components include tracking the current state, managing the board, handling castling options,
 * and using Zobrist hashing for position keys.
 * The class supports Chess960 (Fischer Random Chess) rules and includes methods for making and undoing moves,
 * checking legality, generating FEN strings, setting positions from FEN strings, and more.
 */
public class Position {
    // Constants defining the conditions for a draw
    private static final int RULE_50_COUNT_TO_DRAW = 100; // i think the correct number is 100 not 99

    private byte[] repetitionList;
    private static final int REPETITION_TABLE_SIZE = 1 << 12;
    private static final int REPETITION_MASK = REPETITION_TABLE_SIZE - 1;
    private static final int NUM_REPETITION_TO_DRAW = 3;


    /**
     * Represents the current state of the chess position.
     */
    private int sideToMove;           // The side currently making a move (WHITE or BLACK)
    private int numMoves;             // Total number of moves made in the game // todo maybe we can replace it by using state.ply
    private PositionState state;      // Detailed state of the current position, including king info, castling rights, etc.


    // Board representation using bitboards and arrays

    // `occupancyBB` is a bitboard representing occupied squares on the board.
    private long occupancyBB;
    // `occupancyBySideBB` holds bitboards for each side separately (WHITE and BLACK).
    private long[] occupancyBySideBB;

    // `occupancyByPieceBB` represents bitboards for each piece.
    private long[] occupancyByPieceBB;

    // `occupancyByTypeBB` is an array of bitboards, one for each piece type.
    private long[] occupancyByTypeBB;

    // `board` is an array representing the piece on each square.
    private int[] board;

    // `isChess960` is a flag indicating whether Chess960 rules are in effect.
    private boolean isChess960;


    // counts the pieces by  index. I.E countPieces[WHITE_PAWN] gives the number white pawns on the board.
    private int[] piecesCount;


    // Castling-related data

    // `castlingOptionsMask` represents available castling options for each square.
    private int[] castlingOptionsMask;

    private int[] castlingDestSquareKing;

    private int[] castlingDestSquareRook;
    // `castlingMoves` is an array containing encoded castling moves.
    private int[] castlingMoves;

    // all the squares that king and rook go through while performing castling. Excludes the king square
    // and rook square. This path must be empty on the board when making the castling move.
    private long[] castlingPath;

    // all the squares the king goes through while performing castling. Excludes the king square. (we already now if the king square under attack)
    // This path cannot be under attack by enemy pieces.
    private long[] castlingKingPath;


    // Keys tables for position hashing
    // `pieceSquareKeys` holds random keys for each piece on each square.
    private static final long[][] pieceSquareKeys = new long[Piece.VALUES_COUNT][Square.VALUES_COUNT];
    // `enPassantKeys` holds random keys for en passant squares.
    private static final long[] enPassantKeys = new long[Square.VALUES_COUNT];
    // `castlingKeys` holds random keys for each castling option.
    private static final long[] castlingKeys = new long[Castling.SIZE];
    // `colorKey` holds a random key for the side to move.
    private static final long colorKey;


    // =======================
    // Initialization
    // =======================

    // Static block to initialize key tables
    // Random keys are generated for each table.
    static {
        Random r = SharedRandom.RANDOM;
        // Initialize keys for pieces on each square
        for (int piece : Piece.intValues())
            for (int square = A1; square <= H8; ++square)
                pieceSquareKeys[piece][square] = r.nextLong();

        // Initialize keys for castling options
        for (int i = 0; i < castlingKeys.length; ++i)
            castlingKeys[i] = r.nextLong();

        // Initialize keys for en passant squares
        for (int square = A1; square <= H8; ++square) {
            enPassantKeys[square] = r.nextLong();
        }
        // Special case for no en passant square
        enPassantKeys[NULL_SQUARE] = 0;

        // Initialize a random key for the side to move
        colorKey = r.nextLong();
    }


    /**
     * Creates a new chess position based on the provided FEN string, state, and Chess960 flag.
     *
     * @param newFen The FEN string representing the initial position of the chessboard.
     */
    public Position(String newFen) {
        // Initialize the position using the provided FEN string, state, and Chess960 flag
        setFen(newFen);
    }

    /**
     * Sets the position state based on the given FEN (Forsyth–Edwards Notation) string, along with additional
     * information about castling, en passant, rule50, and full move count. Assumes the provided FEN is valid.
     * <p>
     * FEN Format:
     * 1. Board state: The arrangement of pieces on the chessboard. Each rank is represented by a string of characters.
     *    Pieces are represented by their initials, and empty squares by a number representing the count of consecutive empty squares.
     * 2. Color to move: 'w' for white, 'b' for black.
     * 3. Castling rights: A combination of 'K', 'Q', 'k', 'q' to indicate whether castling is allowed for each side.
     *    'K' and 'k' represent kingside castling, 'Q' and 'q' represent queenside castling. If castling is not allowed, use '-'.
     * 4. En passant square: The square where an en passant capture is possible. Represented by the square's name, e.g., 'e3'. Use '-' if none.
     * 5. Rule50: The half-move clock, indicating the number of half-moves since the last capture or pawn move.
     * 6. Full move count: The number of the full move. Starts at 1 and increments after each move by black.
     * <p>
     * Validation and safety checks performed by this method:
     * - Ensures exactly one white king and one black king are present.
     * - Ensures no pawns are placed on the first or last rank.
     * - Validates that castling rights match the actual pieces on the board.
     * - Validates that the opponent king is not in check when the side to move is set.
     * - Sets up all internal data structures including occupancy bitboards, castling masks, en passant squares, repetition keys, and state.
     * <p>
     * Note: If any of the above validations fail, an {@link IllegalPositionException} is thrown.
     *
     * @param fen The FEN string representing the chess position.
     * @throws IllegalPositionException if the FEN represents an invalid or illegal chess position.
     */
    public void setFen(String fen) throws IllegalPositionException{
        assert FenValidation.isValidFenSyntax(fen);

        // 0. pars fen
        String[] fenParts = splitFen(fen);
        String boardFen = fenParts[0], colorFen = fenParts[1], castlingFen = fenParts[2],
                epFen = fenParts[3], rule50Fen = fenParts[4], fullMoveCountFen = fenParts[5];

        // 0. init class main data
        repetitionList = new byte[REPETITION_TABLE_SIZE]; // 2^14;
//        isChess960 = isChess960Mod;
        state = new PositionState();
        sideToMove = Side.valueBy(colorFen.charAt(0));
        numMoves = 2 * (Integer.parseInt(fullMoveCountFen) - 1) + sideToMove; // num moves start from 0

        // 1. reset the board
        occupancyBB = 0;
        occupancyBySideBB = new long[Side.VALUES_COUNT];
        occupancyByPieceBB = new long[Piece.VALUES_COUNT];
        occupancyByTypeBB = new long[PieceType.VALUES_COUNT];
        board = new int[Square.VALUES_COUNT];
        piecesCount = new int[Piece.VALUES_COUNT];

        castlingOptionsMask = new int[Square.VALUES_COUNT];
        castlingDestSquareKing = new int[Square.VALUES_COUNT];
        castlingDestSquareRook = new int[Square.VALUES_COUNT];
        castlingPath = new long[Square.VALUES_COUNT];
        castlingKingPath = new long[Square.VALUES_COUNT];
        castlingMoves = new int[Castling.SIZE];

        // 2. set the board
        int square = A8;
        for (char c : boardFen.toCharArray()) {
            if (c == '/')
                square = square - 2 * Rank.VALUES_COUNT; // go down in a rank
            else if (Character.isDigit(c))
                square += Character.getNumericValue(c);
            else // add piece and advance square
                addPiece(Piece.valueBy(c), square++);
        }

        // validate kings counts and pawn placement:
        if (pieceCount(WHITE_KING) != 1 || pieceCount(BLACK_KING) != 1){
            throw new IllegalPositionException("Invalid number of kings");
        }

        if ((occupancyByType(PAWN) & (RANK_1_BB | RANK_8_BB)) != 0) {
            throw new IllegalPositionException("Pawn on first or last rank");
        }


        int[] kingsSquares = {squareOf(WHITE_KING), squareOf(BLACK_KING)};

        // 3. set castling info. this method ignore the castling right if is not available on the board
        // so position base be with valid castling rights.
        isChess960 = true; // for begineng try to found any 960 castling
        int castlingRights = 0;
        Arrays.fill(castlingOptionsMask, ALL_CASTLING);

        castlingOptionsMask[kingsSquares[WHITE]] = ALL_CASTLING - (WHITE_SHORT | WHITE_LONG);
        castlingOptionsMask[kingsSquares[BLACK]] = ALL_CASTLING - (BLACK_SHORT | BLACK_LONG);

        char[] castlingChars = castlingFen.toCharArray();
        Arrays.sort(castlingChars);

        for (char c : castlingChars) {
            int side = Character.isUpperCase(c) ? WHITE : BLACK;
            c = Character.toLowerCase(c);

            int rookSq, kingSq = kingsSquares[side];
            int relativeRank = Rank.flippedIfBlack(side, RANK_1);
            long rooksOnFirstRank = occupancyBySideAndType(side, ROOK) & Bitboard.rankBB(relativeRank);

            // find the rookSq square, that made the castle.
            if (c == 'k') {
                rookSq = Bitboard.msbToSquare(rooksOnFirstRank);
            } else if (c == 'q') {
                rookSq = Bitboard.lsbToSquare(rooksOnFirstRank); // rook sq might be NULL_SQUARE and its verify on function isCastlingAvailableOnPosition
            } else if (c >= 'a' && c <= 'h')
                rookSq = Square.valueBy(File.getBy(c), Rank.flippedIfBlack(side, Rank.RANK_1));
            else break; // c = "-"

            int castleRight = rookSq > kingSq ? shortCastling(side) : longCastling(side);

            // Avoid adding duplicate short/long castling rights for the same side.
            // Only set the first valid castling right found, and ensure it is actually
            // possible on the current board position; otherwise, skip it.
            if ((castlingRights & castleRight) == 0 && isCastlingAvailableOnPosition(castleRight, kingSq, rookSq)) { // todo make shure that check not make bug

                int destKing = isShortCastle(castleRight) ? Square.flippedIfBlack(side, G1) : Square.flippedIfBlack(side, C1);
                int destRook = isShortCastle(castleRight) ? Square.flippedIfBlack(side, F1) : Square.flippedIfBlack(side, D1);

                //update castle right info
                castlingRights += castleRight;
                castlingOptionsMask[rookSq] = ALL_CASTLING - castleRight;
                castlingDestSquareKing[rookSq] = destKing;
                castlingDestSquareRook[rookSq] = destRook;
                castlingKingPath[rookSq] = pathBetween(kingSq, destKing) & ~squareToBB(kingSq);
                castlingPath[rookSq] = (castlingKingPath[rookSq] | pathBetween(rookSq, destRook)) & ~squareToBB(rookSq);
                castlingMoves[castleRight] = Move.create(kingSq, rookSq, CASTLING);
            }
        }

        isChess960 = hasChess960CastlingAvailable(); // after we set castling need to check

        // 4. set epPassant sq.
        int enSq = "-".equals(epFen) ? NULL_SQUARE : Square.valueBy(epFen);

        // 5. set state
        state.rule50 = Integer.parseInt(rule50Fen);
        state.kingSquare = kingsSquares[sideToMove];
        state.capturedPiece = NULL_PIECE;
        state.numRepetition = 0;
        state.ply = 0;
        state.previous = null;
        state.castlingRights = castlingRights;
        state.checkers = attackersBB(Side.flipped(sideToMove), state.kingSquare, occupancyBB);
        state.pinMask = pinMask(Side.flipped(sideToMove), state.kingSquare);
        state.enPassant = isValidEpSquare(enSq) ? enSq : NULL_SQUARE;

        state.key ^= ((sideToMove * colorKey) ^ enPassantKeys[state.enPassant] ^ castlingKeys[castlingRights]);
        state.lastMove = NULL_MOVE;

        ++repetitionList[(int) (REPETITION_MASK & state.key)];

        // The opponent side to move must not be in check.
        if (attackersBB(sideToMove, squareOf(Side.flipped(sideToMove), KING), occupancy()) != 0)
            throw new IllegalPositionException("King not in the side to move is under attack");

        assert positionIsLegal();
    }

    /**
     * Prints the current chess position board representation along with additional information.
     */
    public void printBoard() {
        System.out.println(posString());
    }


    public int pieceCount(int piece) {
        assert Piece.isValid(piece);
        return piecesCount[piece];
    }

    public int pieceTypeCount(int pieceType) {
        return piecesCount[Piece.valueBy(WHITE, pieceType)] + piecesCount[Piece.valueBy(BLACK, pieceType)];
    }


    // =======================
    // Castling: queries & helpers
    // =======================

    /**
     * Checks if a specific castling right is allowed in the current position.
     *
     * @param castlingRights the castling right to check (e.g., WHILE_SHORT, WHITE_LONG, BLACK_SHORT, BLACK_LONG)
     * @return true if the castling right is allowed, false otherwise
     */
    public boolean canCastle(int castlingRights) {
        return (state.castlingRights & castlingRights) != 0;
    }

    public boolean canCastle(int side, boolean isLong) {
        return canCastle(Castling.castlingRight(side, isLong));
    }

    public boolean canCastle(boolean isLong) {
        return canCastle(Castling.castlingRight(sideToMove, isLong));
    }

    /**
     * Get the file of the rook involved in castling.
     *
     * @param castleRight The castling right representing the castling move.
     * @return The file of the rook involved in castling.
     */
    private int castlingRookFile(int castleRight) {
        return Square.file(destSquare(castlingMove(castleRight)));
    }

    /**
     * Checks whether the king and rook are correctly positioned on the board
     * for the given single castling right, taking into account standard or Chess960 placement.
     * <p>
     * This does not verify castling legality (e.g., square clearance or checks),
     * only that the king and rook are where they should be.
     *
     * @param castleRight the single castling right to check (must be one of: WHITE_SHORT, WHITE_LONG, BLACK_SHORT, BLACK_LONG)
     * @param kSq         the square index of the king
     * @param rSq         the square index of the rook
     * @return true if the castling right is valid based on piece placement
     */
    public boolean isCastlingAvailableOnPosition(int castleRight, int kSq, int rSq) {
        if (!Castling.isSingleCastlingRights(castleRight)) {
            throw new IllegalArgumentException("castleRight must be one getBy: WHITE_SHORT, WHITE_LONG, BLACK_SHORT, BLACK_LONG");
        }
        if (!Square.isValid(kSq) || !Square.isValid(rSq))
            return false;
        int side = castlingSide(castleRight);
        boolean isShort = isShortCastle(castleRight);

        return getPiece(kSq) == Piece.valueBy(side, KING) &&
                getPiece(rSq) == Piece.valueBy(side, ROOK) &&
                Rank.flippedIfBlack(side, RANK_1) == Square.rank(kSq) && // king on correct rank
                Rank.flippedIfBlack(side, RANK_1) == Square.rank(rSq) && // rook on correct rank
                (isShort == (kSq < rSq)) && // king must be to the left/right of rook depending on castling side
                (squareToBB(kSq) & Bitboard.CORNERS_BB) == 0 && // Not support castling when king on the korner
                (isChess960 ||
                        // extra check on normal check to make sure that king and rook in correct placement
                        (rSq == Square.flippedIfBlack(side, isShort ? H1 : A1) && kSq == Square.flippedIfBlack(side, E1)));
    }


    /**
     * @param side white or black
     * @return the castling move by those parameter.
     */
    public int castlingMove(int side, boolean isLong) {
        return castlingMove(Castling.castlingRight(side, isLong));
    }

    public int castlingMove(boolean isLong) {
        return castlingMove(sideToMove, isLong);
    }


    // Returns NULL_MOVE if the castleRight is not available on init
    public int castlingMove(int castleRight) {
        return castlingMoves[castleRight];
    }

    public boolean hasChess960CastlingAvailable() {
        int whiteShort = castlingMove(Castling.WHITE_SHORT);
        int whiteLong = castlingMove(WHITE_LONG);
        int blackShort = castlingMove(BLACK_SHORT);
        int blackLong = castlingMove(BLACK_LONG);

        // Standard (classical) mappings
        int whiteShortStd = Move.create(E1, H1, CASTLING);
        int whiteLongStd = Move.create(E1, A1, CASTLING);
        int blackShortStd = Move.create(E8, H8, CASTLING);
        int blackLongStd = Move.create(E8, A8, CASTLING);

        // true if any available castling move differs from the classical mapping
        return (whiteShort != NULL_MOVE && whiteShort != whiteShortStd)
                || (whiteLong != NULL_MOVE && whiteLong != whiteLongStd)
                || (blackShort != NULL_MOVE && blackShort != blackShortStd)
                || (blackLong != NULL_MOVE && blackLong != blackLongStd);
    }


    // =======================
    // Make/undo move handling
    // =======================

    public void makeMove(int move) {
        makeMove(move, new PositionState());
    }

    /**
     * Executes a legal move on the current position and updates all relevant state.
     * Assumes the move is fully legal and valid.
     * <p>
     * The move process consists of:
     * <p>
     * 1. State Update:
     * - Initializes the new position state.
     * - Updates captured piece, en passant square, 50-move rule counter, and zobrist key.
     * - Adjusts castling rights and keys if needed.
     * - Sets the new state as current.
     * <p>
     * 2. Board Update:
     * - Executes the move based on its type: normal, castling, promotion, en passant, etc.
     * - Moves, adds, or removes pieces accordingly.
     * - Resets the 50-move counter and sets en passant square if applicable.
     * <p>
     * 3. King Info Update:
     * - Updates king square, checkers bitboard, and pin mask for the side to move.
     * <p>
     * 4. Repetition Tracking:
     * - Updates internal repetition info to support threefold repetition detection.
     * <p>
     * 5. Finalization:
     * - Records the last move.
     * - Verifies resulting position is still legal.
     *
     * @param move     Encoded representation of the move.
     * @param newState Reusable or fresh PositionState representing the resulting position.
     */
    public void makeMove(int move, PositionState newState) {//, PositionState newState
        assert isFullyLegalMove(move) : Move.getName(move) + "\n" + posString();

        int start = startSquare(move);
        int dest = destSquare(move);
        int moveType = moveType(move);
        int sideMoved = sideToMove;

        // 1: Update the new state.

        newState.capturedPiece = NULL_PIECE;
        newState.enPassant = NULL_SQUARE;
        newState.rule50 = state.rule50 + 1;

        newState.ply = state.ply + 1;
        newState.castlingRights = state.castlingRights; // todo
        newState.key = state.key ^ enPassantKeys[state.enPassant] ^ colorKey; // Note: enPassantKeys[NULL_SQUARE] = 0

        // Update castling key if needed and update castling rights
        if (state.castlingRights != 0
                && (state.castlingRights & castlingOptionsMask[start] & castlingOptionsMask[dest]) != state.castlingRights) {
            newState.castlingRights &= (castlingOptionsMask[start] & castlingOptionsMask[dest]);
            newState.key ^= (castlingKeys[state.castlingRights] ^ castlingKeys[newState.castlingRights]);
        }

        // Update the state. This must be called before the board update because the remove-add-move piece method
        // changes the key of the state. It establishes the new state as the current state for subsequent updates.
        newState.previous = state;
        state = newState;
        sideToMove = Side.flipped(sideToMove);
        numMoves++;

        // 2: Update the board

        int pieceOnDest = getPiece(dest);
        if (pieceOnDest != NULL_PIECE && moveType != CASTLING) { // not include enPassant move that we handle letter
            state.capturedPiece = pieceOnDest;
            removePiece(dest);
            state.rule50 = 0;
        }
        if (moveType == NORMAL) {
            movePiece(start, dest);
        } else if (moveType == NORMAL_PAWN_MOVE) {
            movePiece(start, dest);
            state.rule50 = 0;
        } else if (moveType == Move.PAWN_PUSH_TWICE) {
            movePiece(start, dest);
            int epSquare = start + Direction.forward(sideMoved);
            if ((occupancyBySideAndType(sideToMove, PAWN) & pawnAttacks(sideMoved, epSquare)) != 0) {
                state.enPassant = epSquare;
                state.key ^= enPassantKeys[epSquare];
            }
            state.rule50 = 0;
        } else if (moveType == Move.CASTLING) { // Note: Castling move encoded dest to sq of rook
            movePiece(start, castlingDestSquareKing[dest]);
            movePiece(dest, castlingDestSquareRook[dest]);
        } else if (moveType == Move.PROMOTION) {
            addPiece(Piece.valueBy(sideMoved, promotePT(move)), dest);
            removePiece(start);
            state.rule50 = 0;
        } else if (moveType == Move.EN_PASSANT) {
            int captureSq = dest - Direction.forward(sideMoved);
            state.capturedPiece = getPiece(captureSq);

            removePiece(captureSq);
            movePiece(start, dest);

            state.rule50 = 0;
        }

        // 3: Update the current king data info

        int kingSq = squareOf(sideToMove, KING);
        state.kingSquare = kingSq;
        state.checkers = attackersBB(sideMoved, kingSq, occupancyBB);
        state.pinMask = pinMask(Side.flipped(sideToMove), kingSq);
        state.lastMove = move;


        // Update repetition information to handle threefold repetition rule
        updateRepetition();

        // Ensure the resulting position is legal
        assert positionIsLegal() : positionIsLegalOrThrow();
    }

    /**
     * Undoes the last chess move in the current position and reverts the internal state accordingly.
     * Assumes that the move is legal and has been previously made using the makeMove method.
     */
    public void undoMove() {
        // first remove the key fromm repetitionList (before it change)
        --repetitionList[(int) (state.key & REPETITION_MASK)];

        int move = state.lastMove;
        assert move != NULL_MOVE;

        int start = startSquare(move);
        int dest = destSquare(move);
        int moveType = moveType(move);

        // 1: Revert the board
        // This section handles different move types, such as normal moves, castling, en passant, pawn promotions, etc.
        // It involves moving, un capturing, and demoting pieces on the board to their original positions.
        if (Move.isNormalPieceAndPawnOrPushTwice(moveType)) //moveType == NORMAL || moveType == NORMAL_PAWN_MOVE || moveType == PAWN_PUSH_TWICE
            movePiece(dest, start);

        else if (moveType == Move.CASTLING) {
            movePiece(castlingDestSquareKing[dest], start);
            movePiece(castlingDestSquareRook[dest], dest); // Move the rook back to its original square
        } else if (moveType == Move.PROMOTION) {
            // Demote the promoted piece back to a pawn and add the original piece back to its square
            addPiece(Piece.valueBy(Side.flipped(sideToMove), PAWN), start);
            removePiece(dest);
        } else if (moveType == Move.EN_PASSANT) {
            // In en passant moves, state.capturedPiece is encoded as Piece.NO_PIECE
            // Un capture the pawn and move it back to its original square
//            addPiece(Piece.flipped(getPiece(dest)), dest + Direction.Direction.forward(sideToMove));
            movePiece(dest, start);
            dest = dest + Direction.forward(sideToMove); // override dest to add the captured pawn on right place
        }

        // If a piece was captured during the move, add it back to its original square
        if (state.capturedPiece != NULL_PIECE)
            addPiece(state.capturedPiece, dest);

        // 2: Revert data
        // This section updates information related to the side to move, the number of moves, and the position state.
        sideToMove = Side.flipped(sideToMove);
        --numMoves;

        state = state.previous;

        // 3: Ensure the resulting position is legal
        assert positionIsLegal() : positionIsLegalOrThrow();
    }

    // =======================
    // Move validation
    // =======================

    // assume is legal move, convert engine normal (not chess960) castling move to king capture own rook represention.
    public int toMove(int sqStart, int sqDest, int promoteType) {
        if (sqStart == kingSquare() && !isChess960() && Square.distance(sqStart, sqDest) == 2) {
            return castlingMove(sideToMove, sqStart > sqDest);
        }
        int moveType = getMoveType(sqStart, sqDest);
        return Move.create(sqStart, sqDest, moveType, promoteType);
    }

    public int getMoveType(int moveStart, int moveDest) {
        assert Square.isValid(moveStart) && Square.isValid(moveDest) && getPiece(moveStart) != NULL_PIECE;
        int pieceMove = getPiece(moveStart);
        int pieceType = Piece.type(pieceMove);
        int side = Piece.side(pieceMove);


        if (pieceType == PAWN) {
            return ((squareToBB(moveDest) & (RANK_1_BB | RANK_8_BB)) != 0) ? PROMOTION
                    : Square.distance(moveStart, moveDest) == 16 ? PAWN_PUSH_TWICE
                    : moveDest == enPassant() ? EN_PASSANT
                    : NORMAL_PAWN_MOVE;
        }


        // as long castling mark as king capture his rook not need to check another conditions like the king move 2 square.
        return Piece.valueBy(side, ROOK) == getPiece(moveDest) ? CASTLING : NORMAL;
    }


    /**
     * Checks if a chess move is pseudo-legal based on the following conditions:
     * <ol>
     *   <li>Verify if the side to move corresponds to the side of the moving piece.</li>
     *   <li>Ensure that the piece can move according to the occupancy on the board.</li>
     *   <li>Verify if the move type is valid for the given piece.</li>
     * </ol>
     *
     * @param move The encoded representation of the move to be checked.
     * @return {@code true} if the move is pseudo-legal, {@code false} otherwise.
     */
    public boolean isPseudoLegalMove(int move) {
        int start = startSquare(move);
        int dest = destSquare(move);
        int piece = getPiece(start);
        int pieceSide = Piece.side(piece);
        int moveType = moveType(move);
        int pt = Piece.type(piece);
        int promotePt = promotePT(move);
        long enemyBB = occupancyBySide(Side.flipped(pieceSide));

        // simple validation that has piece on start. the correct side move and promote piece not set under not promotion move.
        if (piece == 0 || pieceSide != sideToMove || (promotePt != 0 && moveType != PROMOTION)
                || ((pt == PAWN) != Move.isPawnMoveType(moveType))) {  // validate that the piece move related to the move type
            return false;
        }

        if (moveType == NORMAL) {
            return ((attacks(pt, start, occupancy()) & (enemyBB | ~occupancy())) & squareToBB(dest)) != 0;
        }
        if (pt == PAWN) {
            int direction = Direction.forward(pieceSide);

            if (moveType == EN_PASSANT) {
                // check if you can reach dest square from start by doing en passant walk, and addition check if the destination square are empty
                return dest == state.enPassant && (pawnAttacks(sideToMove, start) & squareToBB(dest) & ~occupancyBB) != 0;
            }
            if (moveType == PAWN_PUSH_TWICE) {
                return Square.rank(start) == Rank.flippedIfBlack(pieceSide, RANK_2) &&
                        dest == start + 2 * direction &&
                        getPiece(start + direction) == NULL_PIECE &&
                        getPiece(start + 2 * direction) == NULL_PIECE;
            }
            if (moveType == PROMOTION) {
                if (!Move.isValidPromotePt(promotePt) || Square.rank(dest) != Rank.flippedIfBlack(pieceSide, RANK_8)) {
                    return false;
                }
            }
            // normal pawn move or promotion end up here
            return Square.file(start) == Square.file(dest) ? // is push move
                    dest == start + direction && getPiece(dest) == NULL_PIECE :
                    (pawnAttacks(sideToMove, start) & squareToBB(dest) & enemyBB) != 0;
        }
        // if we reach here we check all teh move type exept castling
        assert moveType == CASTLING;
        // check castling pseudo legal
        int castleRight = start < dest ? shortCastling(sideToMove) : longCastling(sideToMove);

        // This validates 1. has the right to castle. 2. that start contains king and dest contains rook.
        return ((castleRight & state.castlingRights) != 0) && castlingMoves[castleRight] == move;

    }


    /**
     * Checks if a chess move is legal, considering the move's pseudo-legality and additional conditions.
     * Assumes the move is pseudo-legal.
     * pseudo legal move consider legal if it's not leave own king under threat. (the move must cancel
     * check if any, and make sure the move itself not of new threat in case of pin piece moved.)
     *
     * @param move The encoded representation of the move.
     * @return True if the move is legal, false otherwise.
     */
    public boolean isLegalMove(int move) {
        int moveType = moveType(move);

        if (moveType == CASTLING) {
            return isLegalCastlingMove(move);
        }
        if (moveType == EN_PASSANT) {
            return isLegalEnPassantMove(move);
        }

        int start = startSquare(move);
        int dest = destSquare(move);
        int ksq = state.kingSquare;
        // In case of the king move, check if the destination square is safe.
        if (start == ksq)
            return attackersBB(Side.flipped(sideToMove), dest, occupancyBB ^ squareToBB(ksq)) == 0;
        // Make sure the move doesn't leave the king in check.
        // First, check for double check — in that case, only king moves are legal, and since we already returned if king moved, return false.
        // Otherwise, there's only one checker, so calculate the path mask and check if the destination square blocks the check.
        long checkers = state.checkers;
        if (checkers != 0 && (
                has2OrMoreBits(checkers) || (pathBetween(ksq, lsbToSquare(checkers)) & squareToBB(dest)) == 0))
            return false;

        // check if this piece is pined and in case of pined if it can move along pin line.
        return ((squareToBB(start) & state.pinMask) == 0) || onSameLine(start, dest, ksq);


    }

    /**
     * Checks if a chess move is fully legal
     *
     * @param move The encoded representation of the move.
     * @return True if the move is legal, false otherwise.
     */
    public boolean isFullyLegalMove(int move) {
        return isPseudoLegalMove(move) && isLegalMove(move);
    }


    /**
     * Checks if a castling move is legal.
     * Assumes the move is pseudo-legal.
     *
     * @param move castling move
     * @return True if the castling move is legal, false otherwise.
     */
    public boolean isLegalCastlingMove(int move) {
        int dest = destSquare(move); // dest is the square that the castling rook on it.
        // king can not be in a check and all castling path must be empty.
        if (state.checkers != 0 || (castlingPath[dest] & occupancyBB) != 0) return false;

        int enemySide = Side.flipped(sideToMove);
        long kingPath = castlingKingPath[dest];

        // loop over all the square in castling king path and check if the enemy threatening them.
        for (; kingPath != 0; kingPath = popLsb(kingPath))
            if (attackersBB(enemySide, lsbToSquare(kingPath), occupancyBB) != 0)
                return false;

        // to include the case of chess 960 that calling rook pin to the king,
        // for example enemy queen on a1 rook on b1 and king on c1
        return (squareToBB(dest) & state.pinMask) == 0;
    }


    /**
     * Checks if an en passant move is legal.
     * Assumes the move is pseudo-legal.
     *
     * @param epMove
     * @return True if the en passant move is legal, false otherwise.
     */
    public boolean isLegalEnPassantMove(int epMove) {

        int start = startSquare(epMove);
        int dest = destSquare(epMove);
        int ksq = state.kingSquare;
        int enemySide = Side.flipped(sideToMove);
        int captureSq = dest - Direction.forward(sideToMove);
        long occAfterMove = occupancyBB ^ squareToBB(start) ^ squareToBB(dest) ^ squareToBB(captureSq);


        return (rookAttacks(ksq, occAfterMove) & occupancyBySideAndType(enemySide, ROOK, QUEEN)) == 0 &&
                (bishopAttacks(ksq, occAfterMove) & occupancyBySideAndType(enemySide, BISHOP, QUEEN)) == 0;
    }

    // Note: In the initial position, make sure to call this function only after all position state initialization is complete,
    // because it relies on check detection.
    private boolean isValidEpSquare(int enSq) {

        int enemySide = Side.flipped(sideToMove);
        int pawnMovedDir = Direction.forward(enemySide);

        if (enSq == NULL_SQUARE ||
                Square.rank(enSq) != Rank.flippedIfBlack(sideToMove, RANK_6) ||
                // Check if there is an enemy pawn behind the en passant square (i.e., the pawn that just moved).
                (occupancyBySideAndType(enemySide, PAWN) & squareToBB(enSq + pawnMovedDir)) == 0 ||
                // Check if the squares the pawn would move through are empty.
                ((squareToBB(enSq - pawnMovedDir) | squareToBB(enSq)) & occupancyBB) != 0 ||
                // Check if a pawn from the side to move can capture on the en passant square.
                (occupancyBySideAndType(sideToMove, PAWN) & pawnAttacks(enemySide, enSq)) == 0)
            return false;

        // Validate en passant in the context of a checking position on initial FEN load.
        if (state.ply == 0 && checker() != 0) {

            // If double check, en passant cannot be a legal response — return false.
            if (has2OrMoreBits(checker())) {
                return false;
            }

            long checkMask = Bitboard.pathBetween(state.kingSquare, lsbToSquare(checker()));

            // Allow the en passant square only if it breaks or causes the check (direct or discovered).
            return ((squareToBB(enSq - pawnMovedDir) | squareToBB(enSq + pawnMovedDir)) & checkMask) != 0;

        /*
            We want to consider the en passant square on init only if the check comes from the en passant move—
            either a direct check by the capturing pawn or a discovered check from the starting square.

            This is to exclude cases where the user enters a FEN like:
              +---+---+---+---+---+---+---+---+
            8 | r | n | b | q | k | b |   | r |
              +---+---+---+---+---+---+---+---+
            7 | p | p |   | p | p | p | p | p |
              +---+---+---+---+---+---+---+---+
            6 |   |   |   |   |   |   |   |   |
              +---+---+---+---+---+---+---+---+
            5 |   |   | p | P |   |   |   |   |
              +---+---+---+---+---+---+---+---+
            4 |   |   |   |   |   |   |   |   |
              +---+---+---+---+---+---+---+---+
            3 |   |   |   |   |   | n |   |   |
              +---+---+---+---+---+---+---+---+
            2 | P | P | P |   | P | P | P | P |
              +---+---+---+---+---+---+---+---+
            1 | R | N | B | Q | K | B | N | R |
              +---+---+---+---+---+---+---+---+
                a   b   b   d   e   f   g   h

            FEN: rnbqkb1r/pp1ppppp/8/2pP4/8/5n2/PPP1PPPP/RNBQKBNR w KQkq c6 0 1

            This FEN wrongly declares c6 as a legal en passant target square
            even though the conditions for en passant were never met.
        */
        }

        return true;
    }


    // =======================
    // State getters
    // =======================

    public boolean inCheck() {
        return state.checkers != 0;
    }

    /**
     * Get the bitboard representing the squares pinned by the opponent's pieces.
     *
     * @return The bitboard representing pinned squares.
     */
    public long pinMask() {
        return state.pinMask;
    }

    /**
     * Get the bitboard representing the squares currently attacked by opponent pieces.
     *
     * @return The bitboard representing attacked squares.
     */
    public long checker() {
        return state.checkers;
    }

    /**
     * Get the en passant square, if any, from the current position state.
     *
     * @return The en passant square, or NULL_SQUARE if there is no en passant square.
     */
    public int enPassant() {
        return state.enPassant;
    }

    /**
     * Get the current rule 50 count from the position state.
     *
     * @return The current rule 50 count.
     */
    public int rule50() {
        return state.rule50;
    }


    /**
     * Get the last move made in the position.
     *
     * @return The last move made, encoded as an integer.
     */
    public int lastMove() {
        return state.lastMove;
    }

    // return the piece that is captured during the last move
    public int getCapturedPiece() {
        return state.capturedPiece;
    }


    public PositionState getState() {
        return state;
    }

    /**
     * Retrieves the move history  of the game.
     *
     * @return a list  of Move objects representing the move history
     */
    public List<Integer> moveHistory() {
        LinkedList<Integer> result = new LinkedList<>();
        for (PositionState st = state; st != null && st.lastMove != NULL_MOVE; st = st.previous) {
            result.addFirst(st.lastMove); // O(1) in LinkedList
        }
        return result;
    }

    public String posString() {
        return posString(true);
    }

    /**
     * Get a string representation of the current position.
     * This method generates a visual representation of the chessboard along with important position information.
     * @param printData if true printPosition the internal data like a key checkers etc
     * @return A string containing the board layout, FEN, checkers, pin mask, key, move history, and repetition count.
     */
    public String posString(boolean printData) {
        StringBuilder result = new StringBuilder();

        // Generate the chessboard layout
        for (int i = 8 - 1; i >= 0; --i) {
            result.append("  +---+---+---+---+---+---+---+---+\n");
            result.append(i + 1).append(" ");

            for (int j = 0; j < 8; ++j) {
                int piece = getPiece((i * 8) + j);
                char to_print = piece != NULL_PIECE ? Piece.getName(piece) : ' ';//todo to check
                result.append("| ").append(to_print).append(" ");
            }
            result.append("|\n");
        }
        result.append("  +---+---+---+---+---+---+---+---+\n");
        result.append("    a   b   c   d   e   f   g   h\n\n");

        // Append additional position information
        result.append("Fen:        ").append(getFen()).append("\n");
        if (printData) {
            result.append("Checker:    ").append(toBinaryString(state.checkers)).append("\n");
            result.append("Pin Mask:   ").append(toBinaryString(state.pinMask)).append("\n");
            result.append("key:        ").append(state.key).append("\n");
            result.append("repetition: ").append(state.numRepetition).append("\n");
        }

        result.append("moves play: ").append(moveHistory().stream().map(Move::getName).toList()).append("\n");
        result.append("\n");

        return result.toString();
    }

    /**
     * Generates and returns the Forsyth–Edwards Notation (FEN) string representation of the current chess position.
     * The FEN string includes information about the board, side to move, castling rights, en passant square,
     * half-move clock, and full-move number.
     *
     * @return FEN string representation of the current position
     */
    public String getFen() {
        StringBuilder boardFen = new StringBuilder(); // FEN for the board state
        StringBuilder CFen = new StringBuilder();   // FEN for castling rights
        int piece;
        int numEmptySq = 0;

        // Loop over all the ranks on the board from black side to white side
        for (int square = A1; square <= H8; ++square) {
            if ((piece = getPiece(Square.flipped(square))) != NULL_PIECE) {
                boardFen.append(numEmptySq != 0 ? numEmptySq : "").append(Piece.getName(piece));
                numEmptySq = 0;
            } else
                numEmptySq++;

            if (Square.file(Square.flipped(square)) == FILE_H) { // Check if it's the last square in a rank
                boardFen.append(numEmptySq != 0 ? numEmptySq : "").append(square == H8 ? "" : "/");
                numEmptySq = 0;
            }
        }

        // Calculate the castling FEN
        if (canCastle(WHITE_SHORT))
            CFen.append(isChess960 ? Character.toUpperCase(File.getName(castlingRookFile(WHITE_SHORT))) : "K");
        if (canCastle(WHITE_LONG))
            CFen.append(isChess960 ? Character.toUpperCase(File.getName(castlingRookFile(WHITE_LONG))) : "Q");
        if (canCastle(BLACK_SHORT))
            CFen.append(isChess960 ? File.getName(castlingRookFile(BLACK_SHORT)) : "k");
        if (canCastle(BLACK_LONG))
            CFen.append(isChess960 ? File.getName(castlingRookFile(BLACK_LONG)) : "q");
        if (state.castlingRights == 0)
            CFen.append("-");

        String enPassant = enPassant() == NULL_SQUARE ? "-" : Square.getName(enPassant());

        // Format and return the complete FEN string
        return String.format("%s %c %s %s %d %d", boardFen, Side.getName(sideToMove),
                CFen, enPassant, state.rule50, getNumMoves());
    }


    public boolean isChess960() {
        return isChess960;
    }


    public int getNumMoves() {
        return (numMoves / 2) + 1;
    }


    // =======================
    // Misc getters (non-bitboard/basic)
    // =======================

    /**
     * Gets the side to move in the current position.
     *
     * @return the side to move (WHITE or BLACK)
     */
    public int sideToMove() {
        return sideToMove;
    }


    // =======================
    // Attacker methods
    // =======================

    /**
     * Retrieves a bitboard with the pieces attacking a given square, considering the specified occupancy.
     *
     * @param square    the square to check for attackers
     * @param occupancy the bitboard representing the occupied squares
     * @return a bitboard with the pieces attacking the specified square
     */
    public long attackersBB(int square, long occupancy) {
        return (rookAttacks(square, occupancy) & occupancyByType(ROOK, QUEEN)) |
                (bishopAttacks(square, occupancy) & occupancyByType(BISHOP, QUEEN)) |
                (knightAttacks(square) & occupancyByType(KNIGHT)) |
                (kingAttacks(square) & occupancyByType(KING)) |
                (pawnAttacks(WHITE, square) & occupancyByPiece(BLACK_PAWN)) |
                (pawnAttacks(BLACK, square) & occupancyByPiece(WHITE_PAWN));
    }

    /**
     * Retrieves a bitboard with the pieces attacking a given square, considering the specified occupancy,
     * but restricted to a specific side (color).
     *
     * @param attacksSide the side (color) of the attacking pieces
     * @param square      the square to check for attackers
     * @param occupancy   the bitboard representing the occupied squares
     * @return a bitboard with the pieces of the specified side attacking the specified square
     */
    public long attackersBB(int attacksSide, int square, long occupancy) {
        return occupancyBySideBB[attacksSide] & attackersBB(square, occupancy);
    }

    public long attackersByPiece(int piece, int square) {
        return attackersBB(square, occupancyBB) & occupancyByPiece(piece);
    }

    // =======================
    // Occupancy methods (bitboards)
    // =======================

    /**
     * Get the bitboard representing the occupancy of all pieces on the board.
     *
     * @return The bitboard representing the occupancy.
     */
    public long occupancy() {
        return occupancyBB;
    }

    /**
     * Get the bitboard representing the occupancy of a specific side on the board.
     *
     * @param side The side (WHITE or BLACK) for which to get the occupancy.
     * @return The bitboard representing the occupancy of the specified side.
     */
    public long occupancyBySide(int side) {
        return occupancyBySideBB[side];
    }

    /**
     * Get the bitboard representing the occupancy of a specific piece type on the board.
     *
     * @param pieceType The piece type for which to get the occupancy.
     * @return The bitboard representing the occupancy of the specified piece type.
     */
    public long occupancyByType(int pieceType) {
        return occupancyByTypeBB[pieceType];
    }

    /**
     * Get the bitboard representing the combined occupancy of two piece types on the board.
     *
     * @param pieceType1 The first piece type.
     * @param pieceType2 The second piece type.
     * @return The combined bitboard representing the occupancy of the specified piece types.
     */
    public long occupancyByType(int pieceType1, int pieceType2) {
        return occupancyByTypeBB[pieceType1] | occupancyByTypeBB[pieceType2];
    }

    /**
     * Get the bitboard representing the combined occupancy of three-piece types on the board.
     *
     * @param pieceType1 The first piece type.
     * @param pieceType2 The second piece type.
     * @param pieceType3 The third piece type.
     * @return The combined bitboard representing the occupancy of the specified piece types.
     */
    public long occupancyByType(int pieceType1, int pieceType2, int pieceType3) {
        return occupancyByTypeBB[pieceType1] | occupancyByTypeBB[pieceType2] | occupancyByTypeBB[pieceType3];
    }

    /**
     * Get the bitboard representing the occupancy of a specific piece on the board.
     *
     * @param piece The piece for which to get the occupancy.
     * @return The bitboard representing the occupancy of the specified piece.
     */
    public long occupancyByPiece(int piece) {
        return occupancyByPieceBB[piece];
    }

    /**
     * Get the bitboard representing the combined occupancy of two specific pieces on the board.
     *
     * @param piece1 The first piece.
     * @param piece2 The second piece.
     * @return The combined bitboard representing the occupancy of the specified pieces.
     */
    public long occupancyByPiece(int piece1, int piece2) {
        return occupancyByPieceBB[piece1] | occupancyByPieceBB[piece2];
    }

    /**
     * Get the bitboard representing the combined occupancy of three specific pieces on the board.
     *
     * @param piece1 The first piece.
     * @param piece2 The second piece.
     * @param piece3 The third piece.
     * @return The combined bitboard representing the occupancy of the specified pieces.
     */
    public long occupancyByPiece(int piece1, int piece2, int piece3) {
        return occupancyByPieceBB[piece1] | occupancyByPieceBB[piece2] | occupancyByPieceBB[piece3];
    }

    /**
     * Get the bitboard representing the occupancy of a specific piece type on a specific side.
     *
     * @param side      The side (WHITE or BLACK) for which to get the occupancy.
     * @param pieceType The piece type for which to get the occupancy.
     * @return The bitboard representing the occupancy  of  the specified piece type on the specified side.
     */
    public long occupancyBySideAndType(int side, int pieceType) {
        return occupancyByTypeBB[pieceType] & occupancyBySideBB[side];
    }

    /**
     * Get the bitboard representing the combined occupancy of two piece types on a specific side.
     *
     * @param side       The side (WHITE or BLACK) for which to get the occupancy.
     * @param pieceType1 The first piece type.
     * @param pieceType2 The second piece type.
     * @return The combined bitboard representing the occupancy  of  the specified piece types on the specified side.
     */
    public long occupancyBySideAndType(int side, int pieceType1, int pieceType2) {
        return occupancyBySideBB[side] & (occupancyByTypeBB[pieceType1] | occupancyByTypeBB[pieceType2]);
    }

    // =======================
    // Board state: pieces and squares
    // =======================

    /**
     * Retrieves the piece at the specified square.
     *
     * @param square the square to query
     * @return the piece at the specified square
     */
    public int getPiece(int square) {
        return board[square];
    }

    /**
     * Retrieves the square of the specified piece type belonging to the given side.
     * return the first square from begining that found piece on it
     *
     * @param side      the side (color) of the piece
     * @param pieceType the type of the piece (e.g., KING, QUEEN)
     * @return the square of the specified piece type for the given side
     */
    public int squareOf(int side, int pieceType) {
        return lsbToSquare(occupancyByPieceBB[Piece.valueBy(side, pieceType)]);
    }

    /**
     * Retrieves the square of the specified piece.
     * return the first square from begining that found piece on it
     *
     * @param piece the piece to query
     * @return the square of the specified piece
     */
    public int squareOf(int piece) {
        return lsbToSquare(occupancyByPieceBB[piece]);
    }

    /**
     * Retrieves the square of the king for side to move.
     *
     * @return the square of the king
     */
    public int kingSquare() {
        return state.kingSquare;
    }


    // =======================
    // Draw detection
    // =======================

    /**
     * FIDE dead-position check (no helpmates possible):
     * not incluse ded positon with pawns blocked and every king stack behind his pawns
     * like: 8/6k1/8/1p1p1p1p/pPpPpPpP/P1P1P1P1/5B2/3K4 w - - 0 1
     * - If more than 4 pieces remain or any Q/R/P remain → not dead.
     * - With exactly 4 pieces: the only dead case is "two bishops on the same color"
     * (covers B vs B same-color and K vs K+BB same-color).
     * - With ≤3 pieces: all remaining cases are dead (K vs K, K+B vs K, K+N vs K).
     */
    public boolean inInsufficientMaterial() {
        int countPiece = Bitboard.bitCount(occupancyBB);

        // Early exit: anything with > 4 pieces or with a queen/rook/pawn cannot be a dead position.
        if (countPiece > 4 || occupancyByType(QUEEN, ROOK, PAWN) != 0) {
            return false;
        }

        if (countPiece == 4) {
            // The only 4-piece dead scenario: exactly two bishops left, and they are on the same color complex.
            // This covers both K+B vs K+B (same-color bishops) and K vs K+BB (both bishops same color).
            return pieceTypeCount(BISHOP) == 2 && Bitboard.isOnSameColor(occupancyByType(BISHOP));
        }

        // countPiece <= 3 → all such scenarios are dead (K vs K, K+B vs K, K+N vs K).
        return true;
    }


    /**
     * Checks whether the position has occurred three times by verifying the actual
     * positions in the game history (repetition by FEN logic).
     * <p>
     * This method is slower but guaranteed correct. It replays the move history
     * backwards (up to 50 moves) and compares the repetition-relevant fields  of the FEN:
     * - piece placement
     * - side to move
     * - castling rights
     * - en passant square
     * <p>
     * Use this method if you need a reliable draw detection, e.g. for PGN validation,
     * engine draw claims, or UI indication.
     *
     * @return true if the exact same position has occurred three times, false otherwise.
     */
    public boolean inVerifiedThreeFoldRepetition() { // todo to test it
        return inThreeFoldRepetition() && inVerifyRepetition();
    }

    /**
     * Quickly checks whether the current position is flagged internally as being in a state
     * of threefold repetition, based on Zobrist hash tracking.
     * <p>
     * Note:
     * - This check is fast and efficient, using hash-based counting.
     * - However, due to the nature of Zobrist hashing (64-bit), false positives are
     * theoretically possible due to collisions.
     * - Use {@link #inVerifiedThreeFoldRepetition()} for guaranteed correctness.
     *
     * @return true if the position has potentially occurred three times, false otherwise.
     */
    public boolean inThreeFoldRepetition() {
        return state.numRepetition >= NUM_REPETITION_TO_DRAW;
    }

    /**
     * Check if the position is in a state of reaching the 50-move rule for a draw.
     *
     * @return True if the position is in the 50-move rule condition; otherwise, false.
     */
    public boolean inRule50() {
        return state.rule50 >= RULE_50_COUNT_TO_DRAW;
    }

    private boolean inVerifyRepetition() {
        Map<String, Integer> positionCount = new HashMap<>();
        positionCount.put(generateRepetitionByFenKey(getFen()), 1);

        boolean isVerified = false;
        int end = Math.min(state.rule50, state.ply);
        // collect undone moves so we can restore the exact original state without setFen
        List<Integer> undoneMoves = new ArrayList<>(end);

        for (int i = 0; i < end && !isVerified; i++) {
            undoneMoves.add(state.lastMove);

            undoMove();
            String key = generateRepetitionByFenKey(getFen());
            int count = positionCount.merge(key, 1, Integer::sum);
            isVerified = count >= 3;
        }

        // Restore full state by replaying the undone moves (in reverse order)
        // make all the moves that were made back.
        for (int i = undoneMoves.size() - 1; i >= 0; --i) {
            makeMove(undoneMoves.get(i)); // uses your internal fast repetition tracking
        }

        return isVerified;

    }


    // =======================
    // Board piece helpers
    // =======================

    /**
     * Adds a piece to the chessboard at the specified square.
     * Assumes the square is empty.
     *
     * @param piece  The piece to be added.
     * @param square The square where the piece is added.
     */
    void addPiece(int piece, int square) {
        assert Piece.isValid(piece) && getPiece(square) == NULL_PIECE;
        long sqBB = Bitboard.squareToBB(square);
        occupancyBB |= sqBB;
        occupancyBySideBB[Piece.side(piece)] |= sqBB;
        occupancyByTypeBB[Piece.type(piece)] |= sqBB;
        occupancyByPieceBB[piece] |= sqBB;
        board[square] = piece;
//        numPieces++;
        ++piecesCount[piece];
        state.key ^= pieceSquareKeys[piece][square];

    }

    /**
     * Removes a piece from the chessboard at the specified square.
     * Assumes the square is not empty.
     *
     * @param square The square from which the piece is removed.
     */
    void removePiece(int square) {
        assert square != NULL_SQUARE;
        long sqBB = Bitboard.squareToBB(square);
        int piece = board[square];
        occupancyBB ^= sqBB;
        occupancyBySideBB[Piece.side(piece)] ^= sqBB;
        occupancyByTypeBB[Piece.type(piece)] ^= sqBB;
        occupancyByPieceBB[piece] ^= sqBB;
        board[square] = NULL_PIECE;
//        numPieces--;
        --piecesCount[piece];
        state.key ^= pieceSquareKeys[piece][square];
    }

    /**
     * Moves a piece on the chessboard from the start square to the destination square.
     * Assumes there is a piece on the start square and the destination square is empty.
     * If start == dest, it has no effect.
     *
     * @param start The starting square of the piece.
     * @param dest  The destination square for the piece.
     */
    private void movePiece(int start, int dest) {
        assert Square.isValid(start) && Square.isValid(dest) && getPiece(start) != NULL_PIECE;
        long startOrDestBB = Bitboard.squareToBB(start) ^ Bitboard.squareToBB(dest); // need xor here to
        // set the startOrDestBB to 0 where start = dest
        int piece = getPiece(start);
        occupancyBB ^= startOrDestBB;
        occupancyBySideBB[Piece.side(piece)] ^= startOrDestBB;
        occupancyByTypeBB[Piece.type(piece)] ^= startOrDestBB;
        occupancyByPieceBB[piece] ^= startOrDestBB;
        board[start] = NULL_PIECE;
        board[dest] = piece;
        state.key ^= (pieceSquareKeys[piece][start] ^ pieceSquareKeys[piece][dest]);
    }

    // =======================
    // State-update helper methods
    // =======================

    /**
     * Update the repetition count in the current position state.
     * Checks for repetitions by comparing the position key.
     * If a repetition is found, increments the repetition count.
     */
    private void updateRepetition() {
        state.numRepetition = 0;
        // fast lookup on the table if is 0, the position not accuse before.
        int value = (repetitionList[(int) (state.key & REPETITION_MASK)]++);

        if (value <= 0) {
            return;
        }

        int end = Math.min(state.rule50, state.ply);
        if (end >= 4) {
            PositionState step = state.previous.previous;
            for (int i = 4; i <= end; i += 2) {
                // repetition only can accuse after the third ply and not need to check if black position equal to white position
                step = step.previous.previous;
                if (step.key == state.key) {
                    state.numRepetition = step.numRepetition + 1;
//                    System.out.println("true call");
                    return;
                }
            }
        }
    }

    private long pinMask(int attackSide, int targetSquare) {
        // all attackers around (on file, rank and tow diagonal of targetSquare square) the square targetSquare
        long attackers =
                (occupancyBySideAndType(attackSide, BISHOP, QUEEN) & bishopAttacks(targetSquare)) |
                        (occupancyBySideAndType(attackSide, ROOK, QUEEN) & rookAttacks(targetSquare));
        long pinMask = 0, pathToS, occupancy = occupancyBB ^ attackers;

        for (; attackers != 0; attackers = Bitboard.popLsb(attackers)) {
            int square = lsbToSquare(attackers);
            pathToS = Bitboard.pathBetween(targetSquare, square);
            if (Bitboard.hasOneBit(pathToS & occupancy))
                pinMask |= pathToS;
        }
        return pinMask;
    }

    // =======================
    // Position validation
    // =======================
    public boolean positionIsLegal() {
        try {
            return positionIsLegalOrThrow();
        } catch (IllegalPositionException e) {
            return false;
        }
    }


    /**
     * Checks if the current chess position is legal based on various criteria.
     * <p>
     * Criteria Checked:
     * 1. Each side must have only one king on the board.
     * 2. The opponent side to move must not be in check.
     * 3. Pawns cannot be on the first or last rank.
     * 4. Board representation legality:
     * - Pieces' bits must align with occupancy bitboards.
     * - All bits corresponding to the piece must be set correctly.
     * - Bits corresponding to other pieces and types must be empty.
     * 5. Castling validation:
     * - Validates the legality of castling moves given the castling rights.
     * <p>
     * Note: Assumes en passant validation was done during the setFen method.
     *
     * @return True if the position is legal, otherwise throw IllegalPositionException.
     */
    public boolean positionIsLegalOrThrow() throws IllegalPositionException {
        // 1. Each side must have only one king on the board.
        if (!(pieceCount(WHITE_KING) == 1) || !(pieceCount(BLACK_KING) == 1))
            throw new IllegalPositionException("Invalid number of kings");

        // 2. The opponent side to move must not be in check.
        if (attackersBB(sideToMove, squareOf(Side.flipped(sideToMove), KING), occupancy()) != 0)
            throw new IllegalPositionException("King not in the side to move is under attack\n" + posString());

        // 3. Pawns cannot be on the first or last rank.
        if ((occupancyByType(PAWN) & (RANK_1_BB | RANK_8_BB)) != 0)
            throw new IllegalPositionException("Pawn on first or last rank");

        // 4. Board representation legality:
        //    - Pieces' bits must align with occupancy bitboards.
        //    - All bits corresponding to the piece must be set correctly.
        //    - Bits corresponding to other pieces and types must be empty.
        for (int square = A1; square <= H8; ++square) {
            int piece = getPiece(square);
            if (piece == NULL_PIECE) {
                if ((squareToBB(square) & (occupancy() | occupancyBySide(WHITE) |
                        occupancyBySide(BLACK))) != 0)
                    throw new IllegalPositionException(String.format("Piece on square %s, piece is %c - bitboards not synchronized.\n" + posString(),
                            Square.getName(square), Piece.getName(piece)));
            }
            for (int pieceType : PieceType.intValues()) {
                if (pieceType == Piece.type(piece)) { // all bits need to be set
                    if ((squareToBB(square) &
                            occupancy() &
                            occupancyBySide(Piece.side(piece)) &
                            occupancyByType(pieceType) &
                            ~occupancyByPiece(Piece.flipped(piece)) &
                            ~occupancyBySide(Side.flipped(Piece.side(piece))) &
                            occupancyByPiece(piece)) == 0)
                        throw new IllegalPositionException(String.format("Piece on square %s, piece is %c - bitboards not synchronized.\n" + posString(),
                                Square.getName(square), Piece.getName(piece)));
                } else { // all bits need to be empty
                    if ((squareToBB(square) & (
                            occupancyByPiece(Piece.valueBy(WHITE, pieceType)) |
                                    occupancyByPiece(Piece.valueBy(BLACK, pieceType)) |
                                    occupancyByType(pieceType))) != 0)
                        throw new IllegalPositionException(String.format("Piece on square %s, piece is %c - bitboards not synchronized.\n" + posString(),
                                Square.getName(square), Piece.getName(piece)));
                }
            }
        }

        // 5. Castling validation:
        //    - Validates the legality of castling moves given the castling rights.
        //    - in case of normal chess make sure that the dest king and rook in the correct place.
        for (int castleRight : new int[]{WHITE_SHORT, WHITE_LONG, BLACK_SHORT, BLACK_LONG}) {
            if ((state.castlingRights & castleRight) != 0) {
//                int side = castlingSide(castleRight);
//                boolean isShort = isShortCastle(castleRight);
                int kSq = startSquare(castlingMoves[castleRight]);
                int rSq = destSquare(castlingMoves[castleRight]);

                if (!isCastlingAvailableOnPosition(castleRight, kSq, rSq)) {
                    throw new IllegalPositionException("Castling rights invalid"); // todo give more context
                }

//                if (getPiece(kSq) != Piece.valueBy(side, KING) ||
//                        getPiece(rSq) != Piece.valueBy(side, ROOK) ||
//                        Rank.flippedIfBlack(side, RANK_1) != Square.rank(kSq) || //king in the correct rank
//                        (isShort && kSq >= rSq || !isShort && kSq <= rSq) || /*i think it >=  */
//                        (!isChess960 && (rSq != Square.flippedIfBlack(side, isShort ? H1 : A1) || kSq != Square.flippedIfBlack(side, E1))))
//                    throw new IllegalPositionException("Castling rights invalid");
//            }
            }
        }


        // All criteria passed; the position is legal.
        return true;
    }


    // =======================
    // Utilities
    // =======================

    /**
     * Split a FEN string into its individual components.
     *
     * @param fen The FEN string to be split.
     * @return An array containing the individual components of the FEN string.
     */
    private static String[] splitFen(String fen) {
        return fen.trim().replaceAll("\\s+", " ").split(" ");
    }

    private static String generateRepetitionByFenKey(String fen) {
        String[] parts = fen.split(" ");
        return parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3]; // pieces, side, castling, ep
    }
}
