package chesslib;

import chesslib.types.Square;

import java.util.regex.Pattern;
/**
todo give a short doc

 */
public class FenValidation {
    private static final Pattern FEN_CASTLING_PATTERN = Pattern.compile("^(?:-|(?!.*(.).*\\1)[KQkqA-Ha-h]{1,4})$"); // this prevent duplicate char inside its not work inside the fen reges only if it stand alone
    private static final Pattern FEN_PATTERN = Pattern.compile(
            "([rkqnbpRKQNBP1-8]+/){7}[rkqnbpRKQNBP1-8]+ " // this version support pawns on first and last rank
            // catch if the color is legal.
            + "[wb] "
            // catch the case the FEN is Shredder-FEN. x-fen or regular fen. not check the order of chars
            + "(-|[KQkqA-Ha-h]{1,4}) " // for now read duplicate char in castling fen and letter prevent this on FEN_CASTLING_PATTERN
            // catch if the en passant square is valid.
            + "(-|[a-h][36]) "
            // catch if the num half-move is valid.
            + "\\d+ "
            // catch if the num moves are valid.
            + "\\d+"
    );


    /**
     * Checks whether a FEN string has valid syntax and correct king count.
     * This includes:
     *  - Valid FEN format (piece placement, castling, en passant, half-move and full-move numbers)
     *  - Each rank contains exactly 8 squares (pieces + empty squares)
     *  - Exactly one white king and one black king
     *
     * @param fen the FEN string to validate
     * @return true if the FEN passes the validation, false otherwise
     */
//    public static boolean isValidFenSyntaxAndKingCount(String fen) {
//        return isValidFen(fen, false);
//    }
//
//    /**
//     * Checks whether a FEN string has valid syntax and correct king count.
//     * This includes:
//     *  - Valid FEN format (piece placement, castling, en passant, half-move and full-move numbers)
//     *  - Each rank contains exactly 8 squares (pieces + empty squares)
//     *
//     * @param fen the FEN string to validate
//     * @return true if the FEN passes the validation, false otherwise
//     */
//    public static boolean isValidFenSyntax(String fen) {
//        return isValidFen(fen, true);
//    }

    /**
     * Checks whether a FEN string has valid syntax and correct king count.
     * This includes:
     *  - Valid FEN format (piece placement, castling, en passant, half-move and full-move numbers)
     *  - Each rank contains exactly 8 squares (pieces + empty squares)
     *  - Exactly one white king and one black king
     *
     * @param fen the FEN string to validate
     * @return true if the FEN passes the validation, false otherwise
     */
    public static boolean isValidFenSyntax(String fen) {
        fen = fen.trim().replaceAll("\\s+", " "); // Remove all extra spaces for consistency.
        if (!FEN_PATTERN.matcher(fen).matches()) // Validate the FEN string using the defined pattern.
            return false;
        var fenParts = fen.split(" ");
        String boardFen = fenParts[0];
        String castlingFen = fenParts[2];

        // castling fen need second check to prevent duplicate chars inside
        if (!FEN_CASTLING_PATTERN.matcher(castlingFen).matches()){
            return false;
        }
        // Validate the pieces' placement (at most 8 in every rank) and ensure that there is only
        // one king for each side.
        int countPiecesInLine = 0, countWhiteKing = 0, countBlackKing = 0;
        char lastChar = '/'; // Initialize lastChar to indicate that the last character is not a digit.

        for (char c : boardFen.toCharArray()) {
            if (c == '/') {
                if (countPiecesInLine != Square.BOARD_DIM)
                    return false; // Each rank must have exactly 8 squares.
                countPiecesInLine = 0;
            }
            else if ((Character.isDigit(c))) {
               if (Character.isDigit(lastChar)){
                   return false; // A digit can't follow another digit.
               }
               countPiecesInLine += Character.getNumericValue(c);
            }
            else {
                countPiecesInLine++;
            }
            lastChar = c;
        }

        // Ensure that the last rank also has exactly 8 squares.
        return countPiecesInLine == Square.BOARD_DIM;
    }
}
