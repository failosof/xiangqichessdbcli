import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.regex.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;


public class DPXQ {
    private static final String API_URL = "http://dpxq.com/hldcg/search/";
    private static final String API_SEARCH_URL_TEMPLATE = API_URL + "search.asp?site=&owner=&e=&p=%s%s&red=&black=&title=&date=&class=&event=&open=&result=&page=%d&order=";
    private static final String API_VIEW_URL_TEMPLATE = API_URL + "view.asp?owner=m&id=%d";

    private static final Pattern XQQB = Pattern.compile("([rnbakcp\\d]{1,9}/){9}[rnbakcp\\d]{1,9}( [rb])?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DhtmlXQ_movelist = Pattern.compile("var\\s+DhtmlXQ_movelist\\s*=\\s*'\\[DhtmlXQ_movelist\\](.*?)\\[/DhtmlXQ_movelist\\]';");

    public record Move(String move, int games, int redWins, int blackWins, int draws) {
        @Override
        public String toString() {
            return String.format("Move: %s, Games: %d, Red: %d, Black: %d, Draw: %d",
                    move, games, redWins, blackWins, draws);
        }
    }

    public record GameInfo(int id, String name, int popularity) {
        @Override
        public String toString() {
            return String.format("%d: %s (%d)", id, name, popularity);
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
        var url = API_SEARCH_URL_TEMPLATE.formatted(index, "tree", 1);
        var response = HttpUtil.get(url);
        var doc = Jsoup.parse(response);
        var table = doc.getElementById("m_tree");
        var moves = new LinkedList<Move>();
        if (table == null) return moves;

        var rows = table.select("tbody > tr");
        for (var row : rows) {
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

    public static List<GameInfo> games(String index, int page) throws IOException {
        assert page > 0 : "Page must be greater than 0";

        var url = API_SEARCH_URL_TEMPLATE.formatted(index, "next", page);
        var response = HttpUtil.get(url);
        var doc = Jsoup.parse(response);
        var tables = doc.select("table");
        var infos = new LinkedList<GameInfo>();
        if (tables.size() < 2) return infos;

        var rows = tables.get(1).select("tr");
        for (var row : rows) {
            var cols = row.select("td");
            if (cols.size() == 3) {
                var first = cols.getFirst();
                var last = cols.getLast();
                if (!first.select("a").isEmpty() && !last.select("a").isEmpty()) {
                    var id = Integer.parseInt(first.text());
                    var name = cols.get(1).text();
                    var popularity = Integer.parseInt(cols.get(2).text());
                    infos.add(new GameInfo(id, name, popularity));
                }
            }
        }

        return infos;
    }

    public static String game(int id) throws IOException {
        var url = API_VIEW_URL_TEMPLATE.formatted(id);
        var response = HttpUtil.get(url);
        var doc = Jsoup.parse(response);
        var moveList = findMoveList(doc);
        var game = doc.getElementById("dhtmlxq_view");
        if (game == null) return "";
        return setMoveList(game.text(), moveList);
    }

    private static String findMoveList(Document doc) {
        var scripts = doc.select("script");
        for (var script : scripts) {
            var matcher = DhtmlXQ_movelist.matcher(script.html());
            if (matcher.find()) return matcher.group(1);
        }
        return "";
    }

    private static String setMoveList(String game, String moveList) {
        var escaped = Pattern.quote("[DhtmlXQ_movelist]") + ".*?" + Pattern.quote("[/DhtmlXQ_movelist]");
        return game.replaceAll(escaped,
                Matcher.quoteReplacement("[DhtmlXQ_movelist]" + moveList + "[/DhtmlXQ_movelist]"));
    }

    private static String toUCCI(String code) {
        assert code.length() == 4 : "Code must be 4 chars";
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
