import com.twilio.sdk.TwilioRestException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateAccount {
    public static void createAccount(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException{
        try {
            String phoneNumber = jsonObject.getString(Constants.PHONE_NUMBER);
            if (phoneNumber.isEmpty()) {
                throw new JSONException("Empty phone number.");
            }

            String password = jsonObject.getString(Constants.PASSWORD);
            String insertSQL = "INSERT into accounts (account__phone_number, password) VALUES " +
                    "(?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertSQL);
            stmt.setString(1, phoneNumber);
            stmt.setString(2, password);
            resp.getWriter().print(executeQueryGetId(stmt, resp));
        }
        catch (JSONException e) {
            resp.setStatus(Constants.BAD_REQUEST);
        }
        catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
        }
    }

    public static String executeQueryGetId(PreparedStatement stmt, HttpServletResponse resp) throws SQLException, JSONException{
        long id;
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException();
        }
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            id = generatedKeys.getLong(1);
        }
        else {
            throw new SQLException();
        }

        JSONObject loginJSON = new JSONObject();
        loginJSON.put(Constants.ID, id);
        return loginJSON.toString();
    }
}
