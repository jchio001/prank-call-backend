import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetHistory {
    public static void getHistory(HttpServletRequest req, HttpServletResponse resp, Connection connection, String from, String to)
        throws IOException {
        try {
            String historySQL = "SELECT history__from, history__to, history__timestamp from history WHERE history__from = ?" +
                (!to.equals("") ? "OR history__to = ?" : "") + "ORDER BY history__timestamp ASC";
            PreparedStatement stmt = connection.prepareStatement(historySQL);
            if (to.equals("")) {
                stmt.setString(1, from);
            } else {
                stmt.setString(1, from);
                stmt.setString(2, to);
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
