package chesslib;

import chesslib.types.*;

import java.util.function.Consumer;

import static chesslib.SharedRandom.RANDOM;
import static chesslib.types.Direction.*;
import static chesslib.types.PieceType.Value.*;
import static chesslib.types.Rank.*;
//import static chesslib.types.Side.*;
//import static chesslib.types.Square.*;
import static chesslib.types.Square.Value.*;
import static chesslib.types.Side.Value.*;

public class Bitboard {

    // ================================
    // Constants & basic masks
    // ================================

    public static final long DARK_SQUARES_BB = 0xAA55AA55AA55AA55L;
    public static final long LIGHT_SQUARES_BB = ~DARK_SQUARES_BB;
    public static final long CORNERS_BB = (1L << A1) | (1L << H1) | (1L << A8) | (1L << H8);
    public static final long EMPTY_BB = 0;
    public static final long FULL_BB = ~EMPTY_BB;

    public static final long RANK_1_BB = 0b11111111;
    public static final long RANK_2_BB = RANK_1_BB << 8;
    public static final long RANK_3_BB = RANK_2_BB << 8;
    public static final long RANK_4_BB = RANK_3_BB << 8;
    public static final long RANK_5_BB = RANK_4_BB << 8;
    public static final long RANK_6_BB = RANK_5_BB << 8;
    public static final long RANK_7_BB = RANK_6_BB << 8;
    public static final long RANK_8_BB = RANK_7_BB << 8;

    public static final long FILE_A_BB = 0x101010101010101L;
    public static final long FILE_B_BB = FILE_A_BB << 1;
    public static final long FILE_C_BB = FILE_A_BB << 2;
    public static final long FILE_D_BB = FILE_A_BB << 3;
    public static final long FILE_E_BB = FILE_A_BB << 4;
    public static final long FILE_F_BB = FILE_A_BB << 5;
    public static final long FILE_G_BB = FILE_A_BB << 6;
    public static final long FILE_H_BB = FILE_A_BB << 7;

    private static final long[] FILES_BB = {
            FILE_A_BB, FILE_B_BB, FILE_C_BB, FILE_D_BB, FILE_E_BB, FILE_F_BB, FILE_G_BB, FILE_H_BB
    };
    private static final long[] RANKS_BB = {
            RANK_1_BB, RANK_2_BB, RANK_3_BB, RANK_4_BB, RANK_5_BB, RANK_6_BB, RANK_7_BB, RANK_8_BB
    };

    /**
     * 2D array to store line masks between squares on the chessboard.
     * The valueBy at LINE_THROUGH[sq1][sq2] represents the line mask
     * between square sq1 and square sq2. If sq1 and sq2 are on
     * the same file, rank, or diagonal, it returns the bit mask
     * Of the full line; otherwise, the return value is 0.
     */
    private static final long[][] LINE_THROUGH = new long[Square.VALUES_COUNT][Square.VALUES_COUNT];

    /**
     * 2D array to store path masks between squares on the chessboard.
     * The valueBy at PATH_BETWEEN[sq1][sq2] represents the path mask
     * between square sq1 and square sq2.
     */
    private static final long[][] PATH_BETWEEN = new long[Square.VALUES_COUNT][Square.VALUES_COUNT];

    private static final long[] RIGHT_DIAGONAL = new long[Square.VALUES_COUNT];
    private static final long[] LEFT_DIAGONAL = new long[Square.VALUES_COUNT];

    // ================================
    // Attack tables / magic
    // ================================

    // attacks info
    private static final long[][] PIECES_PSEUDO_ATTACKS = new long[PieceType.VALUES_COUNT][Square.VALUES_COUNT];
    private static final long[][] PAWN_ATTACKS = new long[Side.VALUES_COUNT][Square.VALUES_COUNT];

    // magic data
    private static final int MAX_OCCUPANCY_CONFIGURATION = 1 << 12;
    private static final long[] ATTACKS_TABLE = new long[102400 * 2];
    private static final Magic[] ROOK_MAGIC = new Magic[Square.VALUES_COUNT];
    private static final Magic[] BISHOP_MAGIC = new Magic[Square.VALUES_COUNT];

    //  ================        init
    static {
        // Initializes magic bitboards (sliders) first, then non-occupancy attacks and geometry.
        initMagicBitboards();
        initAttackInfo();
    }

    private static class Magic {// todo explaination more
        long magicNum;
        long mask;
        int index;
        int shift;

        long attacks(long occupancy) {// todo explaination more
            return ATTACKS_TABLE[(int) (index + (((occupancy & mask) * magicNum) >>> shift))];
        }

        int occupancyIndex(long occupancy) {// todo explaination more also about  >>>
            return (int) (((occupancy & mask) * magicNum) >>> shift);
        }
    }

    // ================================
    // Initialization routines
    // ================================

    /**
     * Initializes magic bitboards for rook and bishop attacks.
     * This method computes magic numbers and attack tables for each square and piece type.
     * Magic bitboards allow fast calculation getBy sliding piece attacks (rook, bishop)
     * by using bitwise masking and indexing into precomputed tables.
     *
     * @return the total number getBy failed attempts to find a suitable magic number (for debugging).
     */
    public static long initMagicBitboards() {
        long failsFoundMagicCount = 0; // Total number getBy attempts across all squares
        int attackTableIndex = 0; // Global offset into the shared ATTACKS_TABLE array

        // Iterate over all 64 squares
        for (int sq = A1; sq <= H8; ++sq) {
            // Create magic container for rook and bishop for this square
            ROOK_MAGIC[sq] = new Magic();
            BISHOP_MAGIC[sq] = new Magic();

            for (int pt : new int[]{ROOK, BISHOP}) {
                Magic magic = pt == ROOK ? ROOK_MAGIC[sq] : BISHOP_MAGIC[sq];

                // Exclude board edges, so we don't rely on unreachable squares
                long edges = ((FILE_A_BB | FILE_H_BB) & ~fileBB(Square.file(sq))) |
                        ((RANK_1_BB | RANK_8_BB) & ~rankBB(Square.rank(sq)));

                // The mask defines relevant blocker squares for this square and piece
                magic.mask = createAttacks(pt, sq, 0) & ~edges;
                magic.index = attackTableIndex;

                // The shift tells us how many bits to shift after multiplying with the magic
                magic.shift = 64 - bitCount(magic.mask);

                long[] blockersList = new long[MAX_OCCUPANCY_CONFIGURATION];
                long[] attacksList = new long[MAX_OCCUPANCY_CONFIGURATION];
                int size = 0;

                // Enumerate all blocker combinations (subset getBy mask)
                long blockers = 0;
                do {
                    blockersList[size] = blockers;
                    attacksList[size] = createAttacks(pt, sq, blockers);
                    blockers = (blockers - magic.mask) & magic.mask;
                    ++size;
                } while (blockers != 0);

                // Attempt to find a magic number that avoids collisions
                int[] numTryList = new int[MAX_OCCUPANCY_CONFIGURATION]; // Used to track per-index usage across attempts
                int numTry = 0, i = 0, index;

                while (i < size) {
                    // Try a new candidate magic number
                    magic.magicNum = RANDOM.nextLong() & RANDOM.nextLong() & RANDOM.nextLong();
                    ++numTry;

                    for (i = 0; i < size; ++i) {
                        index = magic.occupancyIndex(blockersList[i]);

                        // If index unused in current trial or attack matches previous one, it's valid
                        if (numTryList[index] < numTry || attacksList[i] == magic.attacks(blockersList[i])) {
                            numTryList[index] = numTry;
                            ATTACKS_TABLE[attackTableIndex + index] = attacksList[i];
                        } else {
                            // Collision: restart the entire process with new magic number
                            break;
                        }
                    }
                }
                failsFoundMagicCount += numTry;
                // Reserve space in attackTable for this square's configurations
                attackTableIndex += size;
            }
        }

        return failsFoundMagicCount;
    }

    /**
     * Internal helper to generate slider attacks from a square given an occupancy.
     * Used by magic init and also to precompute geometry tables.
     */
    private static long createAttacks(int pieceType, int square, long occupancy) {
        long attacks = 0;

        for (int direction : Direction.allDirections(pieceType)) {
            int sqInPath = square;
            while (Square.canAddDirection(sqInPath, direction) && (squareToBB(sqInPath) & occupancy) == 0) {
                sqInPath = sqInPath + direction;
                attacks |= squareToBB(sqInPath);
            }
        }
        return attacks;
    }

    /**
     * Initializes attack information for chess pieces.
     * This method calculates and stores pre-calculated moves, pin data,
     * and various piece attacks for each square on the chessboard.
     */
    private static void initAttackInfo() {
        // Loop through each square on the chessboard
        for (int sq1 = A1; sq1 <= H8; ++sq1) {
            long sq1BB = squareToBB(sq1);

            // Bishop moves
            PIECES_PSEUDO_ATTACKS[BISHOP][sq1] = attacks(BISHOP, sq1, EMPTY_BB);

            // Rook moves
            PIECES_PSEUDO_ATTACKS[ROOK][sq1] = attacks(ROOK, sq1, EMPTY_BB);

            // Queen moves (combination valueOf rook and bishop)
            PIECES_PSEUDO_ATTACKS[QUEEN][sq1] = PIECES_PSEUDO_ATTACKS[ROOK][sq1] |
                    PIECES_PSEUDO_ATTACKS[BISHOP][sq1];

            // Knight moves
            for (int direction : Direction.allDirections(KNIGHT))
                if (Square.canAddDirection(sq1, direction))
                    PIECES_PSEUDO_ATTACKS[KNIGHT][sq1] |= squareToBB(sq1 + direction);

            // King moves
            for (int direction : Direction.allDirections(KING))
                if (Square.canAddDirection(sq1, direction))
                    PIECES_PSEUDO_ATTACKS[KING][sq1] |= squareToBB(sq1 + direction);

            // Pawn moves for both white and black
            PAWN_ATTACKS[WHITE][sq1] = shiftUpLeft(sq1BB) | shiftUpRight(sq1BB);
            PAWN_ATTACKS[BLACK][sq1] = shiftDownLeft(sq1BB) | shiftDownRight(sq1BB);

            // Initialize arrays for path and line between squares
            for (int sq2 = A1; sq2 <= H8; ++sq2) {
                if (sq1 == sq2) {
                    PATH_BETWEEN[sq1][sq1] = LINE_THROUGH[sq1][sq1] = sq1BB;
                    continue;
                }
                long sq2BB = squareToBB(sq2);
                // Determine the piece type that attacks both squares (if any)
                int pt = (sq2BB & attacks(ROOK, sq1)) != 0 ? ROOK :
                        (sq2BB & attacks(BISHOP, sq1)) != 0 ? BISHOP : NULL_PIECE_TYPE;

                // Calculate path between squares
                PATH_BETWEEN[sq1][sq2] = pt != NULL_PIECE_TYPE ? ((attacks(pt, sq1, sq2BB)
                        & attacks(pt, sq2, sq1BB)) | sq2BB) : sq2BB;

                // Calculate line between squares
                LINE_THROUGH[sq1][sq2] = pt != NULL_PIECE_TYPE ? ((attacks(pt, sq1, EMPTY_BB)
                        & attacks(pt, sq2, EMPTY_BB)) | sq1BB | sq2BB) : EMPTY_BB;

                // Note: Uncomment the following line if PathToEdge array is defined
                // PathToEdge[sq][sq2]  = attacks(pt, sq2, squareToBB(sq)) & LineBetween[sq][sq2] ^ sq;
            }

            // Note: Uncomment the following lines if PathBetween, LeftDiagonal, and RightsDiagonal arrays are defined
            // PathBetween[sq][NO_SQUARE] = FullBoard;
            // is from white prespective we look up.
            // left diagnoal valueOf h8 is 0 a
//            System.out.println(addSafety(sq, UP_LEFT));
            LEFT_DIAGONAL[sq1] = LINE_THROUGH[sq1][Square.addSafety(sq1, UP_LEFT)] | LINE_THROUGH[sq1][Square.addSafety(sq1, DOWN_RIGHT)];
            RIGHT_DIAGONAL[sq1] = LINE_THROUGH[sq1][Square.addSafety(sq1, UP_RIGHT)] | LINE_THROUGH[sq1][Square.addSafety(sq1, DOWN_LEFT)];
        }
    }

    // ================================
    // Board geometry helpers
    // ================================

    public static long leftDiagonal(int square) {
        return LEFT_DIAGONAL[square];
    }

    public static long rightDiagonal(int square) {
        return RIGHT_DIAGONAL[square];
    }

    /**
     * Returns a bitboard mask of the path segment from {@code squareFrom} to {@code squareTo}.
     * <p>
     * Rules:
     * - If the squares are aligned (same rank, file, or diagonal):
     * Includes all squares *between* them, plus {@code squareTo}, but not {@code squareFrom}.
     * - If the squares are the same:
     * Includes only that single square.
     * - If not aligned:
     * Includes only {@code squareTo}.
     * <p>
     * Typical use cases:
     * - Checking if pieces block the way between two squares
     * - Determining squares that would block a check or pin
     */
    public static long pathBetween(int squareFrom, int squareTo) {
        return PATH_BETWEEN[squareFrom][squareTo];
    }

    /**
     * Returns a bitboard mask of the full line passing through both squares.
     * <p>
     * Rules:
     * - If the squares are aligned (same rank, file, or diagonal):
     * Includes every square on that line in both directions, including both {@code squareFrom} and {@code squareTo}.
     * - If the squares are the same:
     * Returns only that single square (degenerate case).
     * - If not aligned:
     * Returns an empty mask.
     * <p>
     * Typical use cases:
     * - Detecting pins (checking if the king, attacker, and pinned piece lie on the same line)
     * - Determining all attack squares for sliding pieces
     */
    public static long lineThrough(int squareFrom, int squareTo) {
        return LINE_THROUGH[squareFrom][squareTo];
    }

    /** Bitboard mask for the given file index (0..7). */
    public static long fileBB(int file) {
        return FILES_BB[file];
    }

    /** Bitboard mask for the given rank index (0..7). */
    public static long rankBB(int rank) {
        return RANKS_BB[rank];
    }

    // ================================
    // Bit operations & small utils
    // ================================

    public static long popLsb(long bb) {
        return bb & (bb - 1);
    }

    public static int bitCount(long bitboard) {
        return Long.bitCount(bitboard);
    }

    public static boolean has2OrMoreBits(long bb) {
        return (bb & (bb - 1)) != 0;
    }

    public static boolean hasOneBit(long bb) {
        // first check if bb has at least on bit and second check that has exactly one bit
        return bb != 0 && (bb & (bb - 1)) == 0;
    }

    // ================================
    // Square <-> bitboard conversions
    // ================================

    //  =====    bitboard <-> square utiliti     ============

    public static long squareToBB(int square) {
        return 1L << square;
    }


    // in case valueOf bb == 0 it will return 64 == NO_SQUARE.
    public static int lsbToSquare(long bb) {
        return Long.numberOfTrailingZeros(bb);
    }

    public static int msbToSquare(long bb) {
        return bb == 0 ? NULL_SQUARE : H8 - Long.numberOfLeadingZeros(bb);
    }

    public static Square[] getSquares(long bb) {
        Square[] result = new Square[bitCount(bb)];

        for (int index = 0, sq = lsbToSquare(bb); bb != 0; bb = popLsb(bb)) {
            result[index++] = Square.getBy(sq);
        }

        return result;
    }

    public static void forEachSquareIndex(long bb, Consumer<Integer> consumer) {
        for (int sq = lsbToSquare(bb); bb != 0; bb = popLsb(bb)) {
            consumer.accept(sq);
        }
    }

    // ================================
    // Shifts (directional)
    // ================================

    // ===========      shift  methods         ==================
    public static long shiftLeft(long bb) {
        return (bb & ~FILE_A_BB) >>> 1;
    }

    public static long shiftRight(long bb) {
        return (bb & ~FILE_H_BB) << 1;
    }

    public static long shiftUp(long bb) {
        return bb << 8;
    }

    public static long shiftDown(long bb) {
        return bb >>> 8;
    }

    public static long shiftUpLeft(long bb) {
        return (bb & ~FILE_A_BB) << 7;
    }

    public static long shiftUpRight(long bb) {
        return (bb & ~FILE_H_BB) << 9;
    }

    public static long shiftDownLeft(long bb) {
        return (bb & ~FILE_A_BB) >>> 9;
    }

    public static long shiftDownRight(long bb) {
        return (bb & ~FILE_H_BB) >>> 7;
    }

    public static long shiftDownTwice(long bb) {
        return bb >>> 16;
    }

    public static long shiftUpTwice(long bb) {
        return bb << 16;
    }

    // ================================
    // Color / alignment helpers
    // ================================

    // todo give a categoruy

    /**
     * return true if all the set bit in bb are on light square or dark square.
     * it returns true on empty bb
     *
     * @param bb bitboard
     * @return
     */
    public static boolean isOnSameColor(long bb) {
        return (bb & DARK_SQUARES_BB) == bb || (bb & LIGHT_SQUARES_BB) == bb;

    }

    // rerun true if all the square on the same file or rank or diagonal
    public static boolean onSameLine(int square1, int square2, int square3) {
        return (LINE_THROUGH[square1][square2] & squareToBB(square3)) != 0; //todo
    }

    // ================================
    // Pseudo-attacks (no occupancy)
    // ================================

    // ====      attack methods  without occupancy ============
    //pseudo attacks
    public static long pawnAttacks(int side, int square) {
        return PAWN_ATTACKS[side][square];
    }

    public static long knightAttacks(int square) {
        return PIECES_PSEUDO_ATTACKS[KNIGHT][square];
    }

    public static long bishopAttacks(int square) {
        return PIECES_PSEUDO_ATTACKS[BISHOP][square];
    }

    public static long rookAttacks(int square) {
        return PIECES_PSEUDO_ATTACKS[ROOK][square];
    }

    public static long queenAttacks(int square) {
        return PIECES_PSEUDO_ATTACKS[BISHOP][square] | PIECES_PSEUDO_ATTACKS[ROOK][square];
    }

    public static long kingAttacks(int square) {
        return PIECES_PSEUDO_ATTACKS[KING][square];
    }

    /**
     * Pseudo-attacks for all non-pawn piece types.
     * (Pawns require side info, so they have a dedicated method.)
     */
    public static long attacks(int pieceType, int square) {
        assert pieceType != PAWN;
        return PIECES_PSEUDO_ATTACKS[pieceType][square];
    }

    /**
     * Pawn attacks and forward pushes (single/double) from a square, ignoring blockers.
     * Useful for quick move-gen sketches or highlight masks.
     */
    public static long pawnAttackAndPush(int side, int square) {
        int pd = Direction.forward(side);
        return pawnAttacks(side, square) |
                squareToBB(square + pd) |
                (Square.rank(square) == Rank.flippedIfBlack(side, RANK_2) ? squareToBB(square + pd + pd) : 0); // push twice
    }

    /**
     * Convenience: all destinations (attacks + pawn pushes if PAWN).
     * For non-pawns, equivalent to {@link #attacks(int, int)}.
     */
    public static long validDestinations(int side, int pieceType, int square) {
        return pieceType == PAWN ? pawnAttackAndPush(side, square) : attacks(pieceType, square);
    }

    // ================================
    // Attacks with occupancy (sliders use magic)
    // ================================

    // ====    attacks by occupancy
    public static long bishopAttacks(int square, long occupancy) {
        return BISHOP_MAGIC[square].attacks(occupancy);
    }

    public static long rookAttacks(int square, long occupancy) {
        return ROOK_MAGIC[square].attacks(occupancy);
    }

    public static long queenAttacks(int square, long occupancy) {
        return bishopAttacks(square, occupancy) | rookAttacks(square, occupancy);
    }

    /**
     * Attacks for any piece type from a square given an occupancy mask.
     * For non-sliders (knight/king) occupancy is ignored.
     */
    public static long attacks(int pieceType, int square, long occupancy) {
        assert pieceType != PAWN;

        return switch (pieceType) {
            case BISHOP -> bishopAttacks(square, occupancy);
            case ROOK -> rookAttacks(square, occupancy);
            case QUEEN -> bishopAttacks(square, occupancy) | rookAttacks(square, occupancy);
            case KNIGHT -> knightAttacks(square);
            case KING -> kingAttacks(square);
            default -> 0;
        };
    }


    // ================================
    // Printing / debugging helpers
    // ================================

    // =========     print        =========

    /** Prints a 64-bit binary string (no board layout). */
    public static void printBits(long bb) {
        System.out.println(toBinaryString(bb));
    }

    /** Returns a 64-char binary string for the given bitboard (MSB first). */
    public static String toBinaryString(long bb) {
        return String.format("%64s", Long.toBinaryString(bb)).replace(' ', '0');
    }

    /**
     * Pretty-prints a bitboard as an ASCII board (A1 at bottom-left, H8 at top-right).
     * Returns false to make it convenient for inline debugging (e.g., `return printBB(bb);`).
     */
    public static void printBB(long bb) {
        for (int i = 8 - 1; i >= 0; --i) {
            System.out.println("  +---+---+---+---+---+---+---+---+");
            System.out.print(i + 1 + " ");

            for (int j = 0; j < 8; ++j) {
                char to_print = (bb & squareToBB((i * 8) + j)) != 0 ? '1' : ' ';
                System.out.print("| " + to_print + " ");
            }
            System.out.println("|");
        }
        System.out.println("  +---+---+---+---+---+---+---+---+");
        System.out.println("    a   b   c   d   e   f   g   h\n");
    }
}


