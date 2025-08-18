package chesslib.exeptions;

abstract public class ChessException extends RuntimeException{
    public ChessException(String massage){
        super(massage);
    }
}
