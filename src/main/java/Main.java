import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C3C3/9/RNBAKABNR b";
//        var fen = "rnbaka1nr/9/1c2b2c1/p1p1p1p1p/9/9/P1P1P1P1P/1C2B2C1/9/RN1AKABNR w";
        try {
            var computerMoves = ChessDB.moves(fen);
            System.out.println("Computer Moves");
            System.out.println("Position: " + fen);
            System.out.println("Number of possible moves: " + computerMoves.size());
            for (int i = 0; i < computerMoves.size(); i++) {
                var move = computerMoves.get(i);
                System.out.println((i + 1) + ". " + move);
            }

            System.out.println("Human Moves");
            var index = DPXQ.fenToIndex(fen);
            var humanMoves = DPXQ.moves(index);
            System.out.println("Position: " + fen);
            System.out.println("Number of possible moves: " + humanMoves.size());
            for (int i = 0; i < humanMoves.size(); i++) {
                var move = humanMoves.get(i);
                System.out.println((i + 1) + ". " + move);
            }
        } catch (IOException e) {
            System.err.println("Error getting moves: " + e.getMessage());
        }
    }
}