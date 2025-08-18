package chesslib.move;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface MoveListInterface {
    @NotNull
    Iterator<Integer> iterator();

    int size();

    void add(int move);
}
