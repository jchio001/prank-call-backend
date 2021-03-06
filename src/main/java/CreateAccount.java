import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateAccount {
    public static void createAccount(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
        throws IOException, TwilioRestException {
        try {
            String phoneNumber = jsonObject.getString(Constants.PHONE_NUMBER);
            if (phoneNumber.isEmpty()) {
                throw new JSONException("Empty phone number.");
            }

            if (!StringUtils.isNumeric(phoneNumber) || phoneNumber.length() != 10) {
                throw new JSONException("Invalid phone number");
            }

            Random random = new Random();
            String password = jsonObject.getString(Constants.PASSWORD);
            int confirmKey = genConfirmKey(random);

            String insertSQL = "INSERT into account (account__phone_number, account__password, account__confirm_key) " +
                "VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, phoneNumber);
            stmt.setString(2, password);
            stmt.setInt(3, confirmKey);
            resp.getWriter().print(executeQueryGetId(stmt, resp));

            TwilioRestClient client = new TwilioRestClient(Constants.ACCOUNT_SID, Constants.AUTH_TOKEN);
            List<org.apache.http.NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(Constants.TO, phoneNumber));
            params.add(new BasicNameValuePair(Constants.FROM, Constants.FROM_NUMBER));
            params.add(new BasicNameValuePair(Constants.BODY, Constants.MSG + Integer.toString(confirmKey)));

            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message msg = messageFactory.create(params);
        } catch (JSONException e) {
            resp.setStatus(Constants.BAD_REQUEST);
            resp.getWriter().write(Main.getStackTrace(e));
        } catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            resp.getWriter().write(Main.getStackTrace(e));
        }
    }

    public static int genConfirmKey(Random random) {
        return random.nextInt(9000) + 1000;
    }

    public static String executeQueryGetId(PreparedStatement stmt, HttpServletResponse resp) throws SQLException, JSONException {
        long id;
        int affectedRows = stmt.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException();
        }
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            id = generatedKeys.getLong(1);
        } else {
            throw new SQLException();
        }

        JSONObject accountJSON = new JSONObject();
        accountJSON.put(Constants.ID, id);
        accountJSON.put(Constants.ACCOUNT__ACTIVE, false);
        return accountJSON.toString();
    }
}
