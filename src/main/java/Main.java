import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C3C3/9/RNBAKABNR b";
        var index = DPXQ.fenToIndex(fen);
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
            var humanMoves = DPXQ.moves(index);
            System.out.println("Position: " + fen);
            System.out.println("Number of possible moves: " + humanMoves.size());
            for (int i = 0; i < humanMoves.size(); i++) {
                var move = humanMoves.get(i);
                System.out.println((i + 1) + ". " + move);
            }

            System.out.println("Games");
            var games = DPXQ.games(index, 1);
            for (var game : games) {
                System.out.println(game);
            }
            var game = DPXQ.game(games.getFirst().id());
            System.out.println("First game text:");
            System.out.println(game);
        } catch (IOException e) {
            System.err.println("Error getting moves: " + e.getMessage());
        }
    }
}