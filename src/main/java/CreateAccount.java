import com.twilio.sdk.TwilioRestException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateAccount {
    public static void createAccount(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException, JSONException {
        try {
            String phoneNumber = jsonObject.getString(Constants.PHONE_NUMBER);
            String password = jsonObject.getString(Constants.PASSWORD);
            String insertSQL = "INSERT into accounts (account__phone_number, password, daily_call_cntr) VALUES " +
                    "(?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertSQL);
            stmt.setString(1, phoneNumber);
            stmt.setString(2, password);
            stmt.setInt(3, 0);
        }
        catch (SQLException e) {
        }
    }
}
