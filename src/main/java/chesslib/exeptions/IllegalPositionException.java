package chesslib.exeptions;

public class IllegalPositionException extends ChessException {

    public IllegalPositionException(String massage){
        super(massage);
        // give the user infow whats wrong
        // it could be invalid fen syntax, wrong kings amount, invalid pawn placemant
    }
}
