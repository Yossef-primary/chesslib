package chesslib.move;

import chesslib.Position;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

public class MoveList implements Iterable<Integer>, MoveListInterface{
    private static final int MAX_MOVES = 218; // https://www.chess.com/forum/view/fun-with-chess/what-chess-position-has-the-most-number-of-possible-moves?page=2
    private  int size = 0;
    private final int[] moveList = new int[MAX_MOVES];

    public MoveList(Position position) {
        MoveGenerator.createAll(position, this);
    }
    public MoveList() {}

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Integer next() {
                return moveList[currentIndex++];
            }
        };
    }

    public int size(){
        return size;
    }

    public void add(int move){
        assert size < MAX_MOVES;
        moveList[size++] = move;
    }

}

