////package chesslib.move;
////import chesslib.Bitboard;
////import chesslib.*;
////import chesslib.Position;
////import chesslib.PositionState;
////import chesslib.move.Move;
////import chesslib.types.*;
////import org.jetbrains.annotations.NotNull;
////
////import java.util.List;
////
////import static chesslib.Bitboard.*;
////import static chesslib.move.Move.*;
////import static chesslib.types.Castling.*;
////import static chesslib.types.Direction.*;
////import static chesslib.types.Piece.*;
////import static chesslib.types.PieceType.*;
////import static chesslib.types.Side.*;
////import static chesslib.types.Square.*;
////
////public class MoveGenerator {
////
////
////    public static void createAll(@NotNull Position pos, @NotNull MoveList moveList) {
////
////        int side = pos.getSideToMove();
////        int enemySide = flipped(side);
////        int kSq = pos.getState().kingSquare;
////        long occupancy = pos.occupancy();
////        long enemy = pos.occupancyBySide(enemySide);
////        long empty = ~pos.occupancy();
////        long enemyOrEmpty = enemy | empty;
////        PositionState state = pos.getState();
////
////
////        //king moves
////        long attacksKing = attacks(KING, state.kingSquare) & enemyOrEmpty;
////        for (; attacksKing != 0; attacksKing &= (attacksKing - 1)) {
////            int dest = lsbToSquare(attacksKing);
////            if (pos.attackersBB(enemySide, dest, occupancy ^ squareToBB(kSq)) == 0)
////                moveList.add(create(kSq, dest));
////        }
////        // in double check only king can move
////        if (has2OrMoreBits(pos.checker())) return;
////
////        //castling
////        int allCastling = allCastling(side) & state.castlingRights;
////        if (allCastling != 0) {
////            if ((allCastling & Castling.ALL_SHORT) != 0) {
////                int move = pos.getCastlingMove(allCastling & Castling.ALL_SHORT);
////                if (pos.isLegalCastlingMove(move))
////                    moveList.add(move);
////            }
////            if ((allCastling & Castling.ALL_LONG) != 0) {
////                int move = pos.getCastlingMove(allCastling & Castling.ALL_LONG);
////                if (pos.isLegalCastlingMove(move)) moveList.add(move);
////            }
////        }
////        long checker = pos.checker();
////        long pinMask = pos.pinMask();
////        long pinMaskDiagonals = pinMask & bishopAttacks(kSq);
////        long pinMaskRankFile = pinMask & rookAttacks(kSq);
////
////        long checkMask = checker == 0 ? FULL_BB : pathBetween(kSq, lsbToSquare(checker));
////        // pawns move
////        long pawns = pos.occupancyBySideAndType(side, PAWN);
////        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
////        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq); // todo do it depens on color?
////
////        long lrPawns = pawns & ~pinMaskRankFile;
////        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));
////        // its calculate according to size.
////        long leftPawns, rightPawns, pushP, push2P, rankToPromote;
////        int up , upRight, upLeft;
////
////        if (side == WHITE_VAL){
//////            generateWhitePawnMoves(checkMask, pinMaskRankFile, pinMaskDiagonals, enemy, empty, pos, moveList);
////
////            // direction from white perspective
////            rankToPromote = RANK_7_BB;
////            up = UP;
////            upRight = UP_RIGHT;
////            upLeft = UP_LEFT;
////
////            leftPawns = lrPawns & ~pinOnRightD & shiftDownRight(checkMask & enemy);
////            rightPawns = lrPawns & ~pinOnLeftD & shiftDownLeft(checkMask & enemy);
////            pushP = pPawns & shiftDown(empty);
////            push2P = pushP & RANK_2_BB & Bitboard.shiftDownTwice(empty & checkMask);
////            pushP &= shiftDown(checkMask);
////
////
////        }
////
////        else {
////            // direction from black perspective
////            rankToPromote = RANK_2_BB;
////            up = DOWN;
////            upRight = DOWN_LEFT;
////            upLeft = DOWN_RIGHT;
////
////            leftPawns = lrPawns & ~pinOnRightD & shiftUpLeft(checkMask & enemy);
////            rightPawns = lrPawns & ~pinOnLeftD & shiftUpRight(checkMask & enemy);
////
////            pushP = pPawns & shiftUp(empty);
////            push2P = pushP & RANK_7_BB & Bitboard.shiftUpTwice(empty & checkMask);
////            pushP &= shiftUp(checkMask);
////
//////            generateBlackPawnMoves(checkMask, pinMaskRankFile, pinMaskDiagonals, enemy, empty, pos, moveList);
////        }
//////        int enemySide = flipped(pos.getSideToMove());
//////        int kSq = pos.kingSquare();
////
////
////
////
////
////        int move, start, dest;
////        if (pos.enPassant() != NO_SQUARE_VAL) {
////            long epPawns = lrPawns & pawnAttacks(enemySide, pos.enPassant());
////            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
////
////                if (pos.isLegalEnPassantMove(move = create(lsbToSquare(epPawns), pos.enPassant(), EN_PASSANT)))
////                    moveList.add(move);
////
////            }
////        }
////
////
////
////        if (((leftPawns | rightPawns | pushP) & rankToPromote) != 0) {
////            long lPromotePawns = leftPawns & rankToPromote;
////            long rPromotePawns = rightPawns & rankToPromote;
////            long pPromotePawns = pushP & rPromotePawns;
////
////            leftPawns ^= lPromotePawns;
////            rightPawns ^= rPromotePawns;
////            pushP ^= pPromotePawns;
////
////            for (; lPromotePawns != 0; lPromotePawns &= (lPromotePawns - 1)) {
////                start = lsbToSquare(lPromotePawns);
////                dest = start + upLeft;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; rPromotePawns != 0; rPromotePawns &= (rPromotePawns - 1)) {
////                start = lsbToSquare(rPromotePawns);
////                dest = start + upRight;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; pPromotePawns != 0; pPromotePawns &= (pPromotePawns - 1)) {
////                start = lsbToSquare(pPromotePawns);
////                dest = start + up;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////        }
////
////        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
////            start = lsbToSquare(leftPawns);
////            moveList.add(create(start, start + upLeft, NORMAL_PAWN_MOVE));
////        }
////        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
////            start = lsbToSquare(rightPawns);
////            moveList.add(create(start, start + upRight, NORMAL_PAWN_MOVE));
////        }
////        for (; pushP != 0; pushP &= (pushP - 1)) {
////            start = lsbToSquare(pushP);
////            moveList.add(create(start, start + up, NORMAL_PAWN_MOVE));
////        }
////
////        for (; push2P != 0; push2P &= (push2P - 1)) {
////            start = lsbToSquare(push2P);
////            moveList.add(create(start, start + 2 * up, PAWN_PUSH_TWICE));
////        }
////
////
////
////        checkMask &= enemyOrEmpty;
////        // knights
////        long knights = pos.occupancyBySideAndType(side, KNIGHT) & ~pinMask;
////        for (; knights != 0; knights &= (knights - 1)) {
////            start = lsbToSquare(knights);
////            long attacks = knightAttacks(start) & checkMask;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
////
////        //rooks and queen
////        long rookQueen = pos.occupancyBySideAndType(side, ROOK, QUEEN);
////        long rooksPin = rookQueen & pinMaskRankFile;
////        for (; rooksPin != 0; rooksPin &= (rooksPin - 1)) {
////            start = lsbToSquare(rooksPin);
////
////            long attacks = rookAttacks(start, occupancy) & checkMask & pinMaskRankFile;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
////
////        long rooksNotPin = rookQueen & ~pinMask;
////        for (; rooksNotPin != 0; rooksNotPin &= (rooksNotPin - 1)) {
////            start = lsbToSquare(rooksNotPin);
////            long attacks = rookAttacks(start, occupancy) & checkMask;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
////
////        //bishop and queen
////        long bishopsQueen = pos.occupancyBySideAndType(side, BISHOP, QUEEN);
////        long bishopPin = bishopsQueen & pinMaskDiagonals;
////
////
////        for (; bishopPin != 0; bishopPin &= (bishopPin - 1)) {
////            start = lsbToSquare(bishopPin);
////            long attacks = bishopAttacks(start, occupancy) & checkMask & pinMaskDiagonals;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
////
////        long bishopsNotPin = bishopsQueen & ~pinMask;
////
////        for (; bishopsNotPin != 0; bishopsNotPin &= (bishopsNotPin - 1)) {
////            start = lsbToSquare(bishopsNotPin);
////            long attacks = bishopAttacks(start, occupancy) & checkMask;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
////    }
////
////
////    // its need to be for both color for efficenty peprformance
////    // target is ememy or ompty and check mask
////    private static void generateWhitePawnMoves(long checkMask, long pinMaskRankFile, long pinMaskDiagonals,
////                                               long enemy, long empty, @NotNull Position pos, MoveList moveList) {
////
////        int enemySide = flipped(pos.getSideToMove());
////        int kSq = pos.kingSquare();
////        long pawns = pos.occupancyByPiece(WHITE_PAWN);
////        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
////        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq); // todo do it depens on color?
////
////        long lrPawns = pawns & ~pinMaskRankFile;
////        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));
////
////        long leftPawns = lrPawns & ~pinOnRightD & shiftDownRight(checkMask & enemy);
////        long rightPawns = lrPawns & ~pinOnLeftD & shiftDownLeft(checkMask & enemy);
////
////        long pushP = pPawns & shiftDown(empty);
////        long push2P = pushP & RANK_2_BB & Bitboard.shiftDownTwice(empty & checkMask);
////        pushP &= shiftDown(checkMask);
////
////        int move, start, dest;
////        if (pos.enPassant() != NO_SQUARE_VAL) {
////            long epPawns = lrPawns & pawnAttacks(enemySide, pos.enPassant());
////            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
////
////                if (pos.isLegalEnPassantMove(move = create(lsbToSquare(epPawns), pos.enPassant(), EN_PASSANT)))
////                    moveList.add(move);
////
////            }
////        }
////
////
////
////        if (((leftPawns | rightPawns | pushP) & RANK_7_BB) != 0) {
////            long lPromotePawns = leftPawns & RANK_7_BB;
////            long rPromotePawns = rightPawns & RANK_7_BB;
////            long pPromotePawns = pushP & RANK_7_BB;
////
////            leftPawns ^= lPromotePawns;
////            rightPawns ^= rPromotePawns;
////            pushP ^= pPromotePawns;
////
////            for (; lPromotePawns != 0; lPromotePawns &= (lPromotePawns - 1)) {
////                start = lsbToSquare(lPromotePawns);
////                dest = start + UP_LEFT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; rPromotePawns != 0; rPromotePawns &= (rPromotePawns - 1)) {
////                start = lsbToSquare(rPromotePawns);
////                dest = start + UP_RIGHT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; pPromotePawns != 0; pPromotePawns &= (pPromotePawns - 1)) {
////                start = lsbToSquare(pPromotePawns);
////                dest = start + UP;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////        }
////
////        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
////            start = lsbToSquare(leftPawns);
////            moveList.add(create(start, start + UP_LEFT, NORMAL_PAWN_MOVE));
////        }
////        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
////            start = lsbToSquare(rightPawns);
////            moveList.add(create(start, start + UP_RIGHT, NORMAL_PAWN_MOVE));
////        }
////        for (; pushP != 0; pushP &= (pushP - 1)) {
////            start = lsbToSquare(pushP);
////            moveList.add(create(start, start + UP, NORMAL_PAWN_MOVE));
////        }
////
////        for (; push2P != 0; push2P &= (push2P - 1)) {
////            start = lsbToSquare(push2P);
////            moveList.add(create(start, start + 2 * UP, PAWN_PUSH_TWICE));
////        }
////
////    }
////
////
////    // its need to be for both color for efficenty peprformance
////    // target is ememy or ompty and check mask
////    private static void generateBlackPawnMoves(long checkMask, long pinMaskRankFile, long pinMaskDiagonals,
////                                               long enemy, long empty, Position pos, MoveList moveList) {
////
////        int enemySide = flipped(pos.getSideToMove());
////        int kSq = pos.kingSquare();
////        long pawns = pos.occupancyByPiece(BLACK_PAWN);
////        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
////        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq); // todo do it depens on color?
////
////        long lrPawns = pawns & ~pinMaskRankFile;
////        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));
////
////        long leftPawns = lrPawns & ~pinOnRightD & shiftUpLeft(checkMask & enemy);
////        long rightPawns = lrPawns & ~pinOnLeftD & shiftUpRight(checkMask & enemy);
////
////        long pushP = pPawns & shiftUp(empty);
////        long push2P = pushP & RANK_7_BB & Bitboard.shiftUpTwice(empty & checkMask);
////        pushP &= shiftUp(checkMask);
////
////
////        int move, start, dest;
////        if (pos.enPassant() != NO_SQUARE_VAL) {
////            long epPawns = lrPawns & pawnAttacks(enemySide, pos.enPassant());
////            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
////
////                if (pos.isLegalEnPassantMove(move = create(lsbToSquare(epPawns), pos.enPassant(), EN_PASSANT)))
////                    moveList.add(move);
////
////            }
////        }
////
////
////        if (((leftPawns | rightPawns | pushP) & RANK_2_BB) != 0) {
////            long lPromotePawns = leftPawns & RANK_2_BB;
////            long rPromotePawns = rightPawns & RANK_2_BB;
////            long pPromotePawns = pushP & RANK_2_BB;
////
////            leftPawns ^= lPromotePawns;
////            rightPawns ^= rPromotePawns;
////            pushP ^= pPromotePawns;
////
////            for (; lPromotePawns != 0; lPromotePawns &= (lPromotePawns - 1)) {
////                start = lsbToSquare(lPromotePawns);
////                dest = start + DOWN_RIGHT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; rPromotePawns != 0; rPromotePawns &= (rPromotePawns - 1)) {
////                start = lsbToSquare(rPromotePawns);
////                dest = start + DOWN_LEFT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////
////            for (; pPromotePawns != 0; pPromotePawns &= (pPromotePawns - 1)) {
////                start = lsbToSquare(pPromotePawns);
////                dest = start + DOWN;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////        }
////
////        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
////            start = lsbToSquare(leftPawns);
////            moveList.add(create(start, start + DOWN_RIGHT, NORMAL_PAWN_MOVE));
////        }
////        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
////            start = lsbToSquare(rightPawns);
////            moveList.add(create(start, start + DOWN_LEFT, NORMAL_PAWN_MOVE));
////        }
////        for (; pushP != 0; pushP &= (pushP - 1)) {
////            start = lsbToSquare(pushP);
////            moveList.add(create(start, start + DOWN, NORMAL_PAWN_MOVE));
////        }
////        for (; push2P != 0; push2P &= (push2P - 1)) {
////            start = lsbToSquare(push2P);
////            moveList.add(create(start, start + 2 * DOWN, PAWN_PUSH_TWICE));
////        }
////
////    }
////
////
////    public static void perft(Position pos, int depth) {
////        long startTime = System.nanoTime();
////
////        if (depth <= 0) return;
////        PositionState state = new PositionState();
////        MoveList moveList = new MoveList(pos);
////        long nodes = 0, count;
////        for (int move : moveList) {
////            if (depth > 1) {
////                pos.makeMove(move, state);
////                count = numMoves(pos, depth - 1);
////                pos.undoMove(move);
////            } else
////                count = 1;
////
////            nodes += count;
////            System.out.println(getName(move) + ": " + count);
////
////        }
////        long endTime = System.nanoTime();
////        long elapsedTime = endTime - startTime;
////        double elapsedSeconds = (double) elapsedTime / 1_000_000_000.0; // Convert to seconds
////        System.out.println("Nodes count: " + nodes);
////        System.out.println("time: " + elapsedSeconds + "\n");
////    }
////
////
////    private static long numMoves(Position pos, int depth) {
////        PositionState state = new PositionState();
////        long cont, nodes = 0;
////        MoveList moveList = new MoveList(pos);
////        if (depth == 1) {
////            return moveList.size();
////        }
////        for (int move : moveList) {
////            pos.makeMove(move, state);
////            cont = numMoves(pos, depth - 1);
////            nodes += cont;
////            pos.undoMove(move);
////
////        }
////        return nodes;
////    }
////}
//
//
//// version 2
//
//package chesslib.move;
//import chesslib.Bitboard;
//import chesslib.Position;
//import chesslib.PositionState;
//import chesslib.types.*;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.function.BiFunction;
//import java.util.function.Function;
//
//import static chesslib.Bitboard.*;
//import static chesslib.move.Move.*;
//import static chesslib.types.Castling.*;
//import static chesslib.types.Direction.*;
//import static chesslib.types.PieceType.*;
//import static chesslib.types.Side.*;
//import static chesslib.types.Square.*;
//
//public class MoveGenerator {
//
//
//    public static void createAll(@NotNull Position pos, @NotNull MoveList moveList) {
//
//        int side = pos.getSideToMove();
//        int enemySide = flipped(side);
//        int kSq = pos.getState().kingSquare;
//        long occupancy = pos.occupancy();
//        long enemy = pos.occupancyBySide(enemySide);
//        long empty = ~pos.occupancy();
//        long enemyOrEmpty = enemy | empty;
//        PositionState state = pos.getState();
//
//
//        //king moves
//        long attacksKing = attacks(KING, state.kingSquare) & enemyOrEmpty;
//        for (; attacksKing != 0; attacksKing &= (attacksKing - 1)) {
//            int dest = lsbToSquare(attacksKing);
//            if (pos.attackersBB(enemySide, dest, occupancy ^ squareToBB(kSq)) == 0)
//                moveList.add(create(kSq, dest));
//        }
//        // in double check only king can move
//        if (has2OrMoreBits(pos.checker())) return;
//
//        //castling
//        int allCastling = allCastling(side) & state.castlingRights;
//        if (allCastling != 0) {
//            if ((allCastling & Castling.ALL_SHORT) != 0) {
//                int move = pos.getCastlingMove(allCastling & Castling.ALL_SHORT);
//                if (pos.isLegalCastlingMove(move))
//                    moveList.add(move);
//            }
//            if ((allCastling & Castling.ALL_LONG) != 0) {
//                int move = pos.getCastlingMove(allCastling & Castling.ALL_LONG);
//                if (pos.isLegalCastlingMove(move)) moveList.add(move);
//            }
//        }
//        long checker = pos.checker();
//        long pinMask = pos.pinMask();
//        long pinMaskDiagonals = pinMask & bishopAttacks(kSq);
//        long pinMaskRankFile = pinMask & rookAttacks(kSq);
//
//        long checkMask = checker == 0 ? FULL_BB : pathBetween(kSq, lsbToSquare(checker));
//        // pawns move
//        long pawns = pos.occupancyBySideAndType(side, PAWN);
//        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
//        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq); // todo do it depens on color?
//
//        long lrPawns = pawns & ~pinMaskRankFile;
//        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));
//
//        // its calculate according to size.
//        long leftPawns, rightPawns, pushP, push2P, rankToPromote;
//        int up , upRight, upLeft;
//
//        if (side == WHITE_VAL){ // direction from white perspective
//            rankToPromote = RANK_7_BB;
//            up = UP;
//            upRight = UP_RIGHT;
//            upLeft = UP_LEFT;
//
//            leftPawns = lrPawns & ~pinOnRightD & shiftDownRight(checkMask & enemy);
//            rightPawns = lrPawns & ~pinOnLeftD & shiftDownLeft(checkMask & enemy);
//            pushP = pPawns & shiftDown(empty);
//            push2P = pushP & RANK_2_BB & Bitboard.shiftDownTwice(empty & checkMask);
//            pushP &= shiftDown(checkMask);
//        }
//
//        else { // direction from black perspective
//            rankToPromote = RANK_2_BB;
//            up = DOWN;
//            upRight = DOWN_LEFT;
//            upLeft = DOWN_RIGHT;
//
//            leftPawns = lrPawns & ~pinOnRightD & shiftUpLeft(checkMask & enemy);
//            rightPawns = lrPawns & ~pinOnLeftD & shiftUpRight(checkMask & enemy);
//
//            pushP = pPawns & shiftUp(empty);
//            push2P = pushP & RANK_7_BB & Bitboard.shiftUpTwice(empty & checkMask);
//            pushP &= shiftUp(checkMask);
//        }
//
//
//        int move, start;
//        if (pos.enPassant() != NO_SQUARE_VAL) {
//            long epPawns = lrPawns & pawnAttacks(enemySide, pos.enPassant());
//            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
//                if (pos.isLegalEnPassantMove(move = create(lsbToSquare(epPawns), pos.enPassant(), EN_PASSANT)))
//                    moveList.add(move);
//
//            }
//        }
//
//
//        if (((leftPawns | rightPawns | pushP) & rankToPromote) != 0) {
//            long lPromotePawns = leftPawns & rankToPromote;
//            long rPromotePawns = rightPawns & rankToPromote;
//            long pPromotePawns = pushP & rPromotePawns;
//
//            leftPawns ^= lPromotePawns;
//            rightPawns ^= rPromotePawns;
//            pushP ^= pPromotePawns;
//
//            createPromoteMoves(moveList, upLeft, lPromotePawns);
//            createPromoteMoves(moveList, upRight, rPromotePawns);
//            createPromoteMoves(moveList, up, pPromotePawns);
//        }
//
//        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
//            start = lsbToSquare(leftPawns);
//            moveList.add(create(start, start + upLeft, NORMAL_PAWN_MOVE));
//        }
//        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
//            start = lsbToSquare(rightPawns);
//            moveList.add(create(start, start + upRight, NORMAL_PAWN_MOVE));
//        }
//        for (; pushP != 0; pushP &= (pushP - 1)) {
//            start = lsbToSquare(pushP);
//            moveList.add(create(start, start + up, NORMAL_PAWN_MOVE));
//        }
//
//        for (; push2P != 0; push2P &= (push2P - 1)) {
//            start = lsbToSquare(push2P);
//            moveList.add(create(start, start + 2 * up, PAWN_PUSH_TWICE));
//        }
//
//
//
//        checkMask &= enemyOrEmpty;
//        // knights
//        long knights = pos.occupancyBySideAndType(side, KNIGHT) & ~pinMask;
//        createSliderMoves(moveList, Bitboard::knightAttacks, checkMask, knights);
////        for (; knights != 0; knights &= (knights - 1)) {
////            start = lsbToSquare(knights);
////            long attacks = knightAttacks(start) & checkMask;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
//
//        //rooks and queen
//        long rookQueen = pos.occupancyBySideAndType(side, ROOK, QUEEN);
//        long rooksPin = rookQueen & pinMaskRankFile;
//        createSliderMoves(moveList, square ->rookAttacks(square, occupancy), checkMask & pinMaskRankFile, rooksPin);
////        for (; rooksPin != 0; rooksPin &= (rooksPin - 1)) {
////            start = lsbToSquare(rooksPin);
////
////            long attacks = rookAttacks(start, occupancy) & checkMask & pinMaskRankFile;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
//
//        long rooksNotPin = rookQueen & ~pinMask;
//        createSliderMoves(moveList, square ->rookAttacks(square, occupancy), checkMask, rooksNotPin);
////        for (; rooksNotPin != 0; rooksNotPin &= (rooksNotPin - 1)) {
////            start = lsbToSquare(rooksNotPin);
////            long attacks = rookAttacks(start, occupancy) & checkMask;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
//
//        //bishop and queen
//        long bishopsQueen = pos.occupancyBySideAndType(side, BISHOP, QUEEN);
//        long bishopPin = bishopsQueen & pinMaskDiagonals;
//
//        createSliderMoves(moveList, square ->bishopAttacks(square, occupancy), checkMask&pinMaskDiagonals, bishopPin);
////        for (; bishopPin != 0; bishopPin &= (bishopPin - 1)) {
////            start = lsbToSquare(bishopPin);
////            long attacks = bishopAttacks(start, occupancy) & checkMask & pinMaskDiagonals;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
//
//        long bishopsNotPin = bishopsQueen & ~pinMask;
//
//        createSliderMoves(moveList, square ->bishopAttacks(square, occupancy), checkMask, bishopsNotPin);
////        for (; bishopsNotPin != 0; bishopsNotPin &= (bishopsNotPin - 1)) {
////            start = lsbToSquare(bishopsNotPin);
////            long attacks = bishopAttacks(start, occupancy) & checkMask;
////            for (; attacks != 0; attacks &= (attacks - 1)) {
////                dest = lsbToSquare(attacks);
////                moveList.add(create(start, dest));
////            }
////        }
//    }
//
//    private static void createSliderMoves(@NotNull MoveList moveList, Function<Integer, Long> attacksFunc, long target, long pieces){
//        int start, dest;
//        for (; pieces != 0; pieces &= (pieces - 1)) {
//            start = lsbToSquare(pieces);
//            long attacks = attacksFunc.apply(start) & target;
//            for (; attacks != 0; attacks &= (attacks - 1)) {
//                dest = lsbToSquare(attacks);
//                moveList.add(create(start, dest));
//            }
//        }
//    }
//
//    private static void createPromoteMoves(@NotNull MoveList moveList, int direction, long promotePawns) {
//        int start, dest;
//        for (; promotePawns != 0; promotePawns &= (promotePawns - 1)) {
//            start = lsbToSquare(promotePawns);
//            dest = start + direction;
//            moveList.add(create(start, dest, PROMOTION, KNIGHT));
//            moveList.add(create(start, dest, PROMOTION, BISHOP));
//            moveList.add(create(start, dest, PROMOTION, ROOK));
//            moveList.add(create(start, dest, PROMOTION, QUEEN));
//        }
//    }
//
//
//    public static void perft(Position pos, int depth) {
//        long startTime = System.nanoTime();
//
//        if (depth <= 0) return;
//        PositionState state = new PositionState();
//        MoveList moveList = new MoveList(pos);
//        long nodes = 0, count;
//        for (int move : moveList) {
//            if (depth > 1) {
//                pos.makeMove(move, state);
//                count = numMoves(pos, depth - 1);
//                pos.undoMove(move);
//            } else
//                count = 1;
//
//            nodes += count;
//            System.out.println(getName(move) + ": " + count);
//
//        }
//        long endTime = System.nanoTime();
//        long elapsedTime = endTime - startTime;
//        double elapsedSeconds = (double) elapsedTime / 1_000_000_000.0; // Convert to seconds
//        System.out.println("Nodes count: " + nodes);
//        System.out.println("time: " + elapsedSeconds + "\n");
//    }
//
//
//    private static long numMoves(Position pos, int depth) {
//        PositionState state = new PositionState();
//        long cont, nodes = 0;
//        MoveList moveList = new MoveList(pos);
//        if (depth == 1) {
//            return moveList.size();
//        }
//        for (int move : moveList) {
//            pos.makeMove(move, state);
//            cont = numMoves(pos, depth - 1);
//            nodes += cont;
//            pos.undoMove(move);
//
//        }
//        return nodes;
//    }
//}
//
//
//
//
//
//
//
//// last version valueOf pawn moves
//
//
////
////    // its need to be for both color for efficenty peprformance
////    // target is ememy or ompty and check mask
////    private static void generateWhitePawnMoves(long checkMask, long pinMaskRankFile, long pinMaskDiagonals,
////                                               long enemy, long empty, @NotNull Position pos, MoveList moveList) {
////
////        int enemySide = flipped(pos.getSideToMove());
////        int kSq = pos.kingSquare();
////        long pawns = pos.occupancyByPiece(WHITE_PAWN);
////        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
////        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq); // todo do it depens on color?
////
////        long lrPawns = pawns & ~pinMaskRankFile;
////        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));
////
////        long leftPawns = lrPawns & ~pinOnRightD & shiftDownRight(checkMask & enemy);
////        long rightPawns = lrPawns & ~pinOnLeftD & shiftDownLeft(checkMask & enemy);
////
////        long pushP = pPawns & shiftDown(empty);
////        long push2P = pushP & RANK_2_BB & Bitboard.shiftDownTwice(empty & checkMask);
////        pushP &= shiftDown(checkMask);
////
////        int move, start, dest;
////        if (pos.enPassant() != NO_SQUARE_VAL) {
////            long epPawns = lrPawns & pawnAttacks(enemySide, pos.enPassant());
////            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
////
////                if (pos.isLegalEnPassantMove(move = create(lsbToSquare(epPawns), pos.enPassant(), EN_PASSANT)))
////                    moveList.add(move);
////
////            }
////        }
////
////
////
////        if (((leftPawns | rightPawns | pushP) & RANK_7_BB) != 0) {
////            long lPromotePawns = leftPawns & RANK_7_BB;
////            long rPromotePawns = rightPawns & RANK_7_BB;
////            long pPromotePawns = pushP & RANK_7_BB;
////
////            leftPawns ^= lPromotePawns;
////            rightPawns ^= rPromotePawns;
////            pushP ^= pPromotePawns;
////
////            for (; lPromotePawns != 0; lPromotePawns &= (lPromotePawns - 1)) {
////                start = lsbToSquare(lPromotePawns);
////                dest = start + UP_LEFT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; rPromotePawns != 0; rPromotePawns &= (rPromotePawns - 1)) {
////                start = lsbToSquare(rPromotePawns);
////                dest = start + UP_RIGHT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; pPromotePawns != 0; pPromotePawns &= (pPromotePawns - 1)) {
////                start = lsbToSquare(pPromotePawns);
////                dest = start + UP;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////        }
////
////        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
////            start = lsbToSquare(leftPawns);
////            moveList.add(create(start, start + UP_LEFT, NORMAL_PAWN_MOVE));
////        }
////        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
////            start = lsbToSquare(rightPawns);
////            moveList.add(create(start, start + UP_RIGHT, NORMAL_PAWN_MOVE));
////        }
////        for (; pushP != 0; pushP &= (pushP - 1)) {
////            start = lsbToSquare(pushP);
////            moveList.add(create(start, start + UP, NORMAL_PAWN_MOVE));
////        }
////
////        for (; push2P != 0; push2P &= (push2P - 1)) {
////            start = lsbToSquare(push2P);
////            moveList.add(create(start, start + 2 * UP, PAWN_PUSH_TWICE));
////        }
////
////    }
//
//
////    // its need to be for both color for efficenty peprformance
////    // target is ememy or ompty and check mask
////    private static void generateBlackPawnMoves(long checkMask, long pinMaskRankFile, long pinMaskDiagonals,
////                                               long enemy, long empty, Position pos, MoveList moveList) {
////
////        int enemySide = flipped(pos.getSideToMove());
////        int kSq = pos.kingSquare();
////        long pawns = pos.occupancyByPiece(BLACK_PAWN);
////        long pinOnLeftD = pinMaskDiagonals & Bitboard.leftDiagonal(kSq);
////        long pinOnRightD = pinMaskDiagonals & Bitboard.rightDiagonal(kSq); // todo do it depens on color?
////
////        long lrPawns = pawns & ~pinMaskRankFile;
////        long pPawns = pawns & ~pinMaskDiagonals & ~(pinMaskRankFile & rankBB(Square.rank(kSq)));
////
////        long leftPawns = lrPawns & ~pinOnRightD & shiftUpLeft(checkMask & enemy);
////        long rightPawns = lrPawns & ~pinOnLeftD & shiftUpRight(checkMask & enemy);
////
////        long pushP = pPawns & shiftUp(empty);
////        long push2P = pushP & RANK_7_BB & Bitboard.shiftUpTwice(empty & checkMask);
////        pushP &= shiftUp(checkMask);
////
////
////        int move, start, dest;
////        if (pos.enPassant() != NO_SQUARE_VAL) {
////            long epPawns = lrPawns & pawnAttacks(enemySide, pos.enPassant());
////            for (; epPawns != 0; epPawns &= (epPawns - 1)) {
////
////                if (pos.isLegalEnPassantMove(move = create(lsbToSquare(epPawns), pos.enPassant(), EN_PASSANT)))
////                    moveList.add(move);
////
////            }
////        }
////
////
////        if (((leftPawns | rightPawns | pushP) & RANK_2_BB) != 0) {
////            long lPromotePawns = leftPawns & RANK_2_BB;
////            long rPromotePawns = rightPawns & RANK_2_BB;
////            long pPromotePawns = pushP & RANK_2_BB;
////
////            leftPawns ^= lPromotePawns;
////            rightPawns ^= rPromotePawns;
////            pushP ^= pPromotePawns;
////
////            for (; lPromotePawns != 0; lPromotePawns &= (lPromotePawns - 1)) {
////                start = lsbToSquare(lPromotePawns);
////                dest = start + DOWN_RIGHT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////            for (; rPromotePawns != 0; rPromotePawns &= (rPromotePawns - 1)) {
////                start = lsbToSquare(rPromotePawns);
////                dest = start + DOWN_LEFT;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////
////
////            for (; pPromotePawns != 0; pPromotePawns &= (pPromotePawns - 1)) {
////                start = lsbToSquare(pPromotePawns);
////                dest = start + DOWN;
////                moveList.add(create(start, dest, PROMOTION, KNIGHT));
////                moveList.add(create(start, dest, PROMOTION, BISHOP));
////                moveList.add(create(start, dest, PROMOTION, ROOK));
////                moveList.add(create(start, dest, PROMOTION, QUEEN));
////            }
////        }
////
////        for (; leftPawns != 0; leftPawns &= (leftPawns - 1)) {
////            start = lsbToSquare(leftPawns);
////            moveList.add(create(start, start + DOWN_RIGHT, NORMAL_PAWN_MOVE));
////        }
////        for (; rightPawns != 0; rightPawns &= (rightPawns - 1)) {
////            start = lsbToSquare(rightPawns);
////            moveList.add(create(start, start + DOWN_LEFT, NORMAL_PAWN_MOVE));
////        }
////        for (; pushP != 0; pushP &= (pushP - 1)) {
////            start = lsbToSquare(pushP);
////            moveList.add(create(start, start + DOWN, NORMAL_PAWN_MOVE));
////        }
////        for (; push2P != 0; push2P &= (push2P - 1)) {
////            start = lsbToSquare(push2P);
////            moveList.add(create(start, start + 2 * DOWN, PAWN_PUSH_TWICE));
////        }
////
////    }
