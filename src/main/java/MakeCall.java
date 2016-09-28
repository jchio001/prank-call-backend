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
        try {
            String receiverNumber = jsonObject.getString(Constants.RECEIVER_NUMBER_KEY);
            long accountId = jsonObject.getLong(Constants.ID);
            if (accountId == -1) {
                makeTrialCall(req, connection, receiverNumber);
                return;
            }

            String password = jsonObject.getString(Constants.PASSWORD);

            String getStatusQuery = "Select account__phone_number, account__subbed, account__active, account__last_call, account__daily_call_cntr " +
                "from account WHERE account__id = ? and account__password = ? LIMIT 1";
            PreparedStatement stmt = connection.prepareStatement(getStatusQuery);
            stmt.setLong(1, accountId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String fromNumber = rs.getString(Constants.ACCOUNT__PHONE_NUMBER);
                Timestamp timestamp = rs.getTimestamp(Constants.ACCOUNT__LAST_CALL);
                boolean isSubbed = rs.getBoolean(Constants.ACCOUNT__SUBBED);
                int dailyCallCntr = rs.getInt(Constants.ACCOUNT__DAILY_CALL_CNTR);
                String updateSQL;
                Date today = new Date();

                if (timestamp != null) {
                    Date lastCallDate = new Date(timestamp.getTime());
                    if (DateUtils.isSameDay(lastCallDate, today)) {
                        if (!isSubbed && (dailyCallCntr >= 3)) {
                            throw new JSONException(Constants.EXCEEDED_CALL_LIMIT);
                        }

                        updateSQL = "Update account set account__last_call = CURRENT_TIMESTAMP, account__daily_call_cntr " +
                            "= account__daily_call_cntr + 1, account__total_cnt = account__total_cnt + 1 WHERE account__id = ?";
                    } else {
                        updateSQL = "Update account set account__last_call = CURRENT_TIMESTAMP, account__daily_call_cntr " +
                            "= 1, account__total_cnt = account__total_cnt + 1 WHERE account__id = ?";
                    }
                } else {
                    updateSQL = "Update account set account__last_call = CURRENT_TIMESTAMP, account__daily_call_cntr " +
                        "= account__daily_call_cntr + 1, account__total_cnt = account__total_cnt + 1 WHERE account__id = ?";
                }
                stmt = connection.prepareStatement(updateSQL);
                stmt.setLong(1, accountId);
                makeCall(receiverNumber);
                stmt.executeUpdate();
                updateHistory(connection, fromNumber, receiverNumber, today);
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
        callParams.put(Constants.URL, "https://handler.twilio.com/twiml/EH97f8c542503bea156b00ab7ad72adc42");
        Call call = callFactory.create(callParams);
    }

    public static void makeTrialCall(HttpServletRequest req, Connection connection, String receiverNumber) throws JSONException, SQLException, TwilioRestException {
        String ipAddr = req.getHeader("X-FORWARDED-FOR");
        if (ipAddr == null) {
            ipAddr = req.getRemoteAddr();
        }
        String selectSQL = "Select trial_call__last_call, trial_call__daily_call_cntr FROM trial_call WHERE trial_call__ip_addr = " +
            "?";
        PreparedStatement stmt = connection.prepareStatement(selectSQL);
        stmt.setString(1, ipAddr);

        Date today = new Date();
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Timestamp timestamp = rs.getTimestamp(Constants.TRIAL_CALL__LAST_CALL);

            int dailyCallCntr = rs.getInt(Constants.TRIAL_CALL__DAILY_CALL_CNTR);
            Date lastCallDate = new Date(timestamp.getTime());

            boolean isSameDay = DateUtils.isSameDay(lastCallDate, today);
            String updateSQL;
            if (isSameDay && dailyCallCntr >= 2) {
                throw new JSONException(Constants.EXCEEDED_CALL_LIMIT);
            } else if (isSameDay && dailyCallCntr < 2) {
                updateSQL = "UPDATE trial_call SET trial_call__last_call = CURRENT_TIMESTAMP, trial_call__daily_call_cntr = " +
                    "trial_call__daily_call_cntr + 1, trial_call__total_cnt = trial_call__total_cnt + 1 WHERE trial_call__ip_addr = ? ";

            } else {
                updateSQL = "UPDATE trial_call SET trial_call__last_call = CURRENT_TIMESTAMP, trial_call__daily_cntr = 1, trial_call__total_cnt = " +
                    "trial_call__total_cnt + 1 WHERE trial_call__ip_addr = ?";
            }
            makeCall(receiverNumber);
            stmt = connection.prepareStatement(updateSQL);
            stmt.setString(1, ipAddr);
            stmt.executeUpdate();
        } else {
            String insertSQL = "INSERT into trial_call(trial_call__ip_addr, trial_call__last_call, trial_call__daily_call_cntr, " +
                "trial_call__total_cnt) VALUES (?, CURRENT_TIMESTAMP, 1, 1)";
            stmt = connection.prepareStatement(insertSQL);
            stmt.setString(1, ipAddr);
            stmt.executeUpdate();
            makeCall(receiverNumber);
            updateHistory(connection, ipAddr, receiverNumber, today);
        }
    }

    public static void updateHistory(Connection connection, String from, String to, Date date) throws SQLException {
        String insertSQL = "INSERT into history(history__from, history__to, history__timestamp) VALUES(?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(insertSQL);
        stmt.setString(1, from);
        stmt.setString(2, to);
        stmt.setTimestamp(3, new Timestamp(date.getTime()));
        stmt.executeUpdate();
    }
}
