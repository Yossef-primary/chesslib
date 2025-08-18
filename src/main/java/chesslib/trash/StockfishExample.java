package chesslib.trash;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class StockfishExample {
    public static void main(String[] args) {
        try {
            // Start Stockfish process
            ProcessBuilder processBuilder = new ProcessBuilder("bin/stockfish");
            processBuilder.redirectErrorStream(true);
            Process stockfishProcess = processBuilder.start();

            // Open input and output streams for communication with Stockfish
            OutputStream stockfishOutputStream = stockfishProcess.getOutputStream();
            BufferedReader stockfishInputStream = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));

            // Send UCI command to Stockfish
//            sendCommand(stockfishOutputStream, "uci");

            // Wait for the engine to respond
//            waitForResponse(stockfishInputStream);

            // Send a position command (example: starting position)
            sendCommand(stockfishOutputStream, "position startpos");

            // Send a go command (example: search for 1000 milliseconds)
            sendCommand(stockfishOutputStream, "go perft 3");

//            waitForResponse(stockfishInputStream);
            System.out.println(perftResult(stockfishInputStream));

            // Wait for the engine to respond
//            waitForResponse(stockfishInputStream);
//            sendCommand(stockfishOutputStream, "go perft 5");
//            waitForResponse(stockfishInputStream);

            // Close resources
            stockfishInputStream.close();
            stockfishOutputStream.close();
            stockfishProcess.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendCommand(OutputStream outputStream, String command) throws IOException {
        String fullCommand = command + "\n";
        outputStream.write(fullCommand.getBytes());
        outputStream.flush();
    }

    private static String perftResult(BufferedReader inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
//        Map<String, Integer>
        String line;
        while (!(line = inputStream.readLine()).startsWith("Nodes"))
            response.append(line).append("\n");
        return response.toString();
    }

    private static void waitForResponse(BufferedReader inputStream) throws IOException {
        StringBuilder response = new StringBuilder();
        int character;
        long startTime = System.currentTimeMillis();
        long timeout = 10000; // 10 seconds timeout

        while ((character = inputStream.read()) != -1) {
            char c = (char) character;
            response.append(c);

            // Print the received character if needed
            System.out.print(c);

            // Check for conditions based on the expected responses from Stockfish
            if (response.toString().endsWith("bestmove")) {
                System.out.println("Received bestmove. Exiting loop.");
                break;
            }

            // Check for timeout
            if (System.currentTimeMillis() - startTime > timeout) {
                System.out.println("Timeout reached. Exiting loop.");
                break;
            }
        }
    }


}

 class StockfishExample2 {
    public static void main(String[] args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bin/stockfish");
            processBuilder.redirectErrorStream(true);
            Process stockfishProcess = processBuilder.start();

            BufferedReader stockfishInputStream = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            OutputStream stockfishOutputStream = stockfishProcess.getOutputStream();

            // Start a separate thread to read Stockfish's output
            Thread outputReaderThread = new Thread(() -> {
                try {
                    readStockfishOutput(stockfishInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputReaderThread.start();

            // Send UCI commands
            sendCommand(stockfishOutputStream, "uci");
            sendCommand(stockfishOutputStream, "position startpos");
            sendCommand(stockfishOutputStream, "go perft 5" );

            // Wait for the output reader thread to finish
            outputReaderThread.join();

            // Close resources
            stockfishInputStream.close();
            stockfishOutputStream.close();
            stockfishProcess.destroy();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendCommand(OutputStream outputStream, String command) throws IOException {
        String fullCommand = command + "\n";
        System.out.println("Sent: " + fullCommand);
        outputStream.write(fullCommand.getBytes());
        outputStream.flush();
    }

    private static void readStockfishOutput(BufferedReader inputStream) throws IOException {
        String line;
        while ((line = inputStream.readLine()) != null) {
            System.out.println("Received: " + line);

            // Add conditions based on the expected responses from Stockfish
            if (line.startsWith("bestmove")) {
                System.out.println("Received bestmove. Exiting loop.");
                break;
            }
        }
    }
}
