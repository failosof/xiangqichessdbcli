import java.io.IOException;
import java.util.List;
import java.util.regex.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.*;

public class DPXQ {
    private static final String API_URL = "http://dpxq.com/hldcg/search/";
    private static final Pattern XQQB =Pattern.compile("([rnbakcp\\d]{1,9}/){9}[rnbakcp\\d]{1,9}( [rb])?", Pattern.CASE_INSENSITIVE);

    public record Move(String move, int games, int redWins, int blackWins, int draws) {
        @Override
        public String toString() {
            return String.format("Move: %s, Games: %d, Red: %d, Black: %d, Draw: %d%%",
                    move, games, redWins, blackWins, draws);
        }
    }

    public static String fenToIndex(String fen) {
        var m = XQQB.matcher(fen);
        if (!m.find()) return "";

        var p = m.group(0);
        p = p.replace("9", "111111111")
                .replace("8", "11111111")
                .replace("7", "1111111")
                .replace("6", "111111")
                .replace("5", "11111")
                .replace("4", "1111")
                .replace("3", "111")
                .replace("2", "11");
        p = p.length() > 99 ? p.substring(0, 99) : p;

        var b = "rnbakabnrccpppppRNBAKABNRCCPPPPP".split("");
        for (int i = 0; i < 32; i++) {
            var t = b[i];
            var index = p.indexOf(t);
            b[i] = new StringBuilder(String.format("%03d", 200 + index).substring(1)).reverse().toString();
            p = p.replaceFirst(t, "1");
        }

        var sb = new StringBuilder();
        for (var part: b) {
            sb.append(part);
        }

        return sb.substring(32, sb.length()) + sb.substring(0, 32);
    }

    public static List<Move> moves(String index) throws IOException {
        var url = API_URL + "search.asp?site=&owner=&e=&p=" + index + "tree&red=&black=&title=&date=&class=&event=&open=&result=&page=&order=";
        var response = HttpUtil.get(url);
        var doc = Jsoup.parse(response);
        var table = doc.getElementById("m_tree");
        var moves = new ArrayList<Move>();
        if (table == null) return moves;

        Elements rows = table.select("tbody > tr");
        for (Element row : rows) {
            var cols = row.select("td");
            if (cols.size() == 6) {
                var first = cols.getFirst();
                var last = cols.getLast();
                if (first.select("a").isEmpty() && last.select("a").isEmpty()) {
                    var move = toUCCI(first.text());
                    var games = Integer.parseInt(cols.get(1).text());
                    var redWins = Integer.parseInt(cols.get(2).text());
                    var blackWins = Integer.parseInt(cols.get(3).text());
                    var draws = Integer.parseInt(cols.get(4).text());
                    moves.add(new Move(move, games, redWins, blackWins, draws));
                }
            }
        }

        return moves;
    }

    public static String toUCCI(String code) {
        if (code == null || code.length() != 4) throw new IllegalArgumentException("Code must be 4 chars");
        int x1 = code.charAt(0) - '0';
        int y1 = code.charAt(1) - '0';
        int x2 = code.charAt(2) - '0';
        int y2 = code.charAt(3) - '0';
        char f1 = (char)('a' + x1);
        char r1 = (char)('0' + (9 - y1));
        char f2 = (char)('a' + x2);
        char r2 = (char)('0' + (9 - y2));
        return "" + f1 + r1 + f2 + r2;
    }
}
