import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var chessDB = new ChessDB();
        var fen = "rnbaka1nr/9/1c2b2c1/p1p1p1p1p/9/9/P1P1P1P1P/1C2B2C1/9/RN1AKABNR w";
        try {
            var moves = chessDB.moves(fen);
            System.out.println("Position: " + fen);
            System.out.println("Number of possible moves: " + moves.size());
            System.out.println("Top 10 moves:");
            for (int i = 0; i < Math.min(10, moves.size()); i++) {
                var move = moves.get(i);
                System.out.println((i + 1) + ". " + move);
            }
        } catch (IOException e) {
            System.err.println("Error getting moves: " + e.getMessage());
        }
    }
}