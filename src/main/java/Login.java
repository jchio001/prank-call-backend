import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {
    public static void login(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
        throws IOException {
        try {
            String number = jsonObject.getString(Constants.PHONE_NUMBER);
            String password = jsonObject.getString(Constants.PASSWORD);
            String loginQuery = "Select account__id, account__active FROM account WHERE account__phone_number = ? and " +
                "account__password = ? LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(loginQuery);
            stmt.setString(1, number);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JSONObject loginData = new JSONObject();
                loginData.put(Constants.ID, rs.getLong(Constants.ACCOUNT__ID));
                loginData.put(Constants.ACCOUNT__ACTIVE, rs.getBoolean(Constants.ACCOUNT__ACTIVE));
                resp.getWriter().print(loginData.toString());
            } else
                throw new JSONException(Constants.INVALID_CREDENTIALS);

        } catch (JSONException e) {
            resp.setStatus(Constants.BAD_REQUEST);
            resp.getWriter().print(Main.getStackTrace(e));
        } catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            resp.getWriter().print(Main.getStackTrace(e));
        }
    }
}
