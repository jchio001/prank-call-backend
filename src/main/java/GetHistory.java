import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

public class GetHistory {
    public static void getHistory(HttpServletRequest req, HttpServletResponse resp, Connection connection, String from,
                                  String to, String mode, long timestamp) throws IOException {
        try {
            String historySQL = "SELECT history__from, history__to, history__timestamp from history WHERE (history__from = ?" +
                (!to.equals("") ? "OR history__to = ?" : "") + ") AND history__timestamp " +
                (mode.equals(Constants.LOAD_MODE) ? "<" : ">") + " ?" +
                (mode.equals(Constants.LOAD_MODE) ? " LIMIT 10" : "");
            PreparedStatement stmt = connection.prepareStatement(historySQL);
            if (to.equals("")) {
                stmt.setString(1, from);
                stmt.setTimestamp(2, new Timestamp(timestamp));
            } else {
                stmt.setString(1, from);
                stmt.setString(2, to);
                stmt.setTimestamp(3, new Timestamp(timestamp));
            }
            ResultSet rs = stmt.executeQuery();

            JSONArray historyArr = new JSONArray();
            while (rs.next()) {
                JSONObject history = new JSONObject();
                String sender = rs.getString(Constants.HISTORY__FROM);
                String receiver = rs.getString(Constants.HISTORY__TO);

                history.put(Constants.HISTORY__FROM, (sender.equals(from) ?
                    sender : "anonymous"));
                history.put(Constants.HISTORY__TO, (receiver.equals(to) ?
                    receiver : "anonymous"));
                history.put(Constants.HISTORY__TIMESTAMP, rs.getTimestamp(Constants.HISTORY__TIMESTAMP));
                historyArr.put(history);
            }

            resp.getWriter().print(historyArr.toString());

        } catch (SQLException | JSONException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            resp.getWriter().print(Main.getStackTrace(e));
        }

    }
}
