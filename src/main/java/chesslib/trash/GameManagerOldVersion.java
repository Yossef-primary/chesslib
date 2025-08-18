//package chesslib;
//
//import chesslib.move.MoveList;
//import chesslib.types.*;
//import chesslib.move.Move;
//import org.jetbrains.annotations.NotNull;
////import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//import java.util.Random;
//
//import static chesslib.Bitboard.*;
//import static chesslib.Bitboard.rankBB;
//import static chesslib.move.Move.*;
//import static chesslib.types.Piece.*;
//import static chesslib.types.Piece.getName;
//import static chesslib.types.PieceType.PAWN;
//import static chesslib.types.Square.*;
//import static chesslib.types.Square.getName;
//
///**
// * Manages the state and logic valueOf a chess game.
// */
//public class GameManagerOldVersion {
//
//    public static final String FEN_START_GAME = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
//    private Position position;
//    private MoveList moveList;
//    private GameStatus gameStatus;
//    private final List<List<Move>> legalMovesBySquare = new ArrayList<>(Square.VALUES_COUNT);
//
//
//    /**
//     * Constructs a game manager with the specified FEN position and Chess960 flag.
//     *
//     * @param fen        the FEN position string
//     * @param isChess960 true if the game is Chess960, false otherwise
//     */
//    public GameManagerOldVersion(String fen, boolean isChess960) {
//        setFen(fen, isChess960);
//    }
//
//    /**
//     * Constructs a game manager with the specified FEN position.
//     *
//     * @param fen the FEN position string
//     */
//    public GameManagerOldVersion(String fen) {
//        this(fen, false);
//    }
//
//    /**
//     * Constructs a game manager for a standard chess game or Chess960.
//     *
//     * @param isChess960 true if the game is Chess960, false for standard chess
//     */
//    public GameManagerOldVersion(boolean isChess960) {
//        this(isChess960 ? createChess960Fen() : FEN_START_GAME, isChess960);
//    }
//
//    /**
//     * Constructs a game manager for a standard chess game.
//     */
//    public GameManagerOldVersion() {
//        this(FEN_START_GAME, false);
//    }
//
//    /**
//     * Sets the FEN position for the game manager.
//     *
//     * @param newFen     the new FEN position string
//     * @param isChess960 true if the game is Chess960, false otherwise
//     */
//    public void setFen(String newFen, boolean isChess960) {
//        position = new Position(newFen, new PositionState(), isChess960);
////        System.out.println(legalMovesBySquare.size());
////        legalMovesBySquare = new ArrayList<>(Square.VALUES_COUNT);
////        Collections.fill(legalMovesBySquare, new ArrayList<>());
//
////        for (int i = 0; i < Square.VALUES_COUNT; i++) {
////            legalMovesBySquare.add(null);
////        }
////        legalMovesBySquare.
//
//        updateState();
//    }
//
//    /**
//     * Sets the FEN position for the game manager.
//     *
//     * @param newFen the new FEN position string
//     */
//    public void setFen(String newFen) {
//        setFen(newFen, false);
//    }
//
//    /**
//     * Checks if the provided FEN string is valid.
//     *
//     * @param fen the FEN string to validate
//     * @return true if the FEN is valid, false otherwise
//     */
//    public boolean isValidFenSyntaxAndKingCount(@NotNull String fen) {
//        return FenValidation.isValidFenSyntaxAndKingCount(fen);
//    }
//
//    /**
//     * Gets the current FEN position.
//     *
//     * @return the FEN position string
//     */
//    public String getFen() {
//        return position.getFen();
//    }
//
//    /**
//     * Gets the side to play in the current position.
//     *
//     * @return the side to play
//     */
//    public Side sideToPlay() {
//        return Side.valueOf(position.sideToMove());
//    }
//
//    /**
//     * Retrieves the corresponding internal move valueBy for a given Move object.
//     *
//     * @param move the Move object to find in the current position
//     * @return the internal move valueBy or Move.NULL_MOVE if not found
//     */
//    private int getMove(Move move) {
//        for (int m : moveList) {
//            if (move.valueBy() == Move.valueBy(m))
//                return m;
//
//        }
//        return Move.NULL_MOVE;
//    }
//
//    /**
//     * Attempts to make a move in the current position.
//     *
//     * @param move the Move object representing the move
//     * @return true if the move was made successfully, false otherwise
//     */
//    public boolean makeMove(Move move) {
//        int m = getMove(move);
//        if (m == Move.NULL_MOVE)
//            return false;
//        position.makeMove(m, new PositionState());
//        updateState();
//        return true;
//    }
//
//    public void makePsoudoMove(Move m){
//        int move = Move.create(m.start().valueBy(), m.dest().valueBy(), moveType(m), m.promotePT().valueBy());
//        position.makeMove(move, new PositionState());
//    }
//
//    private int moveType(Move m){
//        PieceType ptMoved = getPiece(m.start()).type();
//        Side side = getPiece(m.start()).side();
//
//        return m.promotePT() != null ? Move.PROMOTION
//                : m.dest().valueBy() == position.enPassant() && ptMoved == PieceType.PAWN? EN_PASSANT
//                : ptMoved == PieceType.KING &&  getPiece(m.dest()).type() == PieceType.ROOK &&
//                    side == getPiece(m.dest()).side()? Move.CASTLING
//                : ptMoved == PieceType.PAWN && Square.distance(m.start().valueBy(), m.dest().valueBy()) == 16 ? PAWN_PUSH_TWICE
//                : NORMAL;
//
//    }
//
//
//
//    /**
//     * Undoes the last move made in the current position.
//     * If no move was made, does nothing.
//     */
//    public void undoMove() {
//        if (position.lastMove() != Move.NULL_MOVE) {
//            position.undoMove(position.lastMove());
//            updateState();
//        }
//    }
//
//    /**
//     * Checks if a given move is legal in the current position.
//     *
//     * @param move the Move object representing the move
//     * @return true if the move is legal, false otherwise
//     */
//    public boolean isLegalMove(Move move) {
//        return getMove(move) != Move.NULL_MOVE;
//    } //todo to use is legal move valueOf class position
//
//    /**
//     * Retrieves the last move made in the current position.
//     *
//     * @return the Move object representing the last move
//     */
//    public Move lastMove() {
//        return Move.valueOf(position.getState().lastMove);
//    }
//
//    /**
//     * Retrieves the move history valueOf the game.
//     *
//     * @return a list valueOf Move objects representing the move history
//     */
//    public List<Move> moveHistory() {
//        List<Move> result = new ArrayList<>();
//        PositionState st = position.getState();
//        while (st != null) {
//            result.add(0, Move.valueOf(st.lastMove));
//            st = st.previous;
//        }
//        return result;
//    }
//
//    /**
//     * Retrieves all legal moves from a specific square in the current position.
//     *
//     * @param from the source square for the moves
//     * @return a list valueOf Move objects representing legal moves from the source square
//     */
//    public List<Move> getAllLegalMoves(Square from) {
//        return legalMovesBySquare.get(from.valueBy());
////        List<Move> result = new ArrayList<>();
////        for (int move : moveList) {
////            if (Move.startSquare(move) == from.valueBy())
////                result.add(Move.valueOf(move));
////        }
////        return result;
//    }
//
//
//
//    // return list valueOf square that the piece on from square can move to.
//    public Set<Square> allDestinations(Piece piece, Square from){
//        long moves = Bitboard.attacksAndMoves(piece.side().valueBy(), piece.type().valueBy(), from.valueBy(), Bitboard.EMPTY_BB, Bitboard.EMPTY_BB);
//        Set<Square> result = new HashSet<>();
//        for (; moves != 0; moves &= (moves - 1))
//            result.add(Square.valueOf(lsbToSquare(moves)));
//        // todo if the king move add the castling square avialable
//        return result;
//    }
//
//    /**
//     * Retrieves all legal moves in the current position.
//     *
//     * @return a list valueOf Move objects representing all legal moves
//     */
//    public List<Move> getAllLegalMoves() {
//        List<Move> result = new ArrayList<>();
//        moveList.forEach(move -> result.add(Move.valueOf(move)));
//        return result;
//    }
//    /**
//     * return the current status valueOf the game.
//     *
//     * @return the GameStatus enum representing the game status
//     */
//    public GameStatus gameStatus(){
//        return gameStatus;
//    }
//
//    public void print() { position.printBoard(); }
//
//    /**
//     * update the current status valueOf the game.
//     * update the move list.
//     * should be called after the move made or unmade.
//     */
//    private void updateState() {
//        moveList = new MoveList(position);
//
//        for (int i = 0; i < Square.VALUES_COUNT; i++) // todo this is bug it keep adding new list for eace do/undo moves
//            legalMovesBySquare.add(new ArrayList<>());
//
//        for (int move : moveList)
//            legalMovesBySquare.get(Move.startSquare(move)).add(Move.valueOf(move));
//
//        gameStatus = moveList.size() == 0         ? !position.inCheck() ? GameStatus.DRAW_BY_STALEMATE :
//               sideToPlay() == Side.WHITE      ? GameStatus.BLACK_WON_BY_CHECKMATE : GameStatus.WHITE_WON_BY_CHECKMATE :
//               position.inInsufficientMaterial()  ? GameStatus.DRAW_BY_INSUFFICIENT_MATERIAL :
//               position.inThreeFoldRepetition()   ? GameStatus.DRAW_BY_REPETITION :
//               position.inRule50()                ? GameStatus.DRAW_BY_REACH_RULE_50 :
////               position.inCheck()                 ? GameStatus.CHECK                            :
//                                                    GameStatus.ONGOING;
//    }
//
//
//    /**
//     * Retrieves the piece on a specified square in the current position.
//     *
//     * @param s the square to check
//     * @return the Piece object representing the piece on the square
//     */
//    public Piece getPiece(Square s) {
//        return Piece.valueOf(position.getPiece(s.valueBy()));
//    }
//
//    /**
//     * Generates a FEN string for a Chess960 (Fischer Random Chess) starting position.
//     * The piece arrangement is shuffled with specific constraints for Chess960.
//     *
//     * @return a FEN string representing a Chess960 starting position
//     */
//    private static String createChess960Fen() {
//        Random r = new Random();
//        char[] fenPieces = new char[8];
//        char empty = '\0';
//
//        // 1: Place bishops on opposite-colored squares
//        fenPieces[2 * r.nextInt(0, 4)] = 'b'; // black square bishop
//        fenPieces[2 * r.nextInt(0, 4) + 1] = 'b'; // light square bishop
//
//        // 2: Shuffle all the rest valueOf the pieces
//        List<Character> pieces = new ArrayList<>(Arrays.asList('n', 'n', 'q', 'r', 'r', 'r'));
//        Collections.shuffle(pieces);
//
//        // 3: Place the shuffled pieces on the fenPieces array
//        int indPiece = 0, numRook = 0; // when the second rook appears, replace it with the king
//        for (int i = 0; i < fenPieces.length; i++) {
//            if (fenPieces[i] == empty) {
//                fenPieces[i] = (pieces.get(indPiece) == 'r' && numRook++ == 1) ? 'k' : pieces.get(indPiece);
//                indPiece++;
//            }
//        }
//
//        // Combine the pieces array into a string and format the Chess960 FEN
//        String fenPieceStr = new String(fenPieces);
//        return String.format("%s/pppppppp/8/8/8/8/PPPPPPPP/%s w KQkq - 0 1",
//                fenPieceStr, fenPieceStr.toUpperCase());
//    }
//
//    public boolean isPsoudoLegalMove(Move move) {
//        return allDestinations(getPiece(move.start()), move.start()).contains(move.dest());
//    }
//
//    public int pieceCount(Piece p){
//        return Long.bitCount(position.occupancyByPiece(p.ordinal()));
//    }
//
//    public int pieceCount(Side side, PieceType type){
//        return Long.bitCount(position.occupancyBySideAndType(side.valueBy(), type.valueBy()));
//    }
//
//    public String toSanLastMove(Move move) {
//        makeMove(move);
//        String result = toSanLastMove();
//        undoMove();
//        return result;
//
//    }
//
//
//    public String toSanLastMove() {
//        String result = "";
//        int move = position.lastMove();
//        int start = startSquare(move);
//        int dest = destSquare(move);
//        int moveType = Move.moveType(move);
//
//        int piece = position.getPiece(dest);
//        int pieceSide = pieceSide(piece);
//        int type = type(piece);
//
//        boolean isCapturing = position.getState().capturedPiece != NULL_PIECE || moveType == EN_PASSANT;
//
//        if (type == PAWN || moveType == PROMOTION) { // need the second condition because on promotion move the piece on the dest is not queen.
//            result = isCapturing ? File.getName(file(start)) + "x" : "";
//            result += getName(dest);
//            result += moveType == PROMOTION ? "=" + getName(Piece.valueOf(pieceSide, promotePT(move))) : "";
//        }
//
//        else if (moveType == CASTLING) {
//            result =  start < dest ? "O-O" : "O-O-O";
//        }
//
//        else {
//            result += getName(piece);
//
//            // handle ambiguity case
//            long occupancy = position.occupancy() ^ (squareToBB(start) | squareToBB(dest));
//            long ambiguitySamePieces = (position.attackersBB(dest, occupancy) & position.occupancyByPiece(piece));
//            if (ambiguitySamePieces != 0) {
//                if ((fileBB(file(start)) & ambiguitySamePieces) == 0) // there no same piece on same file (its mean there is the same piece on same rank so need to add the file letter to destinish between tow pieces
//                    result += getName(file(start));
//
//                else if ((rankBB(rank(start)) & ambiguitySamePieces) == 0) // there no same piece on same rank
//                    result += Rank.getName(rank(start));
//
//                else // there same piece on same file and on same rank
//                    result += getName(start);
//            }
//
//            /* todo check if its work like this:
//            if (ambiguitySamePieces != 0) {
//                if ((rankBB(rank(start)) & ambiguitySamePieces) != 0) // there is tow same piece on same rank
////                    result += getName(rank(start));
//                    result += getName(file(start));
//
//                if ((fileBB(file(start)) & ambiguitySamePieces) != 0) // there is tow same piece on same file
////                    result += getName(file(start));
//                    result += getName(rank(start));
//            }
//            */
//
//
//
//
//
//            // in case valueOf capture adding 'x'.
//            result += isCapturing ? "x" : "";
//            result += getName(dest);
//        }
//
//        // handle case valueOf check / checkmate
//        if (position.inCheck())
//            result += gameStatus.isCheckmate() ? "#" : "+"; // in checkmate game status will be either black_win or white_win
//
//        return result;
//    }
//
//
//}
