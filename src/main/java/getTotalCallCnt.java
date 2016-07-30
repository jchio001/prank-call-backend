import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetTotalCallCnt {
    public static void getTotalCallCnt(HttpServletRequest req, HttpServletResponse resp, Connection conn) throws IOException{
        try {
            String cntSQL = "select SUM(total) as total from (select SUM(account__total_cnt) as total from account UNION " +
                "select SUM(trial_call__total_cnt) as total from trial_call) totals";
            ResultSet rs = conn.createStatement().executeQuery(cntSQL);
            if (rs.next()) {
                long total = rs.getLong(Constants.TOTAL);
                JSONObject responseJSON = new JSONObject();
                responseJSON.put(Constants.TOTAL, total);
                resp.getWriter().print(responseJSON.toString());
            }
            else {
                throw new SQLException();
            }
        }
        catch (JSONException e) {
            resp.setStatus(Constants.BAD_REQUEST);
            resp.getWriter().print(Main.getStackTrace(e));
        }
        catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            resp.getWriter().print(Main.getStackTrace(e));
        }
    }
}
