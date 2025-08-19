/**
 * Represents file (column) constants and utilities for a chessboard.
 * Files are indexed from 0 (file 'a') to 7 (file 'h').
 */
package chesslib.types;

/**
 * Provides constants and utility methods for chessboard files (columns).
 */
public class File {
    public static final int VALUES_COUNT = 8;

    public static final int
            NULL_FILE = -1,
            FILE_A = 0,
            FILE_B = 1,
            FILE_C = 2,
            FILE_D = 3,
            FILE_E = 4,
            FILE_F = 5,
            FILE_G = 6,
            FILE_H = 7;

    /**
     * Returns the character representation of the given file (0–7 → 'a'–'h').
     *
     * @param file the file index (0–7)
     * @return the corresponding character ('a'–'h')
     */
    public static Character getName(int file){
        return isValid(file) ? (char) ('a' + file) : null;
    }

    /**
     * Checks whether the given file index is within valid bounds (0–7).
     *
     * @param file the file index
     * @return true if the file is valid, false otherwise
     */
    public static boolean isValid(int file){
        return (file & 7) == file;
    }

    public static boolean isValid(char charName){
        return charName >= 'a' && charName <= 'h';
    }

    /**
     * Converts a character ('a'–'h') to a file index (0–7).
     *
     * @param fileName the character representing the file
     * @return the file index (0–7)
     */
    public static int getBy(char fileName) {
        return isValid(fileName) ? fileName - 'a' : NULL_FILE;
    }
}
