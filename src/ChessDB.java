import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ChessDB {
    private static final String API_URL = "http://api.chessdb.cn:81";

    public static class Move {
        public final String move;
        public final int score;
        public final int rank;
        public final String note;
        public final double winRate;
        
        private Move(String move, int score, int rank, String note, double winrate) {
            this.move = move;
            this.score = score;
            this.rank = rank;
            this.note = note;
            this.winRate = winrate;
        }

        public static Move from(String str) {
            String move = null;
            int score = 0;
            int rank = 0;
            String note = "";
            double winrate = 0.0;

            var parts = str.split(",");
            for (String part : parts) {
                var keyValue = part.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    switch (key) {
                        case "move":
                            if (value.isEmpty()) {
                                throw new RuntimeException("Error parsing move: its empty");
                            }
                            move = value;
                            break;
                        case "score":
                            try {
                                score = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Error parsing score: " + value);
                            }
                            break;
                        case "rank":
                            try {
                                rank = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Error parsing rank: " + value);
                            }
                            break;
                        case "note":
                            note = value;
                            break;
                        case "winrate":
                            try {
                                winrate = Double.parseDouble(value);
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Error parsing winrate: " + value);
                            }
                            break;
                    }
                }
            }

            return new Move(move, score, rank, note, winrate);
        }

        @Override
        public String toString() {
            return String.format("Move: %s, Score: %d, Rank: %d, Note: %s, Win rate: %.2f%%",
                               move, score, rank, note, winRate);
        }
    }

    public List<Move> moves(String fen) throws IOException {
        var fenEncoded = URLEncoder.encode(fen, StandardCharsets.UTF_8);
        var url = new URL(API_URL + "/chessdb.php?action=queryall&learn=1&showall=1&board=" + fenEncoded);
        var response = get(url);
        return Arrays.stream(response.split("\\|"))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(Move::from)
                .toList();
    }

    private String get(URL url) throws IOException {
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        var response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            connection.disconnect();
        }

        return response.toString();
    }
}