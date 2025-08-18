package chesslib.test;

import chesslib.FenValidation;

public class fenValidationTest {


    public static void runTest() {
        String[] validFens = {
                "bbnnqrkr/pppppppp/8/8/8/8/PPPPPPPP/BBNNQRKR w HFhf - 0 1",
                "bbnnrkqr/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKQR w HEhe - 0 1",
                "bbnnrkrq/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKRQ w GEge - 0 1",
                "bbnnrqkr/pppppppp/8/8/8/8/PPPPPPPP/BBNNRQKR w HEhe - 0 1",
                "bbnqnrkr/pppppppp/8/8/8/8/PPPPPPPP/BBNQNRKR w HFhf - 0 1",
                "bbnqrknr/pppppppp/8/8/8/8/PPPPPPPP/BBNQRKNR w HEhe - 0 1",
                "bbnqrkrn/pppppppp/8/8/8/8/PPPPPPPP/BBNQRKRN w GEge - 0 1",
                "bbnqrnkr/pppppppp/8/8/8/8/PPPPPPPP/BBNQRNKR w HEhe - 0 1",
                "bbnrknqr/pppppppp/8/8/8/8/PPPPPPPP/BBNRKNQR w HDhd - 0 1",
                "bbnrknrq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKNRQ w GDgd - 0 1",
                "bbnrkqnr/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQNR w HDhd - 0 1",
                "bbnrkqrn/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQRN w GDgd - 0 1",
                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w FDfd - 0 1",
                "bbnrkrqn/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRQN w FDfd - 0 1",
                "bbnrnkqr/pppppppp/8/8/8/8/PPPPPPPP/BBNRNKQR w HDhd - 0 1",

                "bbnnqrkr/p2ppppp/8/8/8/8/PPPPPPPP/BBNNQRKR w H - 0 1",
                "bbnnrkqr/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKQR w Ee - 0 1",
                "bbnnrkrq/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKRQ w k - 0 1",
                "bbnnrqkr/pp3ppp/8/8/8/8/PPPPPPPP/BBNNRQKR w Kk - 0 1",
                "bbnqnrkr/pppppppp/8/8/8/8/PPPPPPPP/BBNQNRKR w Kkq - 0 1",
                "bbnqrknr/pppppppp/8/8/8/8/PPPPPPPP/BBNQRKNR w Qkq - 0 1",
                "bbnqrkrn/pppppppp/8/8/8/8/PPPPPPPP/BBNQRKRN w GEge - 0 1",
                "bbnqrnkr/pppppppp/8/8/8/8/PPPPPPPP/BBNQRNKR w HEhe - 0 1",
                "bbnrknqr/pppppppp/8/8/8/8/PPPPPPPP/BBNRKNQR w HDhd - 0 1",
                "bbnrknrq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKNRQ w GDgd - 0 1",
                "bbnrkqnr/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQNR w HDhd - 0 1",
                "bbnrkqrn/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQRN w GDgd - 0 1",
                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w Fa - 0 1",
                "bbnrkrqn/pppppppp/8/8/8/8/PPPPP1PP/BBNRKRQN w d - 10 1",
                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w GQkb - 0 1",
                "       bbnrnkqr/pppppppp/8/8/8/8/PPPPPPPP/BBNRNKQR w HDhd - 0          1           ",


        };

        // now we update the FenValidation not to track the order
        String[] notValidFens = {
                "bbnnqrkr /pppppppp/8/8/8/8/PPPPPPPP/BBNNQRKR w HFhf - 0 1",
                "bbnnrkqr/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKQR w HEhe - 0 1k",
                "bbnnrkrq/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKRQ w GEgee - 0 1",
                "bbnnrqkr/pppppppp/8/8/8/8/PPPPPPPP/BBNNRQKR w KK - 0 1",
//                "bbnqnrkr/pppppppp/8/8/8/8/PPPPPPPP/BBNQNRKR w QK - 0 1", // now it will be a valid
//                "bbnqrknr/pppppppp/8/8/8/8/PPPPPPPP/BBNQRKNR w AH - 0 1", // now it will be a valid
                "bbnqrkrn/pppppppp/8/8/8/8/PPPPPPPP/BBNQRKRN w GEge e4 0 1",
                "bbnqrnkr/pppppppp/8/8/8/8/PPPPPPPP/BBNQRNKR w HEhe 0 1",
                "bbnrknqr/pppppppp/8/8/8/8/PPPPPPPP/BBNRKNQR w - 0 1",
                "bbnrknrq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKNRQ r GDgd - 0 1",
                "bbnrkqnr/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQN w HDhd - 0 1",
                "bbnrkqrn/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQR2 w GDgd - 0 1",
                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/ w FDfd - 0 1",
                "bbnrkrqn/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRQN w FDfd - 0 -1",
                "oajg9u490   94u4tg20   ut8g gu20og[paoes;i0",
                "bbnrqqrn/pppppppp/8/8/8/8/PPPPPPPP/BBNRKQRN w GDgd - 0 1",
                "bbnrkknq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w Fa - 0 1",
                "bbnr1rqn/pppppppp/8/8/8/8/PPPPP1PP/BBNR1RQN w d - 10 1",

                "bbnnrqkr/pp3ppp/8/8/8/8/PPPPPPPP/BNNRQKR w Kk - 0 1",
                "bbnnrqkr/pp3ppp/8/8/8/8/PPPPPPPP/BBNNRQKR  Kk - 0 1",
                "bbnnrqkr/pp3ppp/8/8/8/8/PPPPPPPP/BBNNRQKR w - 0 1",
                "bbnnrqkr/pp3ppp/8/8/8/8/PPPPPPPP/BBNNRQKR w Kk - 1",

                "bbnnrqkrpp3ppp/8/8/8/8/PPPPPPPP/BBNNRQKR w Kk - 0 1",

                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w FFAAa - 0 1",
                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w GQkbb - 0 1",
                "bbnrkrnq/pppppppp/8/8/8/8/PPPPPPPP/BBNRKRNQ w GGQ - 0 1",

        };


        FenValidation.isValidFenSyntaxAndKingCount("bbnnrkqr/pppppppp/8/8/8/8/PPPPPPPP/BBNNRKQR w Ee - 0 1");
        for (String fen : validFens) {
            if (!FenValidation.isValidFenSyntaxAndKingCount(fen)){
                throw new RuntimeException("Excepted: valid fen. Got not valid. Fen: " + fen);
            }
//            assert FenValidation.isValidFenSyntaxAndKingCount(fen) : fen;
        }

        for (String fen : notValidFens) {
            if (FenValidation.isValidFenSyntaxAndKingCount(fen)) {
                throw new RuntimeException("Excepted: NOT valid fen. Got valid Fen: " + fen);
            }
        }
//            assert !FenValidation.isValidFenSyntaxAndKingCount(fen, false) : fen;
    }
}
