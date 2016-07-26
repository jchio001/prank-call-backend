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


public class ActivateAccount {
    public static void activateAccount(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException, TwilioRestException {
        try {
            String phoneNumber = jsonObject.getString(Constants.PHONE_NUMBER);
            String password = jsonObject.getString(Constants.PASSWORD);
            int confirmKey = jsonObject.getInt(Constants.KEY);
            if (!(1000 <= confirmKey && confirmKey <= 9999)) {
                throw new JSONException("Invalid Key");
            }

            String activateAccQuery = "UPDATE account SET account__active = true WHERE account__phone_number = ? and " +
                    "account__password = ? and account__confirm_key = ? and account__active = false";
            PreparedStatement stmt = connection.prepareStatement(activateAccQuery);
            stmt.setString(1, phoneNumber);
            stmt.setString(2, password);
            stmt.setInt(3, confirmKey);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new JSONException("Invalid credentials");
            }
        } catch (JSONException e) {
            resp.setStatus(Constants.BAD_REQUEST);
        } catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
        }
    }
}
