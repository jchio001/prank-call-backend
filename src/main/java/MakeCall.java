import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MakeCall {
    public static void makeCall(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException, TwilioRestException, JSONException {
        String receiverNumber = jsonObject.getString(Constants.RECEIVER_NUMBER_KEY);
        long accountId = jsonObject.getLong(Constants.ID);
        String password = jsonObject.getString(Constants.PASSWORD);

        try {
            String getStatusQuery = "Select account__subbed, account__active, account__last_call, account__daily_call_cntr from account WHERE account__id = ? and account__password = ? LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(getStatusQuery);
            stmt.setLong(1, accountId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp timestamp = rs.getTimestamp(Constants.ACCOUNT__LAST_CALL);
                boolean isSubbed = rs.getBoolean(Constants.ACCOUNT__SUBBED);
                int dailyCallCntr = rs.getInt(Constants.ACCOUNT__DAILY_CALL_CNTR);
                if (!isSubbed && (dailyCallCntr > 3)) {
                    throw new JSONException("User has exceeded daily call limit");
                }
                String updateSQL;
                if (timestamp != null) {
                    Date lastCallDate = new Date(timestamp.getTime());
                    Date today = new Date();
                    if (DateUtils.isSameDay(lastCallDate, today)) {
                        updateSQL = "Update account set account__last_call = CURRENT_TIMESTAMP, account__daily_call_cntr " +
                                "= account__daily_call_cntr + 1 WHERE account__id = ?";
                    }
                    else {
                        updateSQL = "Update account set account__last_call = CURRENT_TIMESTAMP, account__daily_call_cntr " +
                                "= 1 WHERE account__id = ?";
                    }
                }
                else {
                    updateSQL = "Update account set account__last_call = CURRENT_TIMESTAMP, account__daily_call_cntr " +
                            "= account__daily_call_cntr + 1 WHERE account__id = ?";
                }
                stmt = connection.prepareStatement(updateSQL);
                stmt.setLong(1, accountId);
                stmt.executeUpdate();
                makeCall(receiverNumber);
            } else {
                throw new JSONException("Account doesn't exist");
            }
        } catch (SQLException e) {
            resp.setStatus(Constants.INTERNAL_SERVER_ERROR);
            resp.getWriter().write(Main.getStackTrace(e));
        }
    }

    public static void makeCall(String receiverNumber) throws TwilioRestException {
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
}
