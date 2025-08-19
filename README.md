# Java Chess Library ♟️
A lightweight Java library for chess move generation, FEN parsing, and position validation.

## Features
- Full legal move generation (supports Chess960)
- FEN parsing/validation
- Bitboard-based engine foundation
- Move parsing and validation in algebraic/SAN notation
- Detect game status: check, checkmate, stalemate, draw, repetition
- Position manipulation using int/long representation for fast operations


## Usage Example

    // Initialize a new chess game (standard starting position)
    GameManager gameManager = new GameManager();
    Make a move: E2 to E4
    
    Move move = new Move(Square.E2, Square.E4);
    System.out.println("Is move legal? " + gameManager.isLegalMove(move)); // Check legality
    gameManager.makeMove(move); // Apply move
    
    // Print the current board, FEN, and moves played
    gameManager.printPosition();
    /*
       Sample Output:
         +---+---+---+---+---+---+---+---+
       8 | r | n | b | q | k | b | n | r |
         +---+---+---+---+---+---+---+---+
       7 | p | p | p | p | p | p | p | p |
         +---+---+---+---+---+---+---+---+
       6 |   |   |   |   |   |   |   |   |
         +---+---+---+---+---+---+---+---+
       5 |   |   |   |   |   |   |   |   |
         +---+---+---+---+---+---+---+---+
       4 |   |   |   |   | P |   |   |   |
         +---+---+---+---+---+---+---+---+
       3 |   |   |   |   |   |   |   |   |
         +---+---+---+---+---+---+---+---+
       2 | P | P | P | P |   | P | P | P |
         +---+---+---+---+---+---+---+---+
       1 | R | N | B | Q | K | B | N | R |
         +---+---+---+---+---+---+---+---+
           a   b   c   d   e   f   g   h
       Fen:        rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1
       Moves played: [e2e4]
    */
    
    // Get all legal moves for current player
    for (Move m : gameManager.getAllLegalMoves()) {
       // Start square of the move (e.g., e2)
       System.out.println("Start: " + m.start());
    
       // Destination square of the move (e.g., e4)
       System.out.println("Destination: " + m.dest());
    
       // Promotion piece type if this move is a pawn promotion, otherwise null
       System.out.println("Promotion piece: " + m.promotePT());
    
       // Name of the move in human-readable format (like "e2e4" or "e7e8Q")
       System.out.println("Move name: " + m.getName());
    }
    
    // Get all legal moves from a specific square (E2)
    for (Move m : gameManager.getAllLegalMoves(Square.E2)) {
       // Can process or print each move
    }
    
    // Check the game status
    GameStatus gameStatus = gameManager.gameStatus();
    if (gameStatus == GameStatus.DRAW_BY_REPETITION) {
       System.out.println("Game is draw by repetition");
    }
    
    // Undo the last move
    gameManager.undoMove();
    
    // Retrieve the move history
    gameManager.moveHistory();
    }
    

    // FEN Example

    {
    String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";
    GameManager gameManager1 = new GameManager(fen); // Initialize with FEN
    gameManager1.setFen(fen); // Set FEN explicitly if needed
    
    System.out.println("Current FEN: " + gameManager1.getFen());
    }
    
    // SAN Moves Example

    {
    GameManager gameManager = new GameManager();
    
    // Parse SAN string "e4" to a Move object
    Move move = gameManager.parseSan("e4");
    System.out.println("Parsed SAN move equals E2->E4? " + move.equals(new Move(Square.E2, Square.E4)));
    
    // Make the parsed move
    gameManager.makeMove(move);
    
    // Convert Move object back to SAN notation
    Move blackMove = new Move(Square.E7, Square.E5);
    System.out.println("Black move SAN: " + gameManager.toSan(blackMove));
    }
    

    // Chess960 Example

    {
    // Create a random Chess960 starting position
    String chess960Fen = GameManager.createChess960Fen();
    GameManager gameManager = new GameManager(chess960Fen);
    System.out.println(gameManager.isChess960()); // print true
    


### Advanced Usage of the chess library for fast operations.

     String fenStartingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
     Position position = new Position(fenStartingPosition);

     // Make a move using the int representation of a move.
     // Move.create(fromSquare, toSquare, moveType) generates the internal integer encoding for the move.
     // Move types include NORMAL, PAWN_PUSH_TWICE, CASTLING, PROMOTION, etc.
     
     int move = Move.create(Square.Value.E2, Square.Value.E4, Move.PAWN_PUSH_TWICE);
     position.makeMove(move); // Apply the move to the position
    
     // Create and make a move for black
     // Here using a normal knight move from G8 to F6
     int blackMove = Move.create(Square.Value.G8, Square.Value.F6, Move.NORMAL);
     position.makeMove(blackMove); // Apply black's move

     // Get board occupancy as 64-bit bitboard
     long occupancyBitboard = position.occupancy();

     // Generate all legal moves
     MoveList moveList = new MoveList(position);
     for (int move1 : moveList) {
         // Process each move
         int startSquare = Move.start(move1);
         String moveName = Move.getName(move);
     }
     System.out.println("Number of legal moves: " + moveList.size()); // Output: Number of legal moves: 30
    
     if (moveList.size() == 0) {
         // No legal moves => checkmate or stalemate
     }
    
     // Fast check if there is any legal move (without generating all moves)
     boolean hasLegalMove = MoveGenerator.hasAnyLegalMove(position);
    
     // Count specific piece type
     int whitePawn = Piece.Value.WHITE_PAWN;
     System.out.println("Number of white pawns: " + position.pieceCount(whitePawn)); // Output: Number of white pawns: 8
    
    
     // Run perft test to count move sequences to a certain depth
     MoveGenerator.perft(new Position(fenStartingPosition), 6);
     /*
         Output (nodes per move and total nodes):
         a2a3: 4463267
         b2b3: 5310358
         c2c3: 5417640
         d2d3: 8073082
         e2e3: 9726018
         f2f3: 4404141
         g2g3: 5346260
         h2h3: 4463070
         a2a4: 5363555
         b2b4: 5293555
         c2c4: 5866666
         d2d4: 8879566
         e2e4: 9771632
         f2f4: 4890429
         g2g4: 5239875
         h2h4: 5385554
         b1a3: 4856835
         b1c3: 5708064
         g1f3: 5723523
         g1h3: 4877234
         Nodes count: 119060324
         Time: 1.329529833
      */
