import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonathan Chiou on 7/22/2016.
 */
public class MakeCall {
    public static void makeCall(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException, TwilioRestException, JSONException {
        String receiverNumber = jsonObject.getString(Constants.RECEIVER_NUMBER_KEY);
        long accountId = jsonObject.getLong(Constants.ID);
        String password = jsonObject.getString(Constants.PASSWORD);

        try {
            String getStatusQuery = "Select account__subbed, account__active from account WHERE account__id = ? and account__password = ? LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(getStatusQuery);
            stmt.setLong(1, accountId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean active = rs.getBoolean(Constants.ACCOUNT__ACTIVE);
                if (active) {
                    TwilioRestClient client = new TwilioRestClient(Constants.ACCOUNT_SID, Constants.AUTH_TOKEN);
                    Account account = client.getAccount();
                    CallFactory callFactory = account.getCallFactory();
                    Map<String, String> callParams = new HashMap<>();
                    callParams.put(Constants.TO, receiverNumber);
                    callParams.put(Constants.FROM, Constants.FROM_NUMBER);
                    callParams.put(Constants.METHOD, Constants.GET);
                    callParams.put(Constants.URL, "https://raw.githubusercontent.com/jchio001/TwilioXML/master/Response.xml");
                    Call call = callFactory.create(callParams);
                }
                else
                    throw new JSONException("Account is not active");
            }
            else {
                throw new JSONException("Account doesn't exist");
            }
        }
        catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            resp.getWriter().write(Main.getStackTrace(e));
        }
    }
}
