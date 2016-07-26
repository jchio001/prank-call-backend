import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
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

import static javax.swing.UIManager.getInt;

public class ActivateAccount {
    public static void createAccount(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException, TwilioRestException {
        try {
            int phoneNumber = jsonObject.getInt(Constants.PHONE_NUMBER);
            int confirmKey = jsonObject.getInt(Constants.KEY);
            if (!(1000 <= confirmKey && confirmKey <= 9999)) {
                throw new JSONException("Invalid Key");
            }

            String checkKeyQuery = "Select COUNT(*) from account WHERE account__phone_number = ? and " +
                    "confirmKey = ? LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(checkKeyQuery);
            stmt.setInt(1, phoneNumber);
            stmt.setInt(2, confirmKey);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) != 1) {
                    resp.setStatus(Constants.BAD_REQUEST);
                }
            }
            else {
                resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            }

        }
        catch (JSONException e) {
            resp.setStatus(Constants.BAD_REQUEST);
        }
        catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
        }
    }
}
