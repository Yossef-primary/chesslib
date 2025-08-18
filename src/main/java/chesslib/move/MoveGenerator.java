package chesslib.move;

import chesslib.Bitboard;
import chesslib.Position;
import chesslib.PositionState;
import chesslib.types.*;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Function;
import static chesslib.Bitboard.*;
import static chesslib.move.Move.*;
import static chesslib.types.Castling.*;
import static chesslib.types.Direction.*;
//import static chesslib.types.PieceType.*;
//import static chesslib.types.Side.*;
import static chesslib.types.Side.Value.*;
import static chesslib.types.PieceType.Value.*;
import static chesslib.types.Square.Value.NULL_SQUARE;

/**
 * The MoveGenerator class is responsible for generating legal chess moves for a given position.
 * It employs various techniques and algorithms to efficiently compute moves, including bitboard representations
 * for board states, move generation for different piece types, and special considerations for castling, pawn promotions,
 * and en passant captures. The class also contains a performance evaluation tool (perft) for debugging and testing
 * purposes, which calculates the number valueOf possible positions at a specified depth.
 * <p>
 * This class follows a modular approach, encapsulating related functionalities within separate methods for clarity
 * and maintainability. It leverages efficient bit manipulation operations and precomputed attack patterns to optimize
 * move generation. The code is designed to be extensible and adaptable for future improvements or additions to the
 * chess engine.
 * <p>
 * Note: The class assumes the usage valueOf a Position class to represent the current state valueOf the chessboard.
 * It relies on the Bitboard, Move, and other types from the chesslib.types package.

 */
public class MoveGenerator {
    // short-circuiting move list
    private static final MoveListInterface SHORT_CIRCUIT_LIST = new ShortCircuitList();
    private static final EarlyExit EARLY_EXIT = new EarlyExit();


    /**
     * Exception used to short-circuit move generation when a legal move is found.
     * Skips expensive stack trace generation for performance.
     */
    private static class EarlyExit extends RuntimeException {
        private EarlyExit() {
            super(null, null, false, false); // disables suppression and stack trace
        }
    }

    /**
     * Special MoveListInterface that throws on the first added move.
     * Used to detect existence getBy at least one legal move.
     */
    private static class ShortCircuitList implements MoveListInterface {
        @Override
        public void add(int move) {
            throw EARLY_EXIT;
        }

        @Override
        public @NotNull Iterator<Integer> iterator() {
            throw new UnsupportedOperationException("Iteration not supported");
        }

        @Override
        public int size() {
            return 0;
        }
    }

    /**
     * Efficiently checks whether the given position has any legal move.
     * Uses early-exit logic by attempting to generate moves and stopping at the first valid one.
     *
     * @param pos the position to check
     * @return true if any legal move exists, false if none (checkmate or stalemate)
     */
    public static boolean hasAnyLegalMove(@NotNull Position pos) {
        try {
            createAll(pos, SHORT_CIRCUIT_LIST);
            return false; // no legal move was added
        } catch (EarlyExit e) {
            return true; // early exit triggered — legal move exists
        }
    }

    /**
     * Generates all legal moves for the current position and adds them to the given move list.
     *
     * @param pos      The current chess position.
     * @param moveList The list to store the generated moves.
     * @return The move list containing all legal moves.
     */
    public static void createAll(@NotNull Position pos, @NotNull MoveListInterface moveList) {
        int side = pos.sideToMove();
        int enemySide = Side.flipped(side);
        int kSq = pos.getState().kingSquare;
        long occupancy = pos.occupancy();
        long enemy = pos.occupancyBySide(enemySide);
        long empty = ~occupancy;
        long enemyOrEmpty = enemy | empty;
        PositionState state = pos.getState();

        // King moves
        long attacksKing = attacks(KING, state.kingSquare) & enemyOrEmpty;
        for (; attacksKing != 0; attacksKing &= (attacksKing - 1)) {
            int dest = lsbToSquare(attacksKing);
            if (pos.attackersBB(enemySide, dest, occupancy ^ squareToBB(kSq)) == 0)
                moveList.add(Move.create(kSq, dest));
        }

        // In double check, only king can move
        if (has2OrMoreBits(pos.checker()))
            return;
//            return moveList;

        // Castling
        int allCastling = allCastling(side) & state.castlingRights;
        if (allCastling != 0) {
            if ((allCastling & Castling.ALL_SHORT) != 0) {
                int move = pos.castlingMove(allCastling & Castling.ALL_SHORT);
                if (pos.isLegalCastlingMove(move))
                    moveList.add(move);
            }
            if ((allCastling & Castling.ALL_LONG) != 0) {
                int move = pos.castlingMove(allCastling & Castling.ALL_LONG);
                if (pos.isLegalCastlingMove(move))
                    moveList.add(move);
            }
        }

        long checker = state.checkers;
        long pinMask = state.pinMask;
        long pinMaskDiagonals = pinMask & bishopAttacks(kSq);
        long pinMaskRankFile = pinMask & rookAttacks(kSq);

        // Determine check mask
        long checkMask = checker == 0 ? FULL_BB : pathBetween(kSq, lsbToSquare(checker));

        // Pawn moves
        createPawnsMove(pos, moveList, side, enemySide, kSq, enemy, empty, pinMaskDiagonals, pinMaskRankFile, checkMask);

        // Rest getBy the pieces
        checkMask &= enemyOrEmpty;

        // Knights
        createSliderMoves(moveList, Bitboard::knightAttacks, checkMask,
                pos.occupancyBySideAndType(side, KNIGHT) & ~pinMask);

        // Rooks and queen
        long rookQueen = pos.occupancyBySideAndType(side, ROOK, QUEEN);

        // Rooks pin
        createSliderMoves(moveList, square -> rookAttacks(square, occupancy),
                checkMask & pinMaskRankFile, rookQueen & pinMaskRankFile);

        // Rooks not pin
        createSliderMoves(moveList, square -> rookAttacks(square, occupancy), checkMask, rookQueen & ~pinMask);

        // Bishop and queen
        long bishopsQueen = pos.occupancyBySideAndType(side, BISHOP, QUEEN);

        // Bishop pin
        createSliderMoves(moveList, square -> bishopAttacks(square, occupancy),
                checkMask & pinMaskDiagonals, bishopsQueen & pinMaskDiagonals);

        // Bishops not pin
        createSliderMoves(moveList, square -> bishopAttacks(square, occupancy),
                checkMask, bishopsQueen & ~pinMask);

    }


    /**
     * Generates pawn moves for a given side in the position, considering special cases like promotions and en passant.
     *
     * @param pos            The current chess position.
     * @param moveList       The list to store the generated moves.
     * @param side           The side for which pawn moves are generated.
     * @param enemySide      The opponent side.
     * @param kSq            The square valueOf the king.
     * @param enemy          Bitboard representing positions occupied by the opponent's pieces.
     * @param empty          Bitboard representing empty squares on the board.
     * @param pinMaskDiagonals Bitboard representing squares where pinned pieces can move diagonally.
     * @param pinMaskRankFile Bitboard representing squares where pinned pieces can move along ranks and files.
     * @param checkMask      Bitboard representing squares under attack.
     */
    private static void createPawnsMove(@NotNull Position pos, @NotNull MoveListInterface moveList, int side, int enemySide,
                                        int kSq, long enemy, long empty, long pinMaskDiagonals, long pinMaskRankFile,
                                        long checkMask) {
        long pawns = pos.occupancyBySideAndType(side, PAWN);
        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq);

        long lrPawns = pawns & ~pinMaskRankFile;
        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));

        // Variables to store different types valueOf pawn moves
        long leftPawns, rightPawns, pushP, push2P, rankToPromote;
        int up, upRight, upLeft; // side - relative  directions.

        // Set variables based on the side's perspective
        if (side == WHITE) { // direction from white perspective
            rankToPromote = RANK_7_BB;
            up = UP;
            upRight = UP_RIGHT;
            upLeft = UP_LEFT;

            leftPawns = lrPawns & ~pinOnRightD & shiftDownRight(checkMask & enemy);
            rightPawns = lrPawns & ~pinOnLeftD & shiftDownLeft(checkMask & enemy);
            pushP = pPawns & shiftDown(empty);
            push2P = pushP & RANK_2_BB & Bitboard.shiftDownTwice(empty & checkMask);
            pushP &= shiftDown(checkMask);
        } else { // direction from black perspective
            rankToPromote = RANK_2_BB;
            up = DOWN;
            upRight = DOWN_LEFT;
            upLeft = DOWN_RIGHT;

            leftPawns = lrPawns & ~pinOnRightD & shiftUpLeft(checkMask & enemy);
            rightPawns = lrPawns & ~pinOnLeftD & shiftUpRight(checkMask & enemy);

            pushP = pPawns & shiftUp(empty);
            push2P = pushP & RANK_7_BB & Bitboard.shiftUpTwice(empty & checkMask);
            pushP &= shiftUp(checkMask);
        }

        int move, start;

        // Handle en passant
        int enPassantSq = pos.enPassant();
        if (enPassantSq != NULL_SQUARE) {
            long epPawns = lrPawns & pawnAttacks(enemySide, enPassantSq);
            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
                if (pos.isLegalEnPassantMove(move = Move.create(lsbToSquare(epPawns), enPassantSq, EN_PASSANT)))
                    moveList.add(move);
            }
        }

        // Handle pawn promotions
        if (((leftPawns | rightPawns | pushP) & rankToPromote) != 0) {
            long lPromotePawns = leftPawns & rankToPromote;
            long rPromotePawns = rightPawns & rankToPromote;
            long pPromotePawns = pushP & rankToPromote;

            leftPawns ^= lPromotePawns;
            rightPawns ^= rPromotePawns;
            pushP ^= pPromotePawns;

            createPromoteMoves(moveList, upLeft, lPromotePawns);
            createPromoteMoves(moveList, upRight, rPromotePawns);
            createPromoteMoves(moveList, up, pPromotePawns);
        }

        // Generate normal pawn moves
        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
            start = lsbToSquare(leftPawns);
            moveList.add(Move.create(start, start + upLeft, NORMAL_PAWN_MOVE));
        }
        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
            start = lsbToSquare(rightPawns);
            moveList.add(Move.create(start, start + upRight, NORMAL_PAWN_MOVE));
        }
        for (; pushP != 0; pushP &= (pushP - 1)) {
            start = lsbToSquare(pushP);
            moveList.add(Move.create(start, start + up, NORMAL_PAWN_MOVE));
        }

        // Generate pawn moves with double push
        for (; push2P != 0; push2P &= (push2P - 1)) {
            start = lsbToSquare(push2P);
            moveList.add(Move.create(start, start + 2 * up, PAWN_PUSH_TWICE));
        }
    }


    /**
     * Creates slider moves for a given piece type, targeting specific squares and considering occupied squares.
     *
     * @param moveList    The list to store the generated moves.
     * @param attacksFunc The function providing the attack bitboard for a given square.
     * @param target      The target squares for the moves.
     * @param pieces      The bitboard representing the positions valueOf the pieces.
     */
    private static void createSliderMoves(@NotNull MoveListInterface moveList, Function<Integer,
            Long> attacksFunc, long target, long pieces) {
        int start, dest;

        // Iterate through all pieces
        for (; pieces != 0; pieces &= (pieces - 1)) {
            start = lsbToSquare(pieces);

            // Calculate attacks for the current piece
            long attacks = attacksFunc.apply(start) & target;

            // Iterate through all valid attack squares
            for (; attacks != 0; attacks &= (attacks - 1)) {
                dest = lsbToSquare(attacks);

                // Add the move to the move list
                moveList.add(Move.create(start, dest));
            }
        }
    }

    /**
     * Creates promotion moves for pawn promotion.
     *
     * @param moveList     The list to store the generated moves.
     * @param direction    The direction valueOf promotion.
     * @param promotePawns The bitboard representing the positions valueOf pawns eligible for promotion.
     */
    private static void createPromoteMoves(@NotNull MoveListInterface moveList, int direction, long promotePawns) {
        int start, dest;

        // Iterate through all pawns eligible for promotion
        for (; promotePawns != 0; promotePawns &= (promotePawns - 1)) {
            start = lsbToSquare(promotePawns);

            // Calculate the destination square after promotion
            dest = start + direction;

            // Add promotion moves for knight, bishop, rook, and queen to the move list
            moveList.add(Move.create(start, dest, PROMOTION, KNIGHT));
            moveList.add(Move.create(start, dest, PROMOTION, BISHOP));
            moveList.add(Move.create(start, dest, PROMOTION, ROOK));
            moveList.add(Move.create(start, dest, PROMOTION, QUEEN));
        }
    }



    /**
     * Generates and prints the perft (performance test) results for the given position and depth.
     *
     * @param pos   The current chess position.
     * @param depth The depth valueOf the perft search.
     */
    public static void perft(Position pos, int depth) {
        // Record the start time to measure the execution time
        long startTime = System.nanoTime();

        // Base case: If the depth is less than or equal to 0, return without further computation
        if (depth <= 0) return;


        // Create a MoveList containing all legal moves for the current position
        MoveList moveList = new MoveList(pos);

        // Initialize counters for nodes and the count valueOf moves at each depth
        long nodes = 0, count;
        int step = 0;
        // Iterate through all moves in the MoveList
        for (int move : moveList) {
//            if (step++ > 0){
//                break;
//            }
            // If the depth is greater than 1, recursively explore the moves at the next depth
            if (depth > 1) {
                pos.makeMove(move);    // Make the move

                count = numMoves(pos, depth - 1);  // Recursive call for the next depth
                pos.undoMove();           // Undo the move to explore other moves
            } else {
                // If depth is 1, simply count the number valueOf legal moves at this depth
                count = 1;
            }

            // Accumulate the count valueOf moves
            nodes += count;

            // Print the move and the count valueOf moves at the current depth
            System.out.println(Move.getName(move) + ": " + count);
        }

        // Record the end time to calculate the elapsed time
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        double elapsedSeconds = (double) elapsedTime / 1_000_000_000.0; // Convert to seconds

        // Print the total nodes and the elapsed time
        System.out.println("Nodes count: " + nodes);
        System.out.println("Time: " + elapsedSeconds + "\n");
    }

    /**
     * Helper method for counting the number valueOf moves at a given depth.
     *
     * @param pos   The current chess position.
     * @param depth The depth valueOf the search.
     * @return The number valueOf moves at the given depth.
     */
    public static long numMoves2(Position pos, int depth) {
        if (depth <= 0) return 0;

        // Initialize a new PositionState to keep track valueOf the position state during the search
        PositionState state = new PositionState();

        // Initialize counters for the total number valueOf moves and the count at each depth
        long nodes = 0;

        // Create a MoveList containing all legal moves for the current position
        MoveList moveList = new MoveList(pos);

        // Base case: If depth is 1, return the total number valueOf legal moves at this depth
        if (depth == 1) {
            return moveList.size();
        }
        int step = 0;

        // Iterate through all moves in the MoveList
        for (int move : moveList) {
//            if (step++ > 1){
//                return nodes;
//            }
            pos.makeMove(move);    // Make the move

//            if (pos.inCheck() && new MoveList(pos).size() == 0){
//                // normal way Time: 82.326456583   Time: 42.531077125
////                pos.printBoard();
//                System.out.println("positon with chackmate  found");
//            }

            nodes += numMoves2(pos, depth - 1);  // Recursive call for the next depth
//            nodes += cont;               // Accumulate the count valueOf moves
            pos.undoMove();           // Undo the move to explore other moves
        }

        return nodes;
    }

    public static long numMoves(Position pos, int depth) {
        // Base: perft convention is depth==0 -> 1 node (the current position)
        if (depth == 0) return 1;

        long nodes = 0;
        MoveList moveList = new MoveList(pos);

        if (depth == 1) {
            return moveList.size();
        }

        for (int move : moveList) {
            // Create or reuse a state snapshot for THIS ply
            PositionState st = new PositionState(); // or from a preallocated pool/stack

            pos.makeMove(move, st);   // <-- pass state
            nodes += numMoves(pos, depth - 1);
            pos.undoMove();   // <-- use the same state
        }

        return nodes;
    }

    public static void main(String[] args) {
        // simple time test
        String fenWithALotOfEp  = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1";
        String fenWithALotOfEddp  = "rnbqkb1r/pp1ppppp/8/2pP4/8/5n2/PPP1PPPP/RNBQKBNR w KQkq c6 0 1";
        String fen = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        Position position = new Position(fenWithALotOfEp);
        position.printBoard();
        perft(position, 8);
    }

}

/*
on s
a2a3: 106743106
b2b3: 133233975
c2c3: 144074944
d2d3: 227598692
e2e3: 306138410
f2f3: 102021008
g2g3: 135987651
h2h3: 106678423
a2a4: 137077337
b2b4: 134087476
c2c4: 157756443
d2d4: 269605599
e2e4: 309478263
f2f4: 119614841
g2g4: 130293018
h2h4: 138495290
b1a3: 120142144
b1c3: 148527161
g1f3: 147678554
g1h3: 120669525
Nodes count: 3195901860
Time: 145.689124875
 */

