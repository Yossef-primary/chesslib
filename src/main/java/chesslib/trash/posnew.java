//package chesslib;
//
//import chesslib.move.Move;
//import chesslib.types.*;
//
//import java.util.Arrays;
//import java.util.Random;
//import java.util.Stack;
//
//import static chesslib.Bitboard.*;
//import static chesslib.move.Move.*;
//import static chesslib.types.Castling.*;
//import static chesslib.types.Direction.forward;
//import static chesslib.types.File.*;
//import static chesslib.types.Rank.*;
//import static chesslib.types.Side.*;
//import static chesslib.types.PieceType.*;
//import static chesslib.types.Piece.*;
//import static chesslib.types.Square.*;
//
//// this class for engint purpus
//public class Position{
//
//
//    private static final int NUM_REPETITION_TO_DRAW = 3;
//    private static final int RULE_50_COUNT_TO_DRAW = 99;
//    private int sideToMove;
//    private int numMoves;
//    private PositionState state;
//    private Stack<PositionState> states = new Stack<>();
//    // board representation
//    private long occupancyBB;
//    private long[] occupancyBySideBB;
//    private long[] occupancyByPieceBB;
//    private long[] occupancyByTypeBB;
//    private int[] board;
//    private int numPieces;
//
//    private boolean isChess960;
//
//
//    private final int[] positionCount = new int[1 << 13];
//    private final int positionCountMask = (1 << 13) -1;
//
//    // castling data
//    private int[] castlingOptionsMask;
//    //    long castlingFullPath[CastlingOptionsNum]{}; // path that need to check if all square in the path are empty.
////    long castlingKingPath[CastlingOptionsNum]{}; // path that need to check if all square in the path not in check
//    private int[] castlingDestSquareKing;
//    private int[] castlingDestSquareRook;
//    private int[] castlingMoves;
//    // all the squares that king and rook goes through while doing the castling. (exclude king square
//    // and rook square) this path need to be empty on the board when we make this castling move.
//    private long[] castlingPath;
//    // all the squares the king goes through while doing the castling. exclude king sq. this path cant be
//    // under attack valueOf enemy pieces.
//    private long[] castlingKingPath;
//
//    // keys tables
//    private static final long[][] pieceSquareKeys = new long[Piece.VALUES_COUNT][Square.VALUES_COUNT];
//    private static final long[] enPassantKeys = new long[Square.VALUES_COUNT];
//    private static final long[] castlingKeys = new long[Castling.VALUES_COUNT]; //16 option for castling available 4 for whites (wl, wr, w no, w both) * 4 for black
//    private static final long colorKey;
//
//
//    // init key tables
//    static {
//        Random r = chesslib.Random.RANDOM;
//
//        for (int piece: Piece.intValues())
//            for (int square = A1_VAL; square <= H8_VAL; ++square)
//                pieceSquareKeys[piece][square] = r.nextLong();
//
//        for (int i = 0; i < castlingKeys.length; ++i)
//            castlingKeys[i] = r.nextLong();
//
//        for (int square = A1_VAL; square <= H8_VAL; ++square){
//            enPassantKeys[square] = r.nextLong();
//            enPassantKeys[flipped(square)] = r.nextLong();
//        }
//        enPassantKeys[NO_SQUARE_VAL] = 0; // its need for makeMove method that if it has no en passant
//        // we do key xor 0 == key
//
//        colorKey = r.nextLong();
//    }
//
//    public int collision = 0;
//
//
//    public Position(String newFen, PositionState state, boolean isChess960){
//        setFen(newFen, state, isChess960);
//    }
//
//    public String moveHistory(){
//        PositionState st = state;
//        StringBuilder res = new StringBuilder();
//        while (st.lastMove != 0){
//            res.insert(0, getName(st.lastMove) + " ");
//            st = st.previous;
//        }
//        return res.toString();
//    }
//
//    public String posString(){
//        StringBuilder result = new StringBuilder();
//        for (int i = 8 - 1; i >= 0; --i) {
//            result.append("  +---+---+---+---+---+---+---+---+\n");
//            result.append(i + 1).append(" ");
//
//            for (int j = 0; j < 8; ++j) {
//                int piece = getPiece((i * 8) + j);
//                char to_print = piece!= NULL_PIECE ? Piece.getName(piece) : ' ';//todo to check
//                result.append("| ").append(to_print).append(" ");
//            }
//            result.append("|\n");
//        }
//        result.append( "  +---+---+---+---+---+---+---+---+\n");
//        result.append( "    a   b   c   d   e   f   g   h\n\n");
//
//        result.append("Fen:        ").append(getFen()).append("\n");
//        result.append("Checker:    ").append(toBinaryString(state.checkers)).append("\n");
//        result.append("Pin Mask:   ").append(toBinaryString(state.pinMask)).append("\n");
//        result.append("key:        ").append(state.key);result.append("\n");
//        result.append("moves play: ").append(moveHistory()).append("\n");
//        result.append("repetition: " ).append(state.numRepetition).append("\n");
//        result.append("\n");
//        return result.toString();
//
//    }
//
//    public void printBoard(){
//        System.out.println(posString());
////        for (int i = 8 - 1; i >= 0; --i) {
////            System.out.println("  +---+---+---+---+---+---+---+---+");
////            System.out.print(i + 1 + " ");
////
////            for (int j = 0; j < 8; ++j) {
////                int piece = getPiece((i * 8) + j);
////                char to_print = piece!= NULL_PIECE ? Piece.getName(piece) : ' ';//todo to check
////                System.out.print( "| " + to_print + " ");
////            }
////            System.out.println("|");
////        }
////        System.out.println( "  +---+---+---+---+---+---+---+---+");
////        System.out.println( "    a   b   c   d   e   f   g   h\n");
////
////        System.out.println("Fen:        " + getFen());
////        System.out.println("Checker:    " + toBinaryString(state.checkers));
////        System.out.println("Pin Mask:   " + toBinaryString(state.pinMask));
////        System.out.println("key:        " + state.key);
////        System.out.println("repetition: " + state.numRepetition);
////        System.out.println("moves:      " + moveHistory());
////        System.out.println();
//    }
//
//    public void setFen(String fen, PositionState positionState, boolean isChess960) {
//        assert FenValidation.isValidFenSyntaxAndKingCount(fen);
//        this.isChess960 = isChess960;
//        this.state = positionState;
//        // 0. pars fen
//        String[] fenParts = splitFen(fen);
//        String boardFen = fenParts[0], colorFen =  fenParts[1], castlingFen =      fenParts[2],
//                epFen =    fenParts[3], rule50Fen = fenParts[4], fullMoveCountFen = fenParts[5];
//
//        // 1. reset the board
//        occupancyBB = 0;
//        occupancyBySideBB      = new long[Side.VALUES_COUNT];
//        occupancyByPieceBB     = new long[Piece.VALUES_COUNT];
//        occupancyByTypeBB      = new long[PieceType.VALUES_COUNT];
//        board                  = new int[Square.VALUES_COUNT];
//
//        castlingOptionsMask    = new int[Square.VALUES_COUNT];
//        castlingDestSquareKing = new int[Square.VALUES_COUNT];
//        castlingDestSquareRook = new int[Square.VALUES_COUNT];
//        castlingPath           = new long[Square.VALUES_COUNT];
//        castlingKingPath       = new long[Square.VALUES_COUNT];
//        castlingMoves          = new int[Castling.VALUES_COUNT];
//
//        // 2. set the board
//        int square = A8_VAL;
//        for (char c : boardFen.toCharArray()) {
//            if (c == '/')
//                square = square - 2*Rank.VALUES_COUNT; // go down in a rank
//            else if (Character.isDigit(c))
//                square += Character.getNumericValue(c);
//            else // add piece and advance square
//                addPiece(valueOf(c), square++);
//        }
//
//        int[] kingsSquares = {squareOf(WHITE_KING), squareOf(BLACK_KING)};
//
//        // 3. set castling info
//        int castlingRights = 0;
//        Arrays.fill(castlingOptionsMask, ALL_CASTLING);
//
//        castlingOptionsMask[kingsSquares[WHITE_VAL]] = ALL_CASTLING - (WHILE_SHORT | WHITE_LONG);
//        castlingOptionsMask[kingsSquares[BLACK_VAL]] = ALL_CASTLING - (BLACK_SHORT | BLACK_LONG);
//
//        for (char c : castlingFen.toCharArray()) {
//            int side = Character.isUpperCase(c) ? WHITE_VAL : BLACK_VAL;
//            c = Character.toLowerCase(c);
//            int rookSq, kingSq = kingsSquares[side];
//
//            // find the rookSq square, that made the castle.
//            if (c == 'k') {
//                rookSq = Square.flipped(side, H1_VAL);
//                while (getPiece(rookSq) != valueOf(side, ROOK) && rookSq > flipped(side, A1_VAL))
//                    rookSq--;
//
//            }
//            else if (c == 'q'){
//                rookSq = Square.flipped(side, A1_VAL);
//                while (getPiece(rookSq) != valueOf(side, ROOK) && rookSq < flipped(side,H1_VAL)) rookSq++;
//            }
//
//            else if (c >= 'a' && c <= 'h')
//                rookSq = valueOf(valueOf(c), flippedIfBlack(side, Rank.RANK_1_VAL));
//            else break; // c = "-"
//
//            int castleRight = rookSq > kingSq ? shortCastle(side) : longCastle(side);
//            int destKing = isShortCastle(castleRight) ? flipped(side, G1_VAL) : flipped(side, C1_VAL);
//            int destRook = isShortCastle(castleRight) ? flipped(side, F1_VAL) : flipped(side, D1_VAL);
//            //update castle right info
//            castlingRights += castleRight;
//            castlingOptionsMask[rookSq]    = ALL_CASTLING - castleRight;
//            castlingDestSquareKing[rookSq] = destKing;
//            castlingDestSquareRook[rookSq] = destRook;
//            castlingKingPath[rookSq]       = pathBetween(kingSq, destKing) & ~squareToBB(kingSq);
//            castlingPath[rookSq]           = castlingKingPath[rookSq] | pathBetween(rookSq, destRook) & ~squareToBB(rookSq);
//            castlingMoves[castleRight]     = create(kingSq, rookSq, CASTLING);
//        }
//
//        // 4. set epPassant sq.
//        int enSq = valueOf(epFen);
//        int sideMoved = flipped(sideToMove);
//        int pawnMovedDir = forward(sideMoved);
//        // if on valueOf this condition exist en passant move cant be done.
//        if (enSq == NO_SQUARE_VAL                                                                     ||
//                // check if exist pawn after the ep square.
//                (occupancyBySideAndType(sideMoved, PAWN) & squareToBB(enSq + pawnMovedDir)) == 0  ||
//                // check it the squares that pawn moved goes through them are empty.
//                ((squareToBB(enSq - pawnMovedDir) | squareToBB(enSq)) & occupancyBB) != 0             ||
//                // check if exist pawn valueOf side to move that can go to ep square.
//                (occupancyBySideAndType(sideToMove, PAWN) & pawnAttacks(sideMoved, enSq)) == 0)
//            enSq = NO_SQUARE_VAL;
//
//        // 5. set state
//        sideToMove               = valueOf(colorFen.charAt(0));
//        numMoves                 = 2 * Math.min(0, Integer.parseInt(fullMoveCountFen) - 1) + sideToMove; // num moves start from 0
//        state.enPassant          = enSq;
//        state.rule50             = Integer.parseInt(rule50Fen);
//        state.kingSquare         = kingsSquares[sideToMove];
//        state.opponentKingSquare = kingsSquares[flipped(sideToMove)];
//        state.capturedPiece      = NULL_PIECE;
//        state.numRepetition      = 0;
//        state.ply                = 0;
//        state.previous           = null;
//        state.castlingRights     = castlingRights;
//        state.checkers           = attackersBB(flipped(sideToMove), state.kingSquare, occupancyBB);
//        state.pinMask            = pinMask(flipped(sideToMove), state.kingSquare);
//        state.key                = (sideToMove*colorKey) ^ enPassantKeys[state.enPassant] ^ castlingKeys[castlingRights];
//        state.lastMove = NULL_MOVE;
//        assertPositionIsLegal();
//
//        // after the key is set. its need to add this position to position count table.
//        positionCount[(int) (state.key & positionCountMask)] = 1;
//    }
//
//    public String getFen() {
//        StringBuilder boardFen = new StringBuilder(), CFen = new StringBuilder();
//        int piece;
//        int numEmptySq = 0;
//
//        // loop over all the ranks on the board from black side to white side
//        for (int square = A1_VAL; square <= H8_VAL; ++square) {
//            if ((piece  = getPiece(flipped(square)))!= NULL_PIECE) {
//                boardFen.append(numEmptySq != 0 ? numEmptySq : "").append(getName(piece));
//                numEmptySq = 0;
//            }
//            else
//                numEmptySq++;
//
//            if (file(flipped(square)) == FILE_H_VAL) {// h file todo to define enum for files
//                boardFen.append(numEmptySq != 0 ? numEmptySq : "").append(square == H8_VAL ? "" : "/");
//                numEmptySq = 0;
//            }
//        }
//
//        // calculate the castling fen
//        if (canCastle(WHILE_SHORT))
//            CFen.append(isChess960? Character.toUpperCase(getName(file(destSquare(castlingMove(WHILE_SHORT))))): "K");
//        if (canCastle(WHITE_LONG))
//            CFen.append(isChess960? Character.toUpperCase(getName(file(destSquare(castlingMove(WHITE_LONG)))))  : "Q");
//        if (canCastle(BLACK_SHORT))
//            CFen.append(isChess960? getName(file(destSquare(castlingMove(BLACK_SHORT)))) : "k");
//        if (canCastle(BLACK_LONG))
//            CFen.append(isChess960? getName(file(destSquare(castlingMove(BLACK_LONG))))  : "q");
//        if (state.castlingRights == 0)
//            CFen.append("-");
//
//        return String.format("%s %c %s %s %d %d", boardFen, getName(sideToMove),
//                CFen, getName(state.enPassant), state.rule50, numMoves/2 + 1);
//    }
//
//    public boolean canCastle(int castlingRights){
//        return (state.castlingRights & castlingRights) != 0;
//    }
//
//    private int castlingRookFile(int castleRight){
//        return file(destSquare(castlingMove(castleRight)));
//    }
//
//    public int getSideToMove() {
//        return sideToMove;
//    }
//
//    // for debug assume the move is legal
//    public void makeMove(int move, PositionState newState) {
////        assert isPseudoLegalMove(move) : getName(move) + "\n" + posString();
////        assert (isLegalMove(move)) : getName(move) + "\n" + posString();
//        int start = startSquare(move);
//        int dest = destSquare(move);
//        int moveType = moveType(move);
//        int sideMoved = sideToMove;
//
//        // 1: update the new state.
//        // this will update later by the part 2.
//        newState.capturedPiece = NULL_PIECE;
//        newState.enPassant = NO_SQUARE_VAL;
//        newState.rule50 = state.rule50 + 1;
//
//        newState.ply = state.ply + 1;
//        newState.castlingRights = state.castlingRights;
//        newState.key = state.key ^ enPassantKeys[state.enPassant] ^ colorKey;
//
//        //update castling key if needed update castling rights
//        if (state.castlingRights != 0 && (state.castlingRights &
//                castlingOptionsMask[start] & castlingOptionsMask[dest]) != state.castlingRights) {
//            newState.castlingRights &= (castlingOptionsMask[start] & castlingOptionsMask[dest]);
//            newState.key ^= (castlingKeys[state.castlingRights] ^ castlingKeys[newState.castlingRights]);
//        }
//
//        // update the state it's must be called before the board update, because the removeNode-add-move piece method change the key valueOf the state.
//        newState.previous = state;
//        state = newState;
//        sideToMove = flipped(sideToMove);
//        numMoves++;
//
//
//        // 2: update board
//        if ((squareToBB(dest) & occupancyBySideBB[sideToMove]) != 0) {
//            state.capturedPiece = getPiece(dest);
//            state.rule50 = 0;
//            removePiece(dest);
//        }
//
//        if (moveType == NORMAL) {
//            movePiece(start, dest);
//        } else if (moveType == NORMAL_PAWN_MOVE) {
//            movePiece(start, dest);
//            state.rule50 = 0;
//        }
//        // castling move encoded dest to sq valueOf rook
//        else if (moveType == Move.CASTLING) {
//            movePiece(start, castlingDestSquareKing[dest]);
//            movePiece(dest, castlingDestSquareRook[dest]);
//        } else if (moveType == Move.EN_PASSANT) {
//            removePiece(dest - forward(sideMoved));
//            movePiece(start, dest);
////            state.capturedPiece = valueOf(sideToMove, PAWN);
//        } else if (moveType == Move.PAWN_PUSH_TWICE) {
//            movePiece(start, dest);
//            int epSquare = start + forward(sideMoved);
//            state.enPassant = (occupancyByType(sideToMove, PAWN) &
//                    pawnAttacks(sideMoved, epSquare)) == 0 ? NO_SQUARE_VAL : epSquare;
//            state.key ^= enPassantKeys[state.enPassant]; // in case valueOf epSquare == NO_SQUARE_VAL its do nothing.
//        } else if (moveType == Move.PROMOTION) {
//            addPiece(valueOf(sideMoved, promotePT(move)), dest);
//            removePiece(start);
//        }
//
//        // 3: update king data info
//        int kingSq = squareOf(sideToMove, KING); // todo only update the ksq valueOf side who make the move
//        state.kingSquare = kingSq;
//        state.checkers = attackersBB(sideMoved, kingSq, occupancyBB);
//        state.pinMask = pinMask(flipped(sideToMove), kingSq);
//
//
//        // 4: update repetition info
////        int numRepetition = ++positionCount[(int) (state.key & positionCountMask)];
////        if (numRepetition == 1 && isRepite())
////            numRepetition = 2;
//        updateRepetition();
////        for (int i = 0; i < 50; i++) {
////            if(state.numRepetition == 18+i)
////                System.out.println("print");
////        }
//
//        state.lastMove = move;
////        assertPositionIsLegal();
////        assert positionIsLegal(): getName(move)+"\n"+posString();
//    }
//
//    private boolean isRepite() {
//        int num = 0;
//        PositionState st = state;
//        while (st != null ){
//            if (state.key == st.key)
//                num++;
//            st = st.previous;
//        }
//        return num == 2;
//    }
//
//    public void undoMove(int move) {
//        int start = startSquare(move);
//        int dest = destSquare(move);
//        int moveType = moveType(move);
//
//        // update board
//        if (moveType == NORMAL || moveType == NORMAL_PAWN_MOVE || moveType == PAWN_PUSH_TWICE)
//            movePiece(dest, start);
//
//        else if (moveType == Move.CASTLING){
//            movePiece(castlingDestSquareKing[dest], start);
//            movePiece(castlingDestSquareRook[dest], dest); // move rook
//        }
//        else if (moveType == Move.EN_PASSANT){ // in en passant move state.capturedPiece encoded as Piece.NO_PIECE
//            addPiece(flipped(getPiece(dest)), dest +  forward(sideToMove));
//            movePiece(dest, start);
//        }
//        else if (moveType == Move.PROMOTION){
//            addPiece(valueOf(flipped(sideToMove), PAWN), start);
//            removePiece(dest);
//        }
//        if (state.capturedPiece != NULL_PIECE) addPiece(state.capturedPiece, dest);
//
//        // update data
//        sideToMove = flipped(sideToMove);
//        --numMoves;
//        state = state.previous;
//        --positionCount[(int) (state.key & positionCountMask)];
//        assertPositionIsLegal();
//    }
//
//    private void updateRepetition () {
//        state.numRepetition = 0;
//        int end = Math.min(state.rule50, state.ply);
//        if (end >= 4) {
//            PositionState step = state.previous.previous;
//            for (int i = 4;  i <= end; i += 2) {
//                step = step.previous.previous;
//                if (step.key == state.key) {
//                    state.numRepetition = step.numRepetition + 1;
//                    return;
//                }
//            }
//        }
//    }
//
//    public int getPiece(int square) {
////        assert Square.isValid(square);
//        return board[square];
//    }
//
//    public int squareOf(int side, int type){
//        return lsbToSquare(occupancyByPieceBB[valueOf(side, type)]);
//    }
//
//    public int squareOf(int piece){
//        return lsbToSquare(occupancyByPieceBB[piece]);
//    }
//
//    public int squareOfSideToMoveKing(){
//        return state.kingSquare;
//    }
//
//    public int squareOfOpponentKing(){
//        return state.opponentKingSquare;
//    }
//    // return bb with the pieces from both color
//    public long attackersBB(int square, long occupancy){
//        return  (rookAttacks(square, occupancy)   & occupancyByType(ROOK, QUEEN))   |
//                (bishopAttacks(square, occupancy) & occupancyByType(BISHOP, QUEEN)) |
//                (knightAttacks(square)            & occupancyByType(KNIGHT))            |
//                (kingAttacks(square)              & occupancyByType(KING))              |
//                (pawnAttacks(WHITE_VAL, square)   & occupancyByPiece(BLACK_PAWN))       |
//                (pawnAttacks(BLACK_VAL, square)   & occupancyByPiece(WHITE_PAWN));
//    }
//
//    public long attackersBB(int attacksSide, int square, long occupancy){
//        return occupancyBySideBB[attacksSide] & attackersBB(square, occupancy);
//    }
//
//    private void assertPositionIsLegal(){
//        // only one king for each side.
//        assert hasOneBit(occupancyByPiece(WHITE_KING)) && hasOneBit(occupancyByPiece(BLACK_KING)) :
//                "invalid nam valueOf kings";
//
//        //opponent side to move not in check
//        assert attackersBB(sideToMove, squareOf(flipped(sideToMove), KING), occupancy()) == 0 :
//                "king not ide to move under attack\n"+posString();
//
//        //pawn can not be on the first or last rank
//        assert (occupancyByType(PAWN) & (RANK_1_BB | RANK_8_BB)) == 0 :
//                "pawn on first or last rank";
//
//        //en passant validation was done by set fen method.
//
//        // check if the board representation are legal.//todo
//        for (int square = A1_VAL; square <= H8_VAL; ++square){
//            int piece = getPiece(square);
//            assert piece != NULL_PIECE || (squareToBB(square) & (occupancy() |
//                    occupancyBySide(WHITE_VAL) | occupancyBySide(BLACK_VAL))) == 0 :
//                    String.format("1 on square %s, piece is %c bitboards not synchronized.\n"+posString(),
//                            getName(square), getName(piece));
//            for (int type: PieceType.intValues()){
//                if (type == type(piece)){ // all bits need to be set
//                    assert (squareToBB(square)                           &
//                            occupancy()                                  &
//                            occupancyBySide(side(piece))            &
//                            occupancyByType(type)                   &
//                            ~occupancyByPiece(flipped(piece))      &
//                            ~occupancyBySide(flipped(side(piece))) &
//                            occupancyByPiece(piece)) != 0      :
//                            String.format("2 on square %s, piece is %c bitboards not synchronized.\n"+posString(),
//                                    getName(square), getName(piece));;
//
//                }
//                else { // all bits need to be empty
//                    assert (squareToBB(square) & (
//                            occupancyByPiece(valueOf(WHITE_VAL, type)) |
//                                    occupancyByPiece(valueOf(BLACK_VAL, type)) |
//                                    occupancyByType(type))) == 0:
//                            String.format("3 on square %s, piece is %c bitboards not synchronized.\n"+posString(),
//                                    getName(square), getName(piece));;
//                }
//            }
//        }
//
//        //castling validation, check if all the castling given by user are valid on the board
//        for (int castleRight: new int[]{WHILE_SHORT, WHITE_LONG, BLACK_SHORT, BLACK_LONG}){
//            if ((state.castlingRights & castleRight) != 0) {
//                int side = castlingSide(castleRight);
//                boolean isShort = isShortCastle(castleRight);
//                int kSq = startSquare(castlingMoves[castleRight]);
//                int rSq = destSquare(castlingMoves[castleRight]);
//
//                if (getPiece(kSq) != valueOf(side, KING) ||
//                        getPiece(rSq) != valueOf(side, ROOK) ||
//                        flippedIfBlack(side, RANK_1_VAL) != rank(kSq) || //king in the correct rank
//                        (isShort && kSq > rSq || !isShort && kSq < rSq))
//                    assert  false: "castling rights invalid\n"+posString();
////            if (canCastle(castleRight)) {
////                int side = castlingSide(castleRight);
////                boolean isShort = (isShortCastle(castleRight));
////                int kSq = startSquare(castlingMoves[castleRight]);
////                int rSq = destSquare(castlingMoves[castleRight]);
////                assert getPiece(rSq) == valueOf(side, ROOK) &&
////                       flippedIfBlack(side, RANK_1_VAL) == rank(kSq) && //king in the correct rank
////                       (isShort && kSq < rSq || !isShort && kSq > rSq) :
////                       "castling rights invalid";
//            }
//        }
//    }
//
//    public boolean positionIsLegal(){
//        // only one king for each side.
//        if (!hasOneBit(occupancyByPiece(WHITE_KING)) || !hasOneBit(occupancyByPiece(BLACK_KING)))
//            return false;
//
//        //opponent side to move not in check
//        if (attackersBB(sideToMove, squareOf(flipped(sideToMove), KING), occupancy()) != 0) return false;
//
//        //pawn can not be on the first or last rank
//        if ((occupancyByType(PAWN) & (RANK_1_BB | RANK_8_BB)) != 0) return false;
//
//        //en passant validation was done by set fen method.
//
//        // check if the board representation are legal.
//        for (int square = A1_VAL; square <= H8_VAL; ++square){
//            int piece = getPiece(square);
//            if (piece == NULL_PIECE){
//                if ((squareToBB(square) & (occupancy() |
//                        occupancyBySide(WHITE_VAL) | occupancyBySide(BLACK_VAL))) != 0)
//                    return false;
//
//            }
//            for (int type: PieceType.intValues()){
//                if (type == type(piece)){ // all bits need to be set
//                    if ((squareToBB(square)                              &
//                            occupancy()                                  &
//                            occupancyBySide(side(piece))            &
//                            occupancyByType(type)                   &
//                            ~occupancyByPiece(flipped(piece))      &
//                            ~occupancyBySide(flipped(side(piece))) &
//                            occupancyByPiece(piece)) == 0 )
//                        return false;
//
//
//                }
//                else { // all bits need to be empty
//                    if ((squareToBB(square) & (
//                            occupancyByPiece(valueOf(WHITE_VAL, type)) |
//                                    occupancyByPiece(valueOf(BLACK_VAL, type)) |
//                                    occupancyByType(type))) != 0)
//                        return false;
//
//                }
//            }
//        }
//        //castling validation, check if all the castling given by user are valid on the board
//        for (int  castleRight: new int[]{WHILE_SHORT, WHITE_LONG, BLACK_SHORT, BLACK_LONG}){
//            if ((state.castlingRights & castleRight) != 0) {
//                int side = castlingSide(castleRight);
//                boolean isShort = isShortCastle(castleRight);
//                int kSq = startSquare(castlingMoves[castleRight]);
//                int rSq = destSquare(castlingMoves[castleRight]);
//
//                if (getPiece(kSq) != valueOf(side, KING) ||
//                        getPiece(rSq) != valueOf(side, ROOK) ||
//                        flippedIfBlack(side, RANK_1_VAL) != rank(kSq) || //king in the correct rank
//                        (isShort && kSq > rSq || !isShort && kSq < rSq))
//                    return false;
//            }
//        }
//        return true;
//    }
//
//    private int castlingMove(int castleRight){
//        return castlingMoves[castleRight];
//    }
//    /**
//     * Checks if a chess move is pseudo-legal based on the following conditions:
//     * <ol>
//     *   <li>Verify if the side to move corresponds to the side valueOf the moving piece.</li>
//     *   <li>Ensure that the piece can move according to the occupancy on the board.</li>
//     *   <li>Check if the move corresponds to the allowed directions for the piece.</li>
//     *   <li>Verify if the move type is valid for the given piece.</li>
//     *   <li>Check if the move cancels a check on the position (not checking if the moved piece is on pin).</li>
//     * </ol>
//     *
//     * @param move The encoded representation valueOf the move to be checked.
//     * @return {@code true} if the move is pseudo-legal, {@code false} otherwise.
//     */
//    public boolean isPseudoLegalMove(int move) {
//        // Step 0: Extract relevant information from the move
//        int start = startSquare(move);
//        int dest = destSquare(move);
//        int piece = getPiece(start);
//        int side = side(piece);
//        int moveType = moveType(move);
//        int pt = type(piece);
//
//        // Step 1, 2, 3: Check if the side to move corresponds to the piece side and if the piece can move
//        // according to the occupancy on the board
//        if (moveType != CASTLING && (side != sideToMove ||
//                (attacksAndMoves(sideToMove, pt, start, occupancy()) & squareToBB(dest)) == 0)) // todo in handle with occopancy and handle with the pawn
//            return false;
//
//            // Step 4: Check if the move corresponds to the direction that the piece can walk
//        else if (moveType == CASTLING) {
//            int castleRight = start < dest ? shortCastle(sideToMove) : longCastling(sideToMove);
//            if (state.checkers != 0 ||
//                    ((castleRight & state.castlingRights) == 0) ||
//                    castlingMoves[castleRight] != move) // This validates that start contains king and dest contains rook.
//                return false;
//        }
//
//        // Step 4: Check if the move type corresponds to the move
//        else if (moveType == EN_PASSANT) {
//            if (pt != PAWN || dest != state.enPassant &&
//                    (pawnAttacks(sideToMove, start) & squareToBB(dest)) == 0)
//                return false;
//        }
//
//        // Step 4: Check if the move type corresponds to the move
//        else if (moveType == PAWN_PUSH_TWICE) { //todo maybe not nedede
//            if (pt != PAWN && Square.distance(start, dest) == Direction.UP_DIRECTION * 2)
//                return false;
//        }
//
//        // Step 5: Check if the position is in check and if the piece move cancels this check
//        // (not checking if the piece moved is on pin)
//        if (state.checkers != 0) {
//            long checkMask = pathBetween(state.kingSquare, lsbToSquare(state.checkers));
//            return type(piece) == KING ?
//                    attackersBB(flipped(sideToMove), dest, occupancyBB ^ squareToBB(state.kingSquare)) == 0 :
//                    hasOneBit(state.checkers) && (checkMask & squareToBB(dest)) != 0;
//        }
//
//        return true; // If all checks pass, the move is considered pseudo-legal
//    }
//
//
//
//    /**
//     * Checks if a chess move is legal, considering the move's pseudo-legality and additional conditions.
//     * Assumes the move is pseudo-legal.
//     *
//     * @param move The encoded representation valueOf the move.
//     * @return True if the move is legal, false otherwise.
//     */
//    public boolean isLegalMove(int move) {
//        int start = startSquare(move);
//        int dest = destSquare(move);
//        int moveType = moveType(move);
//        int pieceMoved = getPiece(dest);
//        int ksq = state.kingSquare;
//        int enemySide = flipped(sideToMove);
//
//        if (moveType == NORMAL || moveType == PAWN_PUSH_TWICE || moveType == PROMOTION || moveType == NORMAL_PAWN_MOVE) {
//            // In case valueOf the king move, check if the destination square is safe.
//            return (moveType(pieceMoved) == KING) ?
//                    attackersBB(enemySide, dest, occupancyBB ^ squareToBB(ksq)) == 0 :
//                    ((squareToBB(start) & state.pinMask) == 0) || onSameLine(start, dest, ksq);
//        }
//
//        // Here, move type must be either EN_PASSANT or CASTLING.
//        return moveType == CASTLING ? isLegalCastlingMove(move) : isLegalEnPassantMove(move);
//    }
//
//
//    /**
//     * Checks if a castling move is legal.
//     * Assumes the move is pseudo-legal.
//     *
//     * @param move
//     * @return True if the castling move is legal, false otherwise.
//     */
//    public boolean isLegalCastlingMove(int move) {
//        int dest = destSquare(move);
//        int enemySide = flipped(sideToMove);
//        long kingPath = castlingKingPath[dest];
//
//        // king can not be in a check and all castling path must be empty.
//        if (state.checkers != 0 || (castlingPath[dest] & occupancyBB) != 0) return false;
//
//        // loop over all the square in castling king path and check if the enemy threatening them.
//        for (;kingPath != 0; kingPath = popLsb(kingPath))
//            if (attackersBB(enemySide, lsbToSquare(kingPath), occupancyBB) != 0) return false;
//
//        // to include the case valueOf chess 960 that calling rook pin to the king,
//        // for example queen on a1 rook on b1 and king on c1
//        return (squareToBB(castlingDestSquareRook[dest]) & state.pinMask) == 0;
//    }
//
//    public int getCastlingMove(int castleRight){
//        return castlingMoves[castleRight];
//    }
//
//    /**
//     * Checks if an en passant move is legal.
//     * Assumes the move is pseudo-legal.
//     *
//     * @param epMove
//     * @return True if the en passant move is legal, false otherwise.
//     */
//    public boolean isLegalEnPassantMove(int epMove) {
//        int start = startSquare(epMove);
//        int dest = destSquare(epMove);
//        int ksq =  state.kingSquare;
//        int enemySide = flipped(sideToMove);
//        int captureSq = dest - forward(sideToMove);
//        long occAfterMove = occupancyBB ^ squareToBB(start) ^ squareToBB(dest) ^ squareToBB(captureSq);
//        return (rookAttacks(ksq, occAfterMove) & occupancyBySideAndType(enemySide, ROOK, QUEEN)) == 0 &&
//                (bishopAttacks(ksq, occAfterMove) & occupancyBySideAndType(enemySide, BISHOP, QUEEN)) == 0;
//    }
//
//    /**
//     * Adds a piece to the chessboard at the specified square.
//     * Assumes the square is empty.
//     *
//     * @param piece  The piece to be added.
//     * @param square The square where the piece is added.
//     */
//    private void addPiece(int piece, int square){
//        assert Piece.isValid(piece);
//        long sqBB = Bitboard.squareToBB(square);
//        occupancyBB |= sqBB;
//        occupancyBySideBB[side(piece)] |= sqBB;
//        occupancyByTypeBB[type(piece)] |= sqBB;
//        occupancyByPieceBB[piece] |= sqBB;
//        board[square] = piece;
//        numPieces++;
//        state.key ^= pieceSquareKeys[piece][square];
//
//    }
//
//    /**
//     * Removes a piece from the chessboard at the specified square.
//     * Assumes the square is not empty.
//     *
//     * @param square The square from which the piece is removed.
//     */
//    private void removePiece(int square){
//        assert square != NO_SQUARE_VAL;
//        long sqBB = Bitboard.squareToBB(square);
//        int piece = board[square];
//        occupancyBB ^= sqBB;
//        occupancyBySideBB[side(piece)] ^= sqBB;
//        occupancyByTypeBB[type(piece)] ^= sqBB;
//        occupancyByPieceBB[piece] ^= sqBB;
//        board[square] = NULL_PIECE;
//        numPieces--;
//        state.key ^= pieceSquareKeys[piece][square];
//    }
//
//    /**
//     * Moves a piece on the chessboard from the start square to the destination square.
//     * Assumes there is a piece on the start square and the destination square is empty.
//     * If start == dest, it has no effect.
//     *
//     * @param start The starting square valueOf the piece.
//     * @param dest  The destination square for the piece.
//     */
//    private void movePiece(int start, int dest){
//        assert getPiece(start) != NULL_PIECE;
//        long startOrDestBB = Bitboard.squareToBB(start) | Bitboard.squareToBB(dest);
//        int piece = getPiece(start);
//        occupancyBB ^= startOrDestBB;
//        occupancyBySideBB[side(piece)] ^= startOrDestBB;
//        occupancyByTypeBB[type(piece)] ^= startOrDestBB;
//        occupancyByPieceBB[piece] ^= startOrDestBB;
//        board[start] = NULL_PIECE;
//        board[dest] = piece;
//        state.key ^= (pieceSquareKeys[piece][start] ^ pieceSquareKeys[piece][dest]);
//    }
//
//
//    public PositionState getState(){
//        return state;
//    }
//
//    /**
//     * Checks if the current board state represents insufficient material for a checkmate.
//     * Conditions checked:
//     * 1. Cover the case valueOf:
//     *    - King vs King
//     *    - King vs King + Knight or Bishop
//     *    - King vs King + 2 Bishops with the same color.
//     * 2. Ensure that the remaining pieces cannot deliver checkmate.
//     * 3. Ensure that the remaining bishops are on the same color.
//     *
//     * @return True if the material is insufficient for checkmate, false otherwise.
//     */
//    public boolean isInsufficientMaterial() {
//        return (numPieces <= 4 &&
//                occupancyByType(QUEEN, ROOK, PAWN) == 0) &&
//                Bitboard.isOnSameColor(occupancyByType(BISHOP));
//    }
//
//
//    public boolean inCheck(){
//        return state.checkers != 0;
//    }
//
//    public long pinMask(int attackSide, int targetSquare){
//        // all attackers around (on file, rank and tow diagonal valueOf targetSquare square) the square targetSquare
//        long attackers =
//                (occupancyBySideAndType(attackSide, BISHOP, QUEEN) & bishopAttacks(targetSquare)) |
//                        (occupancyBySideAndType(attackSide, ROOK, QUEEN)   & rookAttacks(targetSquare));
//        long pinMask = 0, pathToS, occupancy =  occupancyBB ^ attackers;
//
//        for ( ;attackers != 0; attackers = Bitboard.popLsb(attackers)) {
//            int square = lsbToSquare(attackers);
//            pathToS = Bitboard.pathBetween(targetSquare, square);
//            if (Bitboard.hasOneBit(pathToS & occupancy))
//                pinMask |= pathToS;
//        }
//        return pinMask;
//    }
//    // occupancy methods
//    public long occupancy(){
//        return occupancyBB;
//    }
//
//    public long occupancyBySide(int side) {
//        return occupancyBySideBB[side];
//    }
//
//    public long occupancyByType(int type) {
//        return  occupancyByTypeBB[type];
//    }
//
//    public long occupancyByType(int pieceType1, int pieceType2) {
//        return  occupancyByTypeBB[pieceType1] |
//                occupancyByTypeBB[pieceType2];
//    }
//
//    public long occupancyByType(int pieceType1, int pieceType2, int pieceType3) {
//        return  occupancyByTypeBB[pieceType1] |
//                occupancyByTypeBB[pieceType2] |
//                occupancyByTypeBB[pieceType3];
//    }
//
//    public long occupancyByPiece(int piece) {
//        return occupancyByPieceBB[piece];
//    }
//
//    public long occupancyByPiece(int piece1, int piece2) {
//        return  occupancyByPieceBB[piece1] |
//                occupancyByPieceBB[piece2];
//    }
//
//    public long occupancyByPiece(int piece1, int piece2, int piece3) {
//        return  occupancyByPieceBB[piece1] |
//                occupancyByPieceBB[piece2] |
//                occupancyByPieceBB[piece3];
//    }
//
//    public long occupancyBySideAndType(int side, int type) {
//        return occupancyByTypeBB[type] & occupancyBySideBB[side];
//    }
//
//    public long occupancyBySideAndType(int side, int pieceType1, int pieceType2) {
//        return (occupancyByTypeBB[pieceType1] | occupancyByTypeBB[pieceType2]) &
//                occupancyBySideBB[side];
//    }
//
//    private static String[] splitFen(String fen){
//        return fen.trim().replaceAll("\\s+", " ").split(" ");
//    }
//
//    public long pinMask() {
//        return state.pinMask;
//    }
//
//    public long checker() {
//        return state.checkers;
//    }
//
//    public boolean squareUnderAttack(int attackingSide, int square) {
//        return (occupancyBySideBB[attackingSide] & attackersBB(square, occupancyBB)) != 0;
//    }
//
//    public int enPassant() {
//        return state.enPassant;
//    }
//
//    public int kingSquare() {
//        return state.kingSquare;
//    }
//
//    public int rule50() {
//        return state.rule50;
//    }
//
//    public boolean inThreeFoldRepetition() {
//        return state.numRepetition == 2;
//    }
//
//    public boolean inRule50() {
//        return state.rule50 == RULE_50_COUNT_TO_DRAW;
//    }
//
//    public int lastMove() {
//        return state.lastMove;
//    }
//}
