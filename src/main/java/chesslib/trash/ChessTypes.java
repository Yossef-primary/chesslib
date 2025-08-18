//package chesslib.types;
//
//
//import java.util.*;
//
//public class ChessTypes {
//    public enum Piece{
//        WHITE_PAWN  (chesslib.types.Piece.WHITE_PAWN),
//        WHITE_KNIGHT(chesslib.types.Piece.Value.WHITE_KNIGHT),
//        WHITE_BISHOP(chesslib.types.Piece.Value.WHITE_BISHOP),
//        WHITE_ROOK  (chesslib.types.Piece.Value.WHITE_ROOK),
//        WHITE_QUEEN (chesslib.types.Piece.Value.WHITE_QUEEN),
//        WHITE_KING  (chesslib.types.Piece.Value.WHITE_KING),
//
//        BLACK_PAWN  (chesslib.types.Piece.Value.BLACK_PAWN),
//        BLACK_KNIGHT(chesslib.types.Piece.Value.BLACK_KNIGHT),
//        BLACK_BISHOP(chesslib.types.Piece.Value.BLACK_BISHOP),
//        BLACK_ROOK  (chesslib.types.Piece.Value.BLACK_ROOK),
//        BLACK_QUEEN (chesslib.types.Piece.Value.BLACK_QUEEN),
//        BLACK_KING  (chesslib.types.Piece.Value.BLACK_KING);
//
//        private final int pieceValue;
//
//
//        Piece(int pieceVal) {
//            this.pieceValue = pieceVal;
//        }
//
//        /**
//         * Retrieves the type valueOf the piece.
//         *
//         * @return The type valueOf the piece.
//         */
//        public PieceType type() {
//            return PieceType.getBy(type(pieceValue));
//        }
//
//        /**
//         * Retrieves the side valueOf the piece.
//         *
//         * @return The side valueOf the piece.
//         */
//        public chesslib.types.Side side() {
//            return chesslib.types.Side.getBy(side(pieceValue));
//        }
//
//
//        // Returns the integer valueBy associated with this piece.
//        public int valueBy(){
//            return pieceValue;
//        }
//
//        public static Piece getBy(int pieceValue){
//            return ma
//        }
//
//        /**
//         * Retrieves the getName valueOf the piece.
//         *
//         * @return The getName valueOf the piece.
//         */
//        public char getName() {
//            return getName(pieceValue);
//        }
///*        WHITE_PAWN  (WHITE, PAWN),
//        WHITE_KNIGHT(WHITE, KNIGHT),
//        WHITE_BISHOP(WHITE, BISHOP),
//        WHITE_ROOK  (WHITE, ROOK),
//        WHITE_QUEEN (WHITE, QUEEN),
//        WHITE_KING  (WHITE, KING),
//
//        BLACK_PAWN  (BLACK, PAWN),
//        BLACK_KNIGHT(BLACK, KNIGHT),
//        BLACK_BISHOP(BLACK, BISHOP),
//        BLACK_ROOK  (BLACK, ROOK),
//        BLACK_QUEEN (BLACK, QUEEN),
//        BLACK_KING  (BLACK, KING);
//
//        private final Side side;
//        private final PieceType pieceType;
//
//        Piece(Side side, PieceType pieceType){
//            this.side = side;
//            this.pieceType = pieceType;
//        }
//
//        public Side getSide(){
//            return side;
//        }
//
//        public PieceType getType(){
//            return pieceType;
//        }
//
//        public char getName(){
//            return "PNBRQKpnbrqk".charAt(ordinal());
//        }*/
//
//    }
//
//    public enum PieceType{
//        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;
//
//        public int valueBy(){
//            return chesslib.types.PieceType.Value.PAWN - ordinal();
//        }
//
//        public char getName(){
//            return "pnbrqk".charAt(ordinal());
//        }
//
//        public char getSymbol(boolean isFilled){
//            return isFilled ?"♟♞♝♜♛♚".charAt(ordinal()) : "♙♘♗♖♕♔".charAt(ordinal());
//        }
//
//        public PieceType getBy(char getName){
//            return getName == 'w' ? WHITE : getName == 'b' ? BLACK : null;
//        }
//
//        // for internal uses
//        public PieceType getBy(int pieceTypeValue){
//            return getName == 'w' ? WHITE : getName == 'b' ? BLACK : null;
//        }
//
//    }
//
//    public enum Side{
//        WHITE, BLACK;
//
//        public char getName(){
//            return this == WHITE ? 'w' : 'b';
//        }
//
//        public Side getBy(char getName){
//            return getName == 'w' ? WHITE : getName == 'b' ? BLACK : null;
//        }
//
//        public int valueBy(){
//            return ordinal();
//        }
//
//        public Side getBy(int sideValue){
//            return chesslib.types.Side.isValid(sideValue) ? values()[sideValue] : null;
//        }
//    }
//
//    public enum Square {
//        A1, B1, C1, D1, E1, F1, G1, H1,
//        A2, B2, C2, D2, E2, F2, G2, H2,
//        A3, B3, C3, D3, E3, F3, G3, H3,
//        A4, B4, C4, D4, E4, F4, G4, H4,
//        A5, B5, C5, D5, E5, F5, G5, H5,
//        A6, B6, C6, D6, E6, F6, G6, H6,
//        A7, B7, C7, D7, E7, F7, G7, H7,
//        A8, B8, C8, D8, E8, F8, G8, H8;
//
//        public String getName(){
//            return getName().toLowerCase();
//        }
//
//        public Rank getRank(){
//            return Rank.getBy(getName().charAt(0));
//        }
//
//        public File getFile(){
//            return File.getBy(getName().charAt(0));
//        }
//
//        public boolean isDarkSquare(){
//            return getFile().ordinal() + getRank().ordinal() % 2 == 0;
//        }
//
//        // for internal uses
//        public int valueBy(){
//            return ordinal();
//        }
//
//        public static Square getBy(String getName){
//            // validate the getName
//            if (getName.length() != 2){
//                return null;
//            }
//
//            EnumSet<Square> map = EnumSet.allOf(Square.class);
//
//            File file = File.getBy(getName.charAt(0));
//            Rank rank = Rank.getBy(getName.charAt(1));
//            return getBy(file, rank);
//        }
//
//        public static Square getBy(File file, Rank rank){
//            if (file == null || rank == null)
//                return null;
//            int index = rank.ordinal() * Rank.values().length + file.ordinal();
//            return values()[index];
//        }
//
//        // for internal uses
//        public static Square getBy(int valueBy){
//            return valueBy < 0 || valueBy > values().length ? null : values()[valueBy];
//        }
//    }
//
//    public enum File{
//        FILE_A, FILE_B, FILE_C, FILE_D, FILE_E, FILE_F, FILE_G, FILE_H;
//
//        public char getName(){
//            return (char) ('a' + ordinal());
//        }
//
//        public static File getBy(char getName){
//            if (getName < 'a' || getName > 'h'){
//                return null;
//            }
//            assert true; // verify the getName
//            int index = getName - 'a';
//            return values()[index];
//        }
//    }
//
//    public enum Rank{
//        RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8;
//
//        public char getName(){
//            return (char) ('1' + ordinal());
//        }
//
//        public static Rank getBy(char getName){
//            if (getName < '1' || getName > '8'){
//                return null;
//            }
//            int index = getName - '1';
//            return values()[index];
//        }
//    }
//
//    public record Move(Square start, Square dest, PieceType promotePT){
//
//        public Move(Square start, Square dest){
//            this(start, dest, null);
//        }
//
//        @Override
//        public boolean equals(Object other) {
//            return other instanceof Move o && start == o.start && dest == o.dest && promotePT == o.promotePT;
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(start, dest, promotePT);
//        }
//    }
//}
