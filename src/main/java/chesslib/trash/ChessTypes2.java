//package chesslib.types;
//
//import java.util.Objects;
//import chesslib.*;
//
//public class ChessTypes2 {
//
//    public enum PieceType {
//        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;
//
//        // Returns the integer valueBy valueOf this piece type, based on its ordinal.
//        public int valueBy() {
//            return chesslib.types.PieceType.Value.PAWN + ordinal();
//        }
//
//        // Returns the material valueBy valueOf this piece type, used in evaluation.
//        public int materialValue() {
//            return PieceType.materialValue(valueBy());
//        }
//    }
//
//    public enum Piece {
//        WHITE_PAWN(chesslib.types.Piece.Value.WHITE_PAWN),
//        WHITE_KNIGHT(chesslib.types.Piece.Value.WHITE_KNIGHT),
//        WHITE_BISHOP(chesslib.types.Piece.Value.WHITE_BISHOP),
//        WHITE_ROOK(chesslib.types.Piece.Value.WHITE_ROOK),
//        WHITE_QUEEN(chesslib.types.Piece.Value.WHITE_QUEEN),
//        WHITE_KING(chesslib.types.Piece.Value.WHITE_KING),
//
//        BLACK_PAWN(chesslib.types.Piece.Value.BLACK_PAWN),
//        BLACK_KNIGHT(chesslib.types.Piece.Value.BLACK_KNIGHT),
//        BLACK_BISHOP(chesslib.types.Piece.Value.BLACK_BISHOP),
//        BLACK_ROOK(chesslib.types.Piece.Value.BLACK_ROOK),
//        BLACK_QUEEN(chesslib.types.Piece.Value.BLACK_QUEEN),
//        BLACK_KING(chesslib.types.Piece.Value.BLACK_KING);
//
//        private final int pieceVal;
//
//        Piece(int pieceVal) {
//            this.pieceVal = pieceVal;
//        }
//
//        /**
//         * Retrieves the type valueOf the piece.
//         *
//         * @return The type valueOf the piece.
//         */
//        public PieceType type() {
//            return PieceType.getBy(type(pieceVal));
//        }
//
//        /**
//         * Retrieves the side valueOf the piece.
//         *
//         * @return The side valueOf the piece.
//         */
//        public Side side() {
//            return Side.getBy(side(pieceVal));
//        }
//
//
//        /**
//         * Retrieves the getName valueOf the piece.
//         *
//         * @return The getName valueOf the piece.
//         */
//        public char getName() {
//            return getName(pieceVal);
//        }
//
//        // Returns the integer valueBy associated with this piece.
//        public int valueBy() {
//            return pieceVal;
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//
//    public enum File{
//        FILE_A, FILE_B, FILE_C, FILE_D, FILE_E, FILE_F, FILE_G, FILE_H;
//
//        public char getName(){
//            return (char) ('a' + ordinal());
//        }
//
//        public static ChessTypes.File getBy(char getName){
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
//        public static ChessTypes.Rank getBy(char getName){
//            if (getName < '1' || getName > '8'){
//                return null;
//            }
//            int index = getName - '1';
//            return values()[index];
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
//
//        /**
//         * Retrieves the file valueOf the square.
//         */
//        public int file() {
//            return file(ordinal());
//        }
//
//        /**
//         * Retrieves the rank valueOf the square.
//         */
//        public int rank() {
//            return rank(ordinal());
//        }
//
//        // todo add explanation
//        public int valueBy() {
//            return ordinal();
//        }
//
//        /**
//         * @return true if is the dark square otherwise false.
//         */
//        public boolean isDarkSquare() {
//            return isDarkSquare(ordinal());
//        }
//
//        /**
//         * Retrieves the getName valueOf the square.
//         *
//         * @return The getName valueOf the square.
//         */
//        public String getName() {
//            return getName(ordinal());
//        }
//
//        public chesslib.types.Square flipped() {
//            return chesslib.types.Square.getBy(flipped(valueBy()));
//        }
//
//    }
//
//
//    public record Move(ChessTypes.Square start, ChessTypes.Square dest, ChessTypes.PieceType promotePT){
//
//        public Move(ChessTypes.Square start, ChessTypes.Square dest){
//            this(start, dest, null);
//        }
//
//        @Override
//        public boolean equals(Object other) {
//            return other instanceof ChessTypes.Move o && start == o.start && dest == o.dest && promotePT == o.promotePT;
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(start, dest, promotePT);
//        }
//    }
//
//
//
//}
