package chesslib.test;

import chesslib.Position;
import chesslib.move.Move;
import chesslib.move.MoveGenerator;
import chesslib.move.MoveList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;


public class PerftDebugger {
    /**
     * Entry point: start debugging at a given depth
     */
    private static void debug(String rootFen, boolean isChess960, List<String> uciMovedPlayed, int depth) throws IOException {
        if (depth < 0) {
            return;
        }

        // 1) Get Stockfish’s per-move counts
        Map<String, Long> expected = getStockfishPerMoveCounts(rootFen, uciMovedPlayed, depth, isChess960);

        // 2) Get your engine’s per-move counts
        Map<String, Long> actual = getLocalPerMoveCounts(rootFen, uciMovedPlayed, depth);


        // 3) Compare
        for (Map.Entry<String, Long> e : expected.entrySet()) {
            String mv = e.getKey();
            long expCount = e.getValue();
            long actCount = actual.getOrDefault(mv, 0L);


            if (expCount != actCount) {
                if (depth > 1) {
                    // Drill into the bad branch
                    List<String> newList = new ArrayList<>(uciMovedPlayed);
                    newList.add(mv);

                    debug(rootFen, isChess960,newList, depth - 1);
                }

                // we don't found the move that cuse to the problem
                Position pos = load(rootFen, uciMovedPlayed);
                System.err.printf(pos.posString());
                System.err.printf(
                        "Mismatch on fen: %s\n on move %s: expected %,d but got %,d - depth: %d%n",
                        pos.getFen(), mv, expCount, actCount, depth
                );
                throw new RuntimeException();
            }

        }

    }

    /**
     * Extracts the Stockfish binary from the application's bundled resources and writes it to a
     * temporary executable file on the filesystem.
     *
     * <p>Why is this needed?</p>
     * <ul>
     *   <li>Java cannot directly execute a binary stored inside a JAR (resources are streams, not real files).</li>
     *   <li>To run Stockfish with {@link ProcessBuilder}, the engine must exist as an actual file on disk.</li>
     *   <li>This method copies the resource binary into a temporary file, marks it executable,
     *       and returns a reference to that file.</li>
     *   <li>The temporary file is deleted automatically on JVM exit.</li>
     * </ul>
     *
     * @return a {@link File} pointing to the extracted and executable Stockfish binary
     * @throws IOException if the resource cannot be found or copied
     */
    private static File extractStockfish() throws IOException {
        try (InputStream in = PerftDebugger.class.getResourceAsStream("/bin/stockfish")) {
            if (in == null) {
                throw new IOException("Stockfish binary not found in resources.");
            }

            // Create temp file with proper suffix for Windows
            File tempFile = File.createTempFile("stockfish", System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : "");
            tempFile.deleteOnExit();

            // Copy resource into temp file
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.setExecutable(true);
            return tempFile;
        }
    }

    private static Map<String, Long> getStockfishPerMoveCounts(String fen, List<String> uciMovesPlayed, int depth, boolean isChess960)
            throws IOException {

        File stockfishBin = extractStockfish();
        ProcessBuilder pb = new ProcessBuilder(stockfishBin.getAbsolutePath());
        Process p = pb.start();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

            // init Stockfish
            writer.write("uci\n");
            writer.write("setoption name UCI_Chess960 value " + (isChess960 ? "true" : "false") + "\n");
            writer.write("isready\n");
            writer.flush();

            // wait for "readyok"
            String line;
            while ((line = reader.readLine()) != null) {
                if ("readyok".equals(line)) break;
            }

            // position & perft
            writer.write("position fen " + fen);
            if (!uciMovesPlayed.isEmpty()) {
                writer.write(" moves " + String.join(" ", uciMovesPlayed));
            }
            writer.write("\n");
            writer.write("go perft " + depth + "\n");
            writer.write("quit\n");
            writer.flush();

            // parse results
            Map<String, Long> counts = new LinkedHashMap<>();
            while ((line = reader.readLine()) != null) {
                if (line.contains(":")) {
                    String[] parts = line.split(":");
                    String move = parts[0].trim();
                    if (move.matches("^[a-h][1-8][a-h][1-8][nbrq]?$")) {
                        counts.put(move, Long.parseLong(parts[1].trim()));
                    }
                }
            }
            return counts;
        }
    }


    private static Position load(String fen, List<String> uciMovesPlayed){
        Position position = new Position(fen);
        for (String uciMove: uciMovesPlayed){
            position.makeMove(Move.fromUci(uciMove, position));
        }
        return position;
    }

    /**
     * Uses your MoveGenerator to compute the same division at depth
     */
    private static Map<String, Long> getLocalPerMoveCounts(String fen, List<String> uciMovesPlayed, int depth) {
        Map<String, Long> counts = new LinkedHashMap<>();
        Position pos = load(fen, uciMovesPlayed);
        MoveList moveList = new MoveList(pos);
        for (int move : moveList) {
                pos.makeMove(move);
                long nodes = MoveGenerator.numMoves(pos, depth - 1);
                pos.undoMove();
                counts.put(Move.toUci(move, pos), nodes);
//            }
        }
        return counts;

    }


    public static void debug(String fen, int depth) {
        assert depth > 0;
        System.out.printf("Debug perft at depth %d, FEN=%s%n", depth, fen);
        try {
            boolean isCess960 = new Position(fen).hasChess960CastlingAvailable();
            new Position(fen).hasChess960CastlingAvailable();
            debug(fen, isCess960,new ArrayList<>(), depth);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("passed the test!!");
    }
}



/*
//    private static Map<String, Long> getStockfishPerMoveCounts4(String fen, List<String> uciMovesPlayed, int depth, boolean isChess960)
//            throws IOException {
//
//        // Extract stockfish binary from resources
//        InputStream in = PerftDebugger.class.getResourceAsStream("/bin/stockfish");
//        if (in == null) {
//            throw new IOException("Stockfish binary not found in resources.");
//        }
//
//        // Create a temporary file to store the extracted binary
//        File tempFile = File.createTempFile("stockfish", null);
//        tempFile.setExecutable(true);
//
//        // Copy the binary data from the InputStream to the temporary file
//        try (OutputStream out = new FileOutputStream(tempFile)) {
//            byte[] buffer = new byte[8192];
//            int bytesRead;
//            while ((bytesRead = in.read(buffer)) != -1) {
//                out.write(buffer, 0, bytesRead);
//            }
//        }
//
//        // Execute the extracted binary
//        ProcessBuilder pb = new ProcessBuilder(tempFile.getAbsolutePath());
//        Process p = pb.start();
//        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
//
//            // Send commands to Stockfish
//            writer.write("uci\n");
//            writer.write("setoption name UCI_Chess960 value " + (isChess960 ? "true" : "false") + "\n");
//            writer.write("isready\n");
//            writer.flush();
//
//            // Wait for 'readyok'
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if ("readyok".equals(line)) {
//                    break;
//                }
//            }
//
//            // Set position and perft depth
//            writer.write("position fen " + fen);
//            if (!uciMovesPlayed.isEmpty()) {
//                writer.write(" moves " + String.join(" ", uciMovesPlayed));
//            }
//            writer.write("\n");
//            writer.write("go perft " + depth + "\n");
//            writer.write("quit\n");
//            writer.flush();
//
//            // Parse the output
//            Map<String, Long> counts = new LinkedHashMap<>();
//            while ((line = reader.readLine()) != null) {
//                if (line.contains(":")) {
//                    String[] parts = line.split(":");
//                    String move = parts[0].trim();
//                    if (move.matches("^[a-h][1-8][a-h][1-8][nbrq]?$")) {
//                        long count = Long.parseLong(parts[1].trim());
//                        counts.put(move, count);
//                    }
//                }
//            }
//            return counts;
//        } finally {
//            // Clean up the temporary file
//            tempFile.delete();
//        }
//    }
//
//    /**
//     * Spawns Stockfish, sends “position” + “go perft”, and parses the move counts
//     */
//private static Map<String, Long> getStockfishPerMoveCounts2(String fen, List<String> uciMovesPlayed, int depth, boolean isChess960)
//        throws IOException {
//    // todo modify the code to support the chess960 by passing arg ischess960
//
//
//    ProcessBuilder pb = new ProcessBuilder(PerftDebugger.class.getResource("src/main/java/chesslib/test/bin/stockfish").getPath());
//    Process p = pb.start();
//    BufferedWriter in = new BufferedWriter(
//            new OutputStreamWriter(p.getOutputStream())
//    );
//    BufferedReader out = new BufferedReader(
//            new InputStreamReader(p.getInputStream())
//    );
//
//    String movesPlay = String.join(" ", uciMovesPlayed).trim();
//
//    // Tell Stockfish we speak UCI and (optionally) enable Chess960 BEFORE setting the position
//    in.write("uci\n");
//    in.write("setoption getName UCI_Chess960 value " + (isChess960 ? "true" : "false") + "\n");
//    in.write("isready\n");
//    in.flush();
//
//    // (Optional but robust) Wait for 'readyok' so options are applied before 'position'
//    String line;
//    while ((line = out.readLine()) != null) {
//        if ("readyok".equals(line)) break;
//    }
//
//    // Tell Stockfish our position and perft depth
//    in.write("position fen " + fen);
//    if (!movesPlay.isEmpty()) {
//        in.write(" moves " + movesPlay);
//    }
//    in.write("\n");
//
//    in.write("go perft " + depth + "\n");
//    in.write("quit\n");
//    in.flush();
//
//    Map<String, Long> counts = new LinkedHashMap<>();
//    // lines like “e2e4: 20” or “a7a8q: 1”
//    // keep your original idea, but accept optional promotion at the end
//    while ((line = out.readLine()) != null) {
//        // Assumes Stockfish perft prints one move per line as "uciMove: number"
//        // Examples: "e2e4: 20", "a7a8q: 1"
//        if (line.indexOf(':') > 0) {
//            String[] parts = line.split(":");
//            String move = parts[0].trim();
//            // quick UCI move shape check: from(2) + to(2) + optional promo(1)
//            if (move.matches("^[a-h][1-8][a-h][1-8][nbrq]?$")) {
//                long cnt = Long.parseLong(parts[1].trim());
//                counts.put(move, cnt);
//            }
//        }
//    }
//    return counts;
//}
//
// */